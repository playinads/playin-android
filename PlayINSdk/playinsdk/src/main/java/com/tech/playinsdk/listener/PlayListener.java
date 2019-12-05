package com.tech.playinsdk.listener;

public interface PlayListener {

    void onPlaystart();
    void onPlayFinish();
    void onPlayError(Exception ex);
}
