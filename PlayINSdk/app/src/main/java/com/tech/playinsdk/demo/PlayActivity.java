package com.tech.playinsdk.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.tech.playinsdk.PlayInView;
import com.tech.playinsdk.listener.PlayListener;
import com.tech.playinsdk.util.PlayLog;

public class PlayActivity extends AppCompatActivity implements PlayListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        getSupportActionBar().hide();
        playGame();
    }

    private void playGame() {
        PlayInView playView = findViewById(R.id.playView);
        playView.play(Constants.ADID, 120, this);
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
        PlayLog.e("onPlayError " + ex);
        showErrorDialog();
    }

    @Override
    public void onPlayDownload(String url) {
        if (TextUtils.isEmpty(url) || "null".equals(url)) {
            Toast.makeText(this, "There is no googlePlay download url", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
            PlayLog.e("download app error：" + ex);
        }
    }

    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loading).setVisibility(View.GONE);
            }
        });
    }

    private void showErrorDialog() {
        if (isFinishing()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Exception, click confirm to return")
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
