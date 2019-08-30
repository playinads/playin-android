package com.tech.playinsdk.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tech.playinsdk.PlayInView;
import com.tech.playinsdk.listener.PlayListener;
import com.tech.playinsdk.util.PILog;

public class PlayActivity extends AppCompatActivity implements PlayListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        getSupportActionBar().hide();
        playGame();
    }

    private void playGame() {
        String adId = "hSXyxiRK";
        String appName = "Word Cookies!™";
        String appIcon = "https://playinads.com/static/demo_app/f74e9808-5cd1-11e9-9c10-005056997473.jpg";
        String appCover = "https://playinads.com/static/demo_app/f7659af8-5cd1-11e9-9c10-005056997473.jpg";
        String appDownload = "https://play.google.com/store/apps/details?id=com.bitmango.go.wordcookies";
        int playDuration = 120;
        int playTime = 2;

        PlayInView playView = findViewById(R.id.playView);
        playView.play(adId, appName, appIcon, appCover, appDownload,
                playDuration, playTime, this);
    }

    @Override
    public void onPlaystart() {
        hideLoading();
    }

    @Override
    public void onPlayClose() {
        finish();
    }

    @Override
    public void onPlayError(Exception ex) {
        PILog.e("onPlayError " + ex);
    }

    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loading).setVisibility(View.GONE);
            }
        });
    }
}
