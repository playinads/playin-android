package com.tech.playinsdk.listener;

public interface PlayListener {

    void onPlaystart();
    void onPlayClose();
    void onPlayError(Exception ex);
    void onPlayDownload(String url);
    void onPlayForceTime();         // 强制试玩
}
