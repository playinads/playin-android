package com.tech.playinsdk.decoder;

import android.view.Surface;

public class FFmpegDecoder extends BaseDecoder {

    static {
        System.loadLibrary("playin");
    }

    public native int ffmpegInit(int width, int height, Surface surface);
    public native int ffmpegDecoding(byte[] data);
    public native void ffmpegClose();


    public FFmpegDecoder(int videoWidth, int videoHeight) {
        super(videoWidth, videoHeight);
    }

    @Override
    protected boolean initDecoder(int videoWidth, int videoHeight, Surface surface) {
        return ffmpegInit(videoWidth, videoHeight, surface) >= 0;
    }

    protected void onFrame(byte[] buf, int offset, int length) {
        int value = buf[4] & 0x0f;
        if (!tryCodecSuccess(value)) {
            return;
        }
        ffmpegDecoding(buf);
    }

    @Override
    protected void releaseDecoder() {
//        ffmpegClose();
    }
}
