package com.tech.playinsdk.decoder;

import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class VideoDecoder implements Runnable {

    public interface DecoderListener {
        void decoderSuccess();
    }

    private DecoderListener decoderListener;
    private BlockingQueue<byte[]> videoQueue = new LinkedBlockingQueue<>(30);

    protected int videoWidth;
    protected int videoHeight;
    protected int videoRotate;

    private Thread thread;

    private boolean loopFlag;
    private boolean initCodec;
    private boolean decodeSuccess;

    protected abstract boolean initDecoder(Surface surface);
    protected abstract void onFrame(byte[] buf, int offset, int length);
    protected abstract void releaseDecoder();
    public abstract void updateRotate(int videoRotate);

    public VideoDecoder(int videoWidth, int videoHeight, int videoRotate) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.videoRotate = videoRotate;
    }

    public void setDecoderListener(DecoderListener decoderListener) {
        this.decoderListener = decoderListener;
    }

    @Override
    public void run() {
        try {
            while (loopFlag) {
                byte[] buf = videoQueue.poll(500, TimeUnit.MILLISECONDS);
                if (initCodec && null != buf && buf.length > 0) onFrame(buf, 0, buf.length);
            }
        } catch (Exception e) {
        } finally {
            releaseDecoder();
        }
    }

    public synchronized void setDisplayHolder(SurfaceHolder holder) {
        if (initDecoder(holder.getSurface())) {
            initCodec = true;
        }
    }

    public void sendVideoData(byte[] buf) {
        if (null != videoQueue) {
            videoQueue.offer(buf);
        }
    }

    public void start() {
        videoQueue.clear();
        loopFlag = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        initCodec = false;
    }

    public void resume() {
        initCodec = true;
    }

    public void stop() {
        videoQueue.clear();
        loopFlag = false;
        initCodec = false;
        decodeSuccess = false;
        thread.interrupt();
    }

    protected boolean tryCodecSuccess(int value) {
        if (decodeSuccess == false) {
            if (value == 7 || value == 8) {
                decodeSuccess = true;
                if (null != decoderListener) {
                    decoderListener.decoderSuccess();
                }
            }
        }
        return decodeSuccess;
    }
}
