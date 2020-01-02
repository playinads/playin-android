package com.tech.playinsdk.connect;

import static com.tech.playinsdk.util.Constants.StreamType.*;
import static com.tech.playinsdk.util.Constants.PacketType.*;
import com.tech.playinsdk.util.Analyze;
import com.tech.playinsdk.util.Common;
import com.tech.playinsdk.util.Constants;
import com.tech.playinsdk.util.PlayLog;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Establish a connection.
 */
public abstract class PlaySocket extends Thread {

    public abstract void onOpen();
    public abstract void onMessage(String msg);
    public abstract void onMessage(int streamType, byte[] buf);
    public abstract void onError(Exception ex);

    private String ip;
    private int port;
    private DataProcess dataProcess;
    private ArrayBlockingQueue<byte[]> sendQueue;

    private Socket socket;
    private InputStream istream;
    private OutputStream ostream;
    private Thread mWriteThread;

    public PlaySocket(String ip, int port) {
        this.ip = ip;
        this.port = port;
        dataProcess = new DataProcess();
        sendQueue = new ArrayBlockingQueue<>(200);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void connect() {
        start();
    }

    public void disConnect() {
        try {
            sendQueue.clear();
            interrupt();
            if (null != socket) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
        } catch (Exception e) { }
        if (null != mWriteThread) {
            mWriteThread.interrupt();
        }
        Common.closeStream(istream, ostream);
    }

    public void sendControl(String control) {
        byte[] buf = dataProcess.getSendStream(STREAM, TOUCH, control.getBytes());
        offerMessage(buf);
    }

    public void sendVideoQuality(int quality) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("video_quality", quality);
            byte[] buf = dataProcess.getSendStream(STREAM, PARAMS, obj.toString().getBytes());
            offerMessage(buf);
        } catch (Exception ex) {
            PlayLog.e("PlaySocket changeVideoQuality  exception :" + ex);
        }
    }

    public void sendUserInfo(String token) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", token);
            obj.put("device_name", android.os.Build.BRAND);
            obj.put("os_type", Constants.OS_TYPE);
            obj.put("coder", "annex-b");  // 安卓annex-b, 苹果avcc
            byte[] buf = dataProcess.getSendText(obj.toString());
            offerMessage(buf);
        } catch (Exception ex) {
            PlayLog.e("sendUserContect  exception :" + ex);
        }
    }

    public void sendMessageToAndroid() {
        byte[] buf = dataProcess.getSendStream(STREAM, ANDROID_VIDEO_START, new byte[0]);
        offerMessage(buf);
    }

    public void sendReportTime(byte[] timeBuf) {
        byte[] buf = dataProcess.getSendStream(STREAM, REPORT_SEND, timeBuf);
        offerMessage(buf, false);
    }

    private void offerMessage(byte[] sendBytes) {
        this.offerMessage(sendBytes, true);
    }

    private void offerMessage(byte[] sendBytes, boolean analyze) {
        boolean result = sendQueue.offer(sendBytes);
        if (true) Analyze.getInstance().sendResult(result);
    }

    @Override
    public void run() {
        PlayLog.v("PlaySocket begin to connect  ip: " + ip + " port:" + port);
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 5000);
            socket.setSoTimeout(0);
            socket.setReceiveBufferSize(1024 * 1024);
            socket.setSendBufferSize(32 * 1024);
            socket.setTcpNoDelay(true);
            PlayLog.v("PlaySocket connect successed");
            onOpen();
            istream = socket.getInputStream();
            ostream = socket.getOutputStream();
            // 启动发送数据线程
            mWriteThread = new WriteThread();
            mWriteThread.start();
            // 读取数据流(循环阻塞)
            readData();
        } catch (SocketException ex) {
            PlayLog.e("PlaySocket connect error： " + ex);
            onError(ex);
        } catch (IOException ex) {
            onError(ex);
        }
    }

    private void readData() {
        try {
            dataProcess.receiveData(istream, new DataProcess.ReceiveCallback() {
                @Override
                public void message(String message) {
                    onMessage(message);
                }

                @Override
                public void buffer(int streamType, byte[] timeBuf, byte[] streamBuf) {
                    if (null != timeBuf) {
                        sendReportTime(timeBuf);
                    }
                    onMessage(streamType, streamBuf);
                }
            });
        } catch (IOException ex) {
            onError(ex);
        }
    }

    public class WriteThread extends Thread {
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    byte[] sendBuf = sendQueue.take();
                    ostream.write(sendBuf);
                    ostream.flush();
                }
            } catch (InterruptedException e) {
            } catch (IOException e) {
                PlayLog.e("PlaySocket send error： " + e);
                onError(e);
            }
        }
    }
}
