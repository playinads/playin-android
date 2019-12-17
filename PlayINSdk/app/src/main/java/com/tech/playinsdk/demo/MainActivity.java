package com.tech.playinsdk.demo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tech.playinsdk.PlayInSdk;
import com.tech.playinsdk.http.HttpException;
import com.tech.playinsdk.listener.HttpListener;
import com.tech.playinsdk.listener.InitListener;
import com.tech.playinsdk.util.PlayLog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mInit;
    private Button mPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

//        PlayInSdk.getInstance().setTest(true);
    }

    private void initData() {
        mInit = findViewById(R.id.init);
        mPlay = findViewById(R.id.play);
        mInit.setOnClickListener(this);
        mPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.init:
                configPlayin();
                break;

            case R.id.play:
                toPlay();
                break;
            default:
                break;
        }
    }

    // Initialize the game SDK
    private void configPlayin() {
        PlayInSdk.getInstance().setLog(true);
        PlayInSdk.getInstance().configWithKey(Constants.SDK_KEY, new InitListener() {
            @Override
            public void success() {
                checkAvailable();
            }

            @Override
            public void failure(Exception ex) {
                Toast.makeText(MainActivity.this, "Initialization failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check game can to play
    private void checkAvailable() {
        PlayInSdk.getInstance().checkAvailable(Constants.SDK_KEY, new HttpListener<Boolean>() {
            @Override
            public void success(Boolean result) {
                mPlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(HttpException e) {
                Toast.makeText(MainActivity.this, "No equipment to play with", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toPlay() {
        Intent intent = new Intent(MainActivity.this, PlayActivity.class);
        startActivity(intent);
    }
}
