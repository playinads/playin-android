package com.tech.playinsdk.decoder;

import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import com.tech.playinsdk.util.PlayLog;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioDecoder implements Runnable {

    private BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>(10);

    private AudioTrack audioTrack;
    private Thread thread;
    private boolean loopFlag;
    private boolean initCodec;

    public void sendAudioData(byte[] buf) {
        if (initCodec && null != audioQueue) {
            audioQueue.offer(buf);
        }
    }

    public void initAudioTrack(int sampleRateInHz, int channelConfig, int audioFormat) {
        try {
            int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
            this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                    channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
            this.audioTrack.play();
            initCodec = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            initCodec = false;
        }
    }

    public synchronized void start() {
        loopFlag = true;
        audioQueue.clear();
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        audioQueue.clear();
        loopFlag = false;
        initCodec = false;
        if (this.audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
        if (null != thread) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        while (loopFlag) {
            try {
                byte[] buf = audioQueue.take();
                if (initCodec) {
                    this.audioTrack.write(buf, 0, buf.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
