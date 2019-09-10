package com.tech.playinsdk.decoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import com.tech.playinsdk.util.PlayLog;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioDecoder implements Runnable {

    private BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>(10);

    private static final int mSampleRateInHz = 44100;
    private static final int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private AudioTrack audioTrack;
    private boolean initCodec;

    public void sendAudioData(byte[] buf) {
        if (null != audioQueue) {
            audioQueue.offer(buf);
        }
    }

    public synchronized void start() {
        audioQueue.clear();
        if (initAudioTrack()) {
            initCodec = true;
            new Thread(this).start();
        }
    }

    public synchronized void stop() {
        audioQueue.clear();
        initCodec = false;
        if (this.audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    @Override
    public void run() {
        while (initCodec) {
            try {
                byte[] buf = audioQueue.take();
//                PlayLog.e("音频数据: " + Arrays.toString(buf));
                this.audioTrack.write(buf, 0, buf.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean initAudioTrack() {
        try {
            int bufferSize = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);
            this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRateInHz,
                    mChannelConfig, mAudioFormat, bufferSize, AudioTrack.MODE_STREAM);
            this.audioTrack.play();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
