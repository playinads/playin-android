package com.tech.playinsdk.listener;

public interface PlayListener {

    void onPlayStart(int duration);
    void onPlayEnd(boolean manual);
    void onPlayError(Exception ex);
}
