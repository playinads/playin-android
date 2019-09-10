package com.tech.playinsdk.decoder;

import android.view.Surface;
import android.view.SurfaceHolder;

import com.tech.playinsdk.util.PlayLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class VideoDecoder implements Runnable {

    public interface DecoderListener {
        void decoderSuccess();
    }

    private DecoderListener decoderListener;

    private BlockingQueue<byte[]> videoQueue = new LinkedBlockingQueue<>(30);
    private int videoWidth;
    private int videoHeight;

    private Thread thread;
    private boolean loopFlag;
    private boolean initCodec;
    private boolean decodeSuccess;

    protected abstract boolean initDecoder(int videoWidth, int videoHeight, Surface surface);

    protected abstract void onFrame(byte[] buf, int offset, int length);

    protected abstract void releaseDecoder();

    public VideoDecoder(int videoWidth, int videoHeight) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    public void setDecoderListener(DecoderListener decoderListener) {
        this.decoderListener = decoderListener;
    }

    @Override
    public void run() {
        try {
            while (loopFlag) {
                byte[] buf = videoQueue.poll(500, TimeUnit.MILLISECONDS);
                if (initCodec && null != buf) onFrame(buf, 0, buf.length);
            }
            releaseDecoder();
        } catch (Exception e) {
            releaseDecoder();
            e.printStackTrace();
        }
    }

    public synchronized void setDisplayHolder(SurfaceHolder holder) {
        if (initDecoder(this.videoWidth, this.videoHeight, holder.getSurface())) {
            initCodec = true;
        }
    }

    public void sendVideoData(byte[] buf) {
        if (null != videoQueue) {
            videoQueue.offer(buf);
        }
    }

    public synchronized void start() {
        videoQueue.clear();
        loopFlag = true;
        thread = new Thread(this);
        thread.start();

    }

    public void pause() {
        initCodec = false;
        PlayLog.e("VideoDecoder  pause");
    }

    public void resume() {
        initCodec = true;
        PlayLog.e("VideoDecoder  resume");
    }

    public synchronized void stop() {
        videoQueue.clear();
        videoQueue = null;
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
