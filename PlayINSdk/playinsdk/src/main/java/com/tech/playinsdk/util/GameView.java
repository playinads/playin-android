package com.tech.playinsdk.util;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tech.playinsdk.connect.PlaySocket;
import com.tech.playinsdk.decoder.AudioDecoder;
import com.tech.playinsdk.decoder.FFmpegDecoder;
import com.tech.playinsdk.decoder.VideoDecoder;
import com.tech.playinsdk.http.HttpException;
import com.tech.playinsdk.model.entity.PlayInfo;

import org.json.JSONObject;
import java.io.IOException;
import java.net.SocketException;


public class GameView extends SurfaceView implements SurfaceHolder.Callback, VideoDecoder.DecoderListener {

    public interface GameListener {
        void onGameStart();
        void onGameError(Exception ex);
    }

    private final Handler handler = new Handler();
    private VideoDecoder videodecoder;
    private AudioDecoder audioDecoder;

    private GameListener playListener;
    private PlayInfo playInfo;
    private PlaySocket playSocket;
    private int retryCount;
    private int visibility;
    private boolean audioOn = true;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setAudioState(boolean on) {
        this.audioOn = on;
    }

    private void init() {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);
    }

    public void startConnect(PlayInfo playInfo, GameListener listener) {
        this.playInfo = playInfo;
        this.playListener = listener;
        initAudioDecoder();
        initVideoDecoder();
        connectSocket();
    }

    private void connectSocket() {
        if (null != playSocket) {
            playSocket.disConnect();
        }
        playSocket = new MyPlaySocket(playInfo.getServerIp(), playInfo.getServerPort());
        playSocket.connect();
    }

    public void disconnect() {
        try {
            if (null != playSocket) playSocket.disConnect();
            if (null != videodecoder) videodecoder.stop();
            if (null != audioDecoder) audioDecoder.stop();
        } catch (Exception ex) {
            PlayLog.e("GameView disconnect  exception :" + ex);
        }
    }

    private void initAudioDecoder() {
        audioDecoder = new AudioDecoder();
        audioDecoder.start();
    }

    private void initVideoDecoder() {
        int width = playInfo.getDeviceWidth();
        int height = playInfo.getDeviceHeight();
        int rotate = 0;
        // 横屏
        if (playInfo.getOrientation() == 1) {
            if (playInfo.getOsType() == 1) {
                // ios 设备端
                rotate = 270;
            } else if (playInfo.getOsType() == 2) {
                // android 设备端
                width = playInfo.getDeviceHeight();
                height = playInfo.getDeviceWidth();
            }
        }
        videodecoder = new FFmpegDecoder(width, height, rotate);
//        videodecoder = new MediaDecoder(width, height, rotate);
        videodecoder.setDecoderListener(this);
        videodecoder.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.playListener = null;
        if (null != playSocket) {
            playSocket.disConnect();
        }
        if (null != audioDecoder) {
            audioDecoder.stop();
        }
        if (null != videodecoder) {
            videodecoder.stop();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.visibility = visibility;
        if (null != videodecoder) {
            if (visibility == 0) {
                videodecoder.resume();
            } else {
                videodecoder.pause();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null != videodecoder) {
            videodecoder.setDisplayHolder(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        String conStr = TouchUtil.processTouchEvent(event, getWidth(), getHeight(), playInfo);
        if (null != conStr) {
            playSocket.sendControl(conStr);
        }
        return true;
    }

    public void changeVideoQuality(int quality) {
        PlayLog.e("changeVideoQuality  " + quality);
        playSocket.sendVideoQuality(quality);
    }

    private class MyPlaySocket extends PlaySocket {

        public MyPlaySocket(String ip, int port) {
            super(ip, port);
        }

        @Override
        public void onOpen() {
            PlayLog.v("MyPlaySocket --> onMessage  onOpen ");
            sendUserInfo(playInfo.getToken());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        sendMessageToAndroid();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onMessage(String msg) {
            PlayLog.v("MyPlaySocket --> onMessage  msg " + msg);
            try {
                JSONObject object = new JSONObject(msg);
                int code = object.optInt("code");
                if (0 != code) {
                    // 安卓设备，code=18继续重连
                    if (isAttachedToWindow() && playInfo.getOsType() == 2 && retryCount < 6 && code == -18) {
                        retryCount++;
                        Thread.sleep(1600);
                        connectSocket();
                    } else {
                        invokeGameError(new HttpException(code, object.optString("error")));
                    }
                } else {
                    parserAudioConfig(object);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(int streamType, byte[] buf) {
//            PlayLog.e("MyPlaySocket --> onMessage  " + streamType + " ====  " + buf.length);
            if (GameView.this.visibility == 0) {
                if (streamType == Constants.StreamType.H264) {
                    if (null != videodecoder) videodecoder.sendVideoData(buf);
                } else if (streamType == Constants.StreamType.PCM) {
                    if (null != audioDecoder && audioOn) audioDecoder.sendAudioData(buf);
                }
            }
        }

        @Override
        public void onError(Exception ex) {
            PlayLog.v("MyPlaySocket --> onError  " + ex);
            String msg = "Unknown exception";
            if (ex instanceof IOException) {
                msg = "Stream terminal";
            } else if (ex instanceof SocketException) {
                msg = "Socket connect exception";
            }
            invokeGameError(new Exception(msg));
        }
    }

    @Override
    public void decoderSuccess() {
        invokeGameStart();
    }

    private void parserAudioConfig(JSONObject object) {
        int sampleRateInHz, channelConfig, audioFormat;

        if (playInfo.getOsType() == 1) {
            // ios 默认音频参数
            sampleRateInHz = 22050;
            channelConfig = AudioFormat.CHANNEL_IN_MONO;
            audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        } else {
            // android 默认音频参数
            sampleRateInHz = 48000;
            channelConfig = AudioFormat.CHANNEL_IN_STEREO;
            audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        }
        // 如果流里面有音频参数，以流里面为准
        JSONObject streamInfo = object.optJSONObject("stream_info");
        if (null != streamInfo) {
            sampleRateInHz = streamInfo.optInt("sample_rate");
            channelConfig = streamInfo.optInt("channels");
            int bps = streamInfo.optInt("bits_per_sample");
            if (bps == 16) audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            else if (bps == 8) audioFormat = AudioFormat.ENCODING_PCM_8BIT;
        }
        audioDecoder.initAudioTrack(sampleRateInHz, channelConfig, audioFormat);
    }

    private void invokeGameStart() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (null != playListener) {
                    playListener.onGameStart();
                }
            }
        });
    }

    private void invokeGameError(final Exception ex) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (null != playListener) {
                    playListener.onGameError(ex);
                }
            }
        });
    }
}
