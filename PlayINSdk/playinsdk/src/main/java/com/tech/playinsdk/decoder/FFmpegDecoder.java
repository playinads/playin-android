package com.tech.playinsdk.decoder;

import android.view.Surface;

public class FFmpegDecoder extends VideoDecoder {

    static {
        System.loadLibrary("playin");
    }

    public native int ffmpegInit(int width, int height, Surface surface);
    public native int ffmpegDecoding(byte[] data);
    public native void ffmpegClose();
    public long ffmpegHandle;            // Don't delete, c holds the pointer to the object

    private boolean init;

    public FFmpegDecoder(int videoWidth, int videoHeight) {
        super(videoWidth, videoHeight);
    }

    @Override
    protected boolean initDecoder(int videoWidth, int videoHeight, Surface surface) {
        boolean result = ffmpegInit(videoWidth, videoHeight, surface) >= 0;
        init = result;
        return result;
    }

    protected void onFrame(byte[] buf, int offset, int length) {
        int value = buf[4] & 0x0f;
        if (!tryCodecSuccess(value)) {
            return;
        }
        if (init) {
            ffmpegDecoding(buf);
        }
    }

    @Override
    protected void releaseDecoder() {
        if (init) {
            ffmpegClose();
        }
        init = false;
    }
}
