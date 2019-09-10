package com.tech.playinsdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tech.playinsdk.http.HttpException;
import com.tech.playinsdk.http.HttpHelper;
import com.tech.playinsdk.listener.HttpListener;
import com.tech.playinsdk.listener.PlayListener;
import com.tech.playinsdk.model.entity.PlayInfo;
import com.tech.playinsdk.util.Constants;
import com.tech.playinsdk.util.GameView;
import com.tech.playinsdk.util.PlayLog;

public class PlayInView extends FrameLayout implements View.OnClickListener, GameView.GameListener {

    private PlayInfo playInfo;
    private PlayListener playListener;

    private View appInfoView;

    private int playDuration;
    private int progressCount;

    private boolean isDetached;
    private boolean isFinish, isDownload;


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
        getHandler().removeCallbacksAndMessages(null);
        reportPlayEnd();
    }

    /**
     * playGame
     * @param adid
     * @param playDuration
     * @param listener
     */
    public void play(String adid, int playDuration, final PlayListener listener) {
        this.playDuration = playDuration;
        this.playListener = listener;
        this.requestPlayInfo(adid);
    }

    @Override
    public void onGameStart() {
        playListener.onPlaystart();
        countdown();
        progress();
    }

    @Override
    public void onGameError(final Exception ex) {
        showPlayFinish();
        if (!isFinish && !isDownload) {
            playListener.onPlayError(ex);
        }
    }

    @Override
    public void onClick(View v) {
        int cId = v.getId();
        if (cId == R.id.closeIv) {
            playListener.onPlayClose();
        } else if (cId == R.id.downloadTv) {
            goDownload();
        } else if (cId == R.id.appInfoView) {
            hidMenuInfo();
        } else if (cId == R.id.menuLayout) {
            showMenuInfo();
        }
    }

    private void requestPlayInfo(String adid) {
        PlayInSdk.getInstance().userActions(adid, new HttpListener<PlayInfo>() {
            @Override
            public void success(PlayInfo result) {
                if (isDetached) return;
                playInfo = result;
                initView(result);
                initData(result);
                connectPlayIn(result);
            }

            @Override
            public void failure(final HttpException userActionExc) {
                playListener.onPlayError(userActionExc);
            }
        });
    }

    private void initView(PlayInfo playInfo) {
//        playInfo.setOrientation(1);
//        playInfo.setDuration(20);

        View rootView;
        if (playInfo.getOrientation() == 0) {
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.playin_view_portrait, null);
        } else {
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.playin_view_landscape, null);
        }
        this.addView(rootView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        appInfoView = findViewById(R.id.appInfoView);
        appInfoView.setOnClickListener(this);
        findViewById(R.id.closeIv).setOnClickListener(this);
        findViewById(R.id.downloadTv).setOnClickListener(this);
        findViewById(R.id.menuLayout).setOnClickListener(this);
    }

    private void initData(PlayInfo playInfo) {
        TextView appNameTv = findViewById(R.id.appName);
        TextView appAudienceTv = findViewById(R.id.appAudience);
        TextView appRateTv = findViewById(R.id.appRate);
        TextView commentTv = findViewById(R.id.commentTv);
        TextView adInfoTv = findViewById(R.id.adInfoTv);
        appNameTv.setText(playInfo.getAppName());
        appAudienceTv.setText(String.valueOf(playInfo.getAudience()));
        appRateTv.setText(String.valueOf(playInfo.getAppRate()));
        commentTv.setText(String.valueOf(playInfo.getCommentsCount()));
        adInfoTv.setText(playInfo.getCopywriting());

        final ImageView appIcon = findViewById(R.id.appIcon);
        HttpHelper.obtian().getHttpBitmap(playInfo.getAppIcon(), new HttpListener<Bitmap>() {
            @Override
            public void success(Bitmap result) {
                appIcon.setImageBitmap(result);
            }

            @Override
            public void failure(HttpException e) {
                PlayLog.e("获取APP Icon图片失败:  " + e);
            }
        });
    }

    private void connectPlayIn(PlayInfo playInfo) {
        playDuration = Math.min(playDuration, playInfo.getDuration());
        GameView gameView = findViewById(R.id.gameview);
        gameView.startConnect(playInfo, PlayInView.this);
    }

    private void goDownload() {
        reportDownload();
        String downloadUrl = playInfo.getGoogleplayUrl();
        playListener.onPlayDownload(downloadUrl);
    }

    private void countdown() {
        final TextView countView = findViewById(R.id.countDownTv);
        countView.setText(playDuration + "");
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                countView.setText(playDuration + "");
                playDuration--;
                if (playDuration >= 0) {
                    getHandler().postDelayed(this, 1000);
                } else {
                    showPlayFinish();
                    reportPlayEnd();
                }
            }
        }, 1000);
    }

    private void progress() {
        final View progressView = findViewById(R.id.progressView);
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) progressView.getLayoutParams();
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final int screenHeight = getResources().getDisplayMetrics().heightPixels;

        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressCount++;
                if (progressCount <= playInfo.getDuration()) {
                    if (playInfo.getOrientation() == 0) {
                        // 横屏
                        int progressWidth = progressCount * screenWidth / playInfo.getDuration();
                        params.width = progressWidth;
                    } else {
                        // 竖屏
                        int progressHeight = progressCount * screenHeight / playInfo.getDuration();
                        params.height = progressHeight;
                    }
                    progressView.setLayoutParams(params);
                    getHandler().postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void showPlayFinish() {
        findViewById(R.id.closeIv).setVisibility(VISIBLE);          // 显示关闭按钮
        findViewById(R.id.countDownTv).setVisibility(GONE);         // 隐藏倒计时
        findViewById(R.id.menuLayout).setVisibility(GONE);          // 隐藏menu
        appInfoView.setVisibility(VISIBLE);                         // 显示下载弹窗
        appInfoView.setOnClickListener(null);
    }

    private void showMenuInfo() {
        appInfoView.setVisibility(VISIBLE);                         // 显示下载弹窗
        findViewById(R.id.menuLayout).setVisibility(GONE);          // 隐藏menu
    }

    private void hidMenuInfo() {
        appInfoView.setVisibility(GONE);
        findViewById(R.id.menuLayout).setVisibility(VISIBLE);
    }


    private void reportDownload() {
        isDownload = true;
        if (null != playInfo && !TextUtils.isEmpty(playInfo.getToken())) {
            PlayInSdk.getInstance().report(playInfo.getToken(), Constants.Report.DOWN_LOAD);
        }
    }

    private void reportPlayEnd() {
        isFinish = true;
        if (null != playInfo && !TextUtils.isEmpty(playInfo.getToken())) {
            PlayInSdk.getInstance().report(playInfo.getToken(), Constants.Report.END_PLAY);
        }
    }
}
