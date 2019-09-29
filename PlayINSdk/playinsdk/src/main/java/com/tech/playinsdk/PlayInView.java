package com.tech.playinsdk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private TextView videoTimeTv;
    private TextView totalTimeTv;

    private int videoTime;
    private int totalTime;

    private boolean isDetached, isPause, isFinish, isDownload;

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

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            if (isPause) {
                // 返回前台
                getHandler().postDelayed(videoTimeRunnable, 1000);
            }
            isPause = false;
        } else if (visibility == 4) {
            if (!isPause) {
                // 进入后台
                getHandler().removeCallbacks(videoTimeRunnable);
            }
            isPause = true;
        }

    }

    /**
     * playGame
     * @param adid
     * @param playDuration
     * @param listener
     */
    public void play(String adid, int playDuration, final PlayListener listener) {
        this.videoTime = playDuration;
        this.playListener = listener;
        this.requestPlayInfo(adid);
    }

    @Override
    public void onGameStart() {
        playListener.onPlaystart();
        countVideoTime();
        countTotalTime();
    }

    @Override
    public void onGameError(final Exception ex) {
        if (!isFinish && !isDownload) {
            playListener.onPlayError(ex);
        }
        try {
            reportPlayEnd();
            showPlayFinish();
            // 异常
            getHandler().removeCallbacksAndMessages(null);
            totalTimeTv.setVisibility(GONE);
            videoTimeTv.setText("Skip Ads");
            videoTimeTv.setOnClickListener(PlayInView.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int cId = v.getId();
        if (cId == R.id.videoTimeTv) {
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

                // 测试数据  begin
//                result.setOsType(2);
//                result.setOrientation(1);
//                result.setDuration(30);
//                videoTime = 5;
                // end

                initView(result);
                initData(result);
                connectPlayIn(result);
                setScreenOrientation(result);
            }

            @Override
            public void failure(final HttpException userActionExc) {
                playListener.onPlayError(userActionExc);
            }
        });
    }

    private void setScreenOrientation(PlayInfo playInfo) {
        try {
            Activity curActivity = (Activity) getContext();
            if (playInfo.getOsType() == 1) {
                // ios 设备端全部是竖屏
                curActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                // android 设备端动态旋转
                if (playInfo.getOrientation() == 0) {
                    // 竖屏
                    curActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    // 横屏
                    curActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private View getFuncView(PlayInfo playInfo) {
        View rootView;
        if (playInfo.getOsType() == 1) {
            // ios 设备端
            if (playInfo.getOrientation() == 0) {
                rootView = LayoutInflater.from(getContext()).inflate(R.layout.playin_view_portrait, null);
            } else {
                rootView = LayoutInflater.from(getContext()).inflate(R.layout.playin_view_landscape, null);
            }
        } else {
            // android 设备端
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.playin_view_portrait, null);
        }
        return rootView;
    }

    private void initView(PlayInfo playInfo) {
        View rootView = getFuncView(playInfo);
        this.addView(rootView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        appInfoView = findViewById(R.id.appInfoView);
        appInfoView.setOnClickListener(this);
        videoTimeTv = findViewById(R.id.videoTimeTv);
        totalTimeTv = findViewById(R.id.totlalTimeTv);

        findViewById(R.id.downloadTv).setOnClickListener(this);
        findViewById(R.id.menuLayout).setOnClickListener(this);


        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.playin_menu);
        ImageView menuIv = findViewById(R.id.menuIv);
        menuIv.startAnimation(anim);
    }

    private void initData(PlayInfo playInfo) {
        adapterLandscape();
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

    private void adapterLandscape() {
        if (playInfo.getOsType() == 1) {
            // ios 设备端
            if (playInfo.getOrientation() == 1) {
                // 横屏
                View fixBug = findViewById(R.id.fixbug);
                if (fixBug != null) fixBug.setVisibility(VISIBLE);

                View appView = findViewById(R.id.appView);
                appView.setRotation(90);
                appView.setTranslationY(-TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics()));
            }
        } else {
            // android 设备端
            if (playInfo.getOrientation() == 1) {
                // 横屏
                View appView = findViewById(R.id.appView);
                ViewGroup.LayoutParams params = appView.getLayoutParams();
                params.width = getResources().getDisplayMetrics().heightPixels * 2 / 3;
                appView.setLayoutParams(params);
            }
        }
    }

    private void connectPlayIn(PlayInfo playInfo) {
        videoTime = Math.min(videoTime, playInfo.getDuration());
        totalTime = playInfo.getDuration();
        GameView gameView = findViewById(R.id.gameview);
        gameView.startConnect(playInfo, PlayInView.this);
    }

    private void goDownload() {
        reportDownload();
        String downloadUrl = playInfo.getGoogleplayUrl();
        playListener.onPlayDownload(downloadUrl);
    }

    private Runnable videoTimeRunnable = new Runnable() {
        @Override
        public void run() {
            videoTimeTv.setText("Skip Ads ( " + videoTime + " )");
            videoTime--;
            if (videoTime >= 0) {
                getHandler().postDelayed(this, 1000);
            } else {
                videoTimeTv.setText("Skip Ads");
                videoTimeTv.setOnClickListener(PlayInView.this);
                findViewById(R.id.menuLayout).setVisibility(VISIBLE);
                playListener.onPlayForceTime();
            }
        }
    };

    // 激励视频倒计时
    private void countVideoTime() {
        if (videoTime <= 0) {
            videoTimeTv.setText("Skip Ads");
            videoTimeTv.setOnClickListener(this);
            return;
        }
        videoTimeTv.setText("Skip Ads ( " + videoTime + " )");
        getHandler().postDelayed(videoTimeRunnable, 1000);
    }

    // 总试玩时长倒计时
    private void countTotalTime() {
        totalTimeTv.setText(totalTime + "s | ");
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                totalTimeTv.setText(totalTime + "s | ");
                totalTime--;
                if (totalTime >= 0) {
                    getHandler().postDelayed(this, 1000);
                } else {
                    totalTimeTv.setVisibility(GONE);
                    reportPlayEnd();
                    showPlayFinish();
                }
            }
        }, 1000);
    }

    private void showPlayFinish() {
        findViewById(R.id.menuLayout).setVisibility(GONE);          // 隐藏menu
        appInfoView.setVisibility(VISIBLE);                         // 显示下载弹窗
        // 时间到不让关闭弹窗
        if (isFinish) {
            appInfoView.setOnClickListener(null);
            findViewById(R.id.continueTv).setVisibility(INVISIBLE);
        }
    }

    private void showMenuInfo() {
        if (findViewById(R.id.menuLayout).getVisibility() == GONE) return;
        findViewById(R.id.menuLayout).setVisibility(GONE);          // 隐藏menu

        int animId = playInfo.getOrientation() == 0 ? R.anim.playin_portrait_in : R.anim.playin_landscape_in;
        if (playInfo.getOsType() == 2) {
            // 安卓设备端
            animId = R.anim.playin_portrait_in;
        }
        Animation anim = AnimationUtils.loadAnimation(getContext(), animId);
        appInfoView.setVisibility(VISIBLE);                         // 显示下载弹窗
        appInfoView.startAnimation(anim);
    }

    private void hidMenuInfo() {
        if (appInfoView.getVisibility() == GONE) return;
        appInfoView.setVisibility(GONE);
        int animId = playInfo.getOrientation() == 0 ? R.anim.playin_portrait_out : R.anim.playin_landscape_out;
        if (playInfo.getOsType() == 2) {
            // 安卓设备端
            animId = R.anim.playin_portrait_out;
        }
        Animation anim = AnimationUtils.loadAnimation(getContext(), animId);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.menuLayout).setVisibility(VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        appInfoView.startAnimation(anim);
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
