package com.tech.playinsdk.listener;

public interface PlayListener {

    void didConnectSuccess(int duration);
    void didDisconnect(boolean manual);
    void didConnectFail(Exception ex);
}
