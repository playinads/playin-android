package com.tech.playinsdk.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class MediaDecoder extends VideoDecoder {

    private MediaCodec mediaCodec;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    public MediaDecoder(int videoWidth, int videoHeight, int videoRotate) {
        super(videoWidth, videoHeight, videoRotate);
    }

    @Override
    protected boolean initDecoder(Surface surface) {
        try {
            MediaFormat format = MediaFormat.createVideoFormat(MIMETYPE_VIDEO_AVC, videoWidth, videoHeight);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20);

            byte[] header_sps = {0, 0, 0, 1, 39, 77, 0, 30, -85, 64, -64, 42, -14, -38};
            byte[] header_pps = {0, 0, 0, 1, 40, -18, 60, 48};
            format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
            mediaCodec = MediaCodec.createDecoderByType(MIMETYPE_VIDEO_AVC);
            mediaCodec.configure(format, surface, null, 0);
            mediaCodec.start();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onFrame(byte[] buf, int offset, int length) {
        int value = buf[4] & 0x0f;
        if (!tryCodecSuccess(value)) {
            return;
        }
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(20);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
            if (value == 7 || value == 8) {
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, 0, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
            } else if (value == 5) {
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, 0, MediaCodec.BUFFER_FLAG_KEY_FRAME);
            } else {
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100);
        while (outputBufferIndex >= 0) {
            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100);
        }
    }

    @Override
    protected void releaseDecoder() {
        try {
            if (null != mediaCodec) {
                mediaCodec.stop();
                mediaCodec.release();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void updateRotate(int videoRotate) {

    }
}
