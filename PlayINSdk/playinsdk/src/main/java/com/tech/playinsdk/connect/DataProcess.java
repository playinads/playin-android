package com.tech.playinsdk.connect;

import com.tech.playinsdk.util.Constants;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Process sending and receiving data.
 */
public class DataProcess {

    public interface ReceiveCallback {
        void message(String message);
        void buffer(int streamType, byte[] timeBuf, byte[] streamBuf);
    }

    private int type;
    private byte[] msgLengthBuf = new byte[4];
    private byte[] packetBuf = new byte[4];
    private byte[] msgIdBuf = new byte[2];
    private byte[] timeBuf = new byte[4];

    public void receiveData(InputStream is, ReceiveCallback callback) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        while (!Thread.interrupted()) {
            type = dis.read();
            dis.readFully(msgLengthBuf);
            int msgLength = bytesToInt(msgLengthBuf);
            if (type == Constants.PacketType.CONTROL) {
                processRecvText(dis, msgLength, callback);
            } else if (type == Constants.PacketType.STREAM) {
                processRecvStream(dis, msgLength, callback);
            }
        }
    }

    // 处理接受的字符串
    private void processRecvText(DataInputStream dis, int msgLength, ReceiveCallback callback) throws IOException {
        dis.readFully(packetBuf);
        dis.readFully(msgIdBuf);
        byte[] contentBuf = new byte[msgLength - packetBuf.length - msgIdBuf.length];
        dis.readFully(contentBuf);
        callback.message(new String(contentBuf));
    }

    // 处理接受的Stream流
    private void processRecvStream(DataInputStream dis, int msgLength, ReceiveCallback callback) throws IOException {
        int streamType = dis.read();
        if (streamType == Constants.StreamType.REPORT_RECEIVE) {
            // 带有4个字节的时间戳的视频流
            dis.readFully(timeBuf);
            byte[] streamBuf = new byte[msgLength - 1 - timeBuf.length];
            dis.readFully(streamBuf);
            callback.buffer(Constants.StreamType.H264, timeBuf, streamBuf);
        } else {
            // 不带时间戳的数据流
            byte[] streamBuf = new byte[msgLength - 1];
            dis.readFully(streamBuf);
            callback.buffer(streamType, null, streamBuf);
        }
    }

    // type(1字节) | 消息长度(4字节) | packetBuf(4字节) | 消息id(2字节) | 内容(n字节)
    public byte[] getSendText(String msg) {
        byte[] packetBuf = {0, 0, 0, 0};
        byte[] msgIdBuf = {2, 1};
        byte[] contentBuf = msg.getBytes();
        byte[] msgLenBuf = intToBytes(packetBuf.length + msgIdBuf.length + contentBuf.length);
        byte[] sendBytes = new byte[1 + msgLenBuf.length + packetBuf.length + msgIdBuf.length + contentBuf.length];

        // 消息类型 (1字节)
        sendBytes[0] = 1;
        // 消息长度 (4字节)
        System.arraycopy(msgLenBuf, 0, sendBytes, 1, msgLenBuf.length);
        // packetBuf (4字节)
        System.arraycopy(packetBuf, 0, sendBytes, 5, packetBuf.length);
        // 消息Id (2字节)
        System.arraycopy(msgIdBuf, 0, sendBytes, 9, msgIdBuf.length);
        // 内容
        System.arraycopy(contentBuf, 0, sendBytes, 11, contentBuf.length);
        return sendBytes;
    }

    // packType(1字节) | 消息长度(4字节) | streamType(1字节) | 内容(n字节)
    public byte[] getSendStream(byte packType, byte streamType, byte[] contentBuf) {
        byte[] msgLenBuf = intToBytes(1 + contentBuf.length);
        byte[] sendBytes = new byte[1 + msgLenBuf.length + 1 + contentBuf.length];

        // 消息类型 (1字节)
        sendBytes[0] = packType;
        // 消息长度 (4字节)
        System.arraycopy(msgLenBuf, 0, sendBytes, 1, msgLenBuf.length);
        // streamType (1字节)
        sendBytes[5] = streamType;
        // 内容
        System.arraycopy(contentBuf, 0, sendBytes, 6, contentBuf.length);
        return sendBytes;
    }

    private byte[] intToBytes(int num){
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(num).array();
    }

    private int bytesToInt(byte[] bytes){
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
