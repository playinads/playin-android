package com.tech.playinsdk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tech.playinsdk.http.HttpException;
import com.tech.playinsdk.listener.HttpListener;
import com.tech.playinsdk.listener.PlayListener;
import com.tech.playinsdk.model.entity.PlayInfo;
import com.tech.playinsdk.util.Analyze;
import com.tech.playinsdk.util.Constants;
import com.tech.playinsdk.util.GameView;

public class PlayInView extends FrameLayout implements GameView.GameListener {

    private PlayInfo playInfo;
    private PlayListener playListener;

    private int totalTime;
    private boolean audioState, autoRotate;
    private boolean isDetached, isFinish;

    private GameView gameView;

    public PlayInView(Context context) {
        super(context);
    }

    public PlayInView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDetached = true;
        gameView = null;
        playEnd();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapterGameSize();
            }
        }, 200);
    }

    public void establishConnection(String adid, PlayListener listener) {
        this.establishConnection(adid, true, true, listener);
    }

    public void establishConnection(String adid, boolean audioState, boolean autoRotate, PlayListener listener) {
        this.audioState = audioState;
        this.autoRotate = autoRotate;
        this.playListener = listener;
        this.requestPlayInfo(adid);
    }

    public void finish() {
        playListener.didDisconnect(true);
        playEnd();
    }

    /**
     * 设置视频清晰度
     * @param quality
     */
    public void setVideoQuality(int quality) {
        if (null != gameView) gameView.changeVideoQuality(quality);
    }

    /**
     * 设置声音状态
     * @param on
     */
    public void setAudioState(boolean on) {
        this.audioState = on;
        if (null != gameView) gameView.setAudioState(on);
    }

    public void setAutoRotate(boolean rotate) {
        this.autoRotate = rotate;
        setScreenOrientation();
    }

    private void requestPlayInfo(String adid) {
        String androidId = Settings.System.getString(getContext().getContentResolver(), Settings.System.ANDROID_ID);
        PlayInSdk.getInstance().userActions(adid, androidId, new HttpListener<PlayInfo>() {
            @Override
            public void success(PlayInfo result) {
                if (isDetached) return;
                playInfo = result;
                LayoutInflater.from(getContext()).inflate(R.layout.playin_view, PlayInView.this);
                connectPlayIn(result);
            }

            @Override
            public void failure(final HttpException userActionExc) {
                playListener.didConnectFail(userActionExc);
            }
        });
    }

    private void connectPlayIn(PlayInfo playInfo) {
        totalTime = playInfo.getDuration();
        gameView = findViewById(R.id.gameview);
        gameView.startConnect(playInfo, PlayInView.this);
        gameView.setAudioState(this.audioState);
        setScreenOrientation();
        adapterGameSize();
    }

    private void setScreenOrientation() {
        if (playInfo == null || !autoRotate) return;
        try {
            Activity curActivity = (Activity) getContext();
            if (playInfo.getOrientation() == 0) {
                curActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                curActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void adapterGameSize() {
        if (gameView == null ||  playInfo == null || isFinish) return;
        if (getWidth() != 0 && getHeight() != 0) {
            int srcWidth = playInfo.getDeviceWidth();
            int srcHeight = playInfo.getDeviceHeight();
            // 横屏
            if (playInfo.getOrientation() == 1) {
                srcWidth = playInfo.getDeviceHeight();
                srcHeight = playInfo.getDeviceWidth();
            }
            int destWidth;
            int destHeight;
            float scaleWidth = getWidth() * 1.0f / srcWidth;
            float scaleHeight = getHeight() * 1.0f / srcHeight;
            if (scaleWidth < scaleHeight) {
                destWidth = getWidth();
                destHeight = (int) (getWidth() * 1.0f * srcHeight / srcWidth);
            } else {
                destWidth = (int) (getHeight() * 1.0f * srcWidth / srcHeight);
                destHeight = getHeight();
            }
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) gameView.getLayoutParams();
            params.width = destWidth;
            params.height = destHeight;
            gameView.setLayoutParams(params);
        }
    }

    // 总试玩时长倒计时
    private void countTotalTime() {
        totalTime--;
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                totalTime--;
                if (totalTime > 0) {
                    getHandler().postDelayed(this, 1000);
                } else {
                    playListener.didDisconnect(false);
                    playEnd();
                }
            }
        }, 1000);
    }


    private void playEnd() {
        try {
            if (isFinish) return;
            isFinish = true;
            getHandler().removeCallbacksAndMessages(null);
            if (null != gameView) gameView.disconnect();
            if (null != playInfo && !TextUtils.isEmpty(playInfo.getToken())) {
                PlayInSdk.getInstance().report(playInfo.getToken(), Constants.Report.END_PLAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGameStart() {
        playListener.didConnectSuccess(playInfo.getDuration());
        countTotalTime();
    }

    @Override
    public void onGameError(final Exception ex) {
        if (!isFinish) {
            Analyze.getInstance().playError(ex);
            playListener.didConnectFail(ex);
        }
        playEnd();
    }
}
