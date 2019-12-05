package com.tech.playinsdk.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.tech.playinsdk.PlayInView;
import com.tech.playinsdk.listener.PlayListener;
import com.tech.playinsdk.util.PlayLog;

public class PlayActivity extends AppCompatActivity implements PlayListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        playGame();
        initView();
    }

    private void initView() {
        ToggleButton switcher = findViewById(R.id.toggle);
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PlayInView playView = findViewById(R.id.playView);
                playView.setAudioState(isChecked);
            }
        });
    }

    private void playGame() {
        PlayInView playView = findViewById(R.id.playView);
        playView.play(Constants.ADID, this);
//        playView.setAudioState(true);
//        playView.setAutoRotate(false);
    }

    public void finshGame(View view) {
        PlayInView playView = findViewById(R.id.playView);
        playView.finish();
    }

    @Override
    public void onPlaystart() {
        hideLoading();
        PlayLog.e("onPlaystart");
    }

    @Override
    public void onPlayFinish() {
        PlayLog.e("onPlayFinish");
        finish();
    }

    @Override
    public void onPlayError(Exception ex) {
        PlayLog.e("onPlayFinish " + ex.toString());
        hideLoading();
        showDialog(ex.getMessage());
    }

    private void hideLoading() {
        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    private void showDialog(String message) {
        if (isFinishing()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(message + ", click confirm to return")
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();
        dialog.setCancelable(false);
        dialog.show();
    }
}
