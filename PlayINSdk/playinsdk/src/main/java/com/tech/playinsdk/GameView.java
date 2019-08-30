package com.tech.playinsdk;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.tech.playinsdk.connect.PlaySocket;
import com.tech.playinsdk.decoder.BaseDecoder;
import com.tech.playinsdk.decoder.FFmpegDecoder;
import com.tech.playinsdk.model.entity.PlayInfo;
import com.tech.playinsdk.util.Constants;
import com.tech.playinsdk.util.PILog;
import org.json.JSONException;
import org.json.JSONObject;


class GameView extends SurfaceView implements SurfaceHolder.Callback, BaseDecoder.DecoderListener {

    public interface GameListener {
        void onGameStart();
        void onGameError(Exception ex);
    }

    private final StringBuilder controlBuilder = new StringBuilder();

    private BaseDecoder decoder;

    private GameListener playListener;
    private PlayInfo playInfo;
    private PlaySocket playSocket;
    private int visibility;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);
    }

    public void startConnect(PlayInfo playInfo, GameListener listener) {
        this.playInfo = playInfo;
        this.playListener = listener;
        playSocket = new MyPlaySocket(playInfo.getServerIp(), playInfo.getServerPort());
        playSocket.connect();

        decoder = new FFmpegDecoder(playInfo.getDeviceWidth(), playInfo.getDeviceHeight());
//        decoder = new MediaDecoder(playInfo.getDeviceWidth(), playInfo.getDeviceHeight());
        decoder.setDecoderListener(this);
        decoder.start(getHolder().getSurface());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.playListener = null;
        if (null != playSocket) {
            playSocket.disConnect();
        }

        if (null != decoder) {
            decoder.stop();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.visibility = visibility;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null != decoder) {
            decoder.setDisplayHolder(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        if (null != decoder) {
//            decoder.stop();
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rateWidth = event.getX() / getWidth();
        float rateHeight = event.getY() / getHeight();
        int action = event.getAction();  // 0 down, 1 up, 2 move
        // 目标触摸 0-down,1-move,2-up
        if (action == 1) {
            action = 2;
        } else if (action == 2) {
            action = 1;
        }
        controlBuilder.delete(0, controlBuilder.length());
        controlBuilder.append(rateWidth).append("_")
                .append(rateHeight).append("_")
                .append(action)
                .append("_0_0");
        sendControl(event.getPointerCount(), controlBuilder.toString());
        return true;
    }

    private void sendControl(int finger, String control) {
        if (null != playSocket && playSocket.isConnected()) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("" + finger, control);
                playSocket.sendControl(obj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendMessageToAndroid() {
        playSocket.sendStream(Constants.PacketType.STREAM, Constants.StreamType.ANDROID_VIDEO_START, "");
    }


    private void sendUserContect() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", playInfo.getToken());
            obj.put("device_name", android.os.Build.BRAND);
            obj.put("os_type", Constants.OS_TYPE);
            obj.put("coder", "annex-b");  // 安卓annex-b, 苹果avcc
            playSocket.sendText(obj.toString());
        } catch (Exception ex) {
            PILog.e("sendUserContect  exception :" + ex);
        }
    }

    private class MyPlaySocket extends PlaySocket {

        public MyPlaySocket(String ip, int port) {
            super(ip, port);
        }

        @Override
        public void onOpen() {
            PILog.v("MyPlaySocket --> onMessage  onOpen ");
            sendUserContect();
//            if (playInfo.getOsType() == 2) {
                sendMessageToAndroid();
//            }
        }

        @Override
        public void onMessage(String msg) {
            PILog.v("MyPlaySocket --> onMessage  msg " + msg);
            try {
                JSONObject object = new JSONObject(msg);
                if (0 != object.optInt("code") && null != playListener) {
                    playListener.onGameError(new Exception(object.optString("onPlayError")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(byte[] buf) {
            if (GameView.this.visibility == 0) {
                decoder.sendVideoData(buf);
            }
        }

        @Override
        public void onError(Exception ex) {
            PILog.v("MyPlaySocket --> onError  " + ex);
            if (null != playListener) {
                playListener.onGameError(ex);
            }
        }
    }

    @Override
    public void decoderSuccess() {
        playListener.onGameStart();
    }
}
