package com.tech.playinsdk.connect;

import com.tech.playinsdk.util.Constants;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Process sending and receiving data.
 */
public class DataProcess {

    public interface ReceiveCallback {
        void message(String message);
        void buffer(byte[] buf);
    }

    private ArrayBlockingQueue<byte[]> sendQueue;
    public ArrayBlockingQueue<byte[]> getSendQueue() {
        return sendQueue;
    }

    public DataProcess() {
        sendQueue = new ArrayBlockingQueue<>(20);
    }


    public void receiveData(InputStream is, ReceiveCallback callback) throws IOException {
        DataInputStream dis = new DataInputStream(is);

        int type;
        byte[] msgLengthBuf = new byte[4];
        byte[] packetBuf = new byte[4];
        byte[] msgIdBuf = new byte[2];

        while (!Thread.interrupted()) {
            type = dis.read();
            dis.readFully(msgLengthBuf);
            int msgLength = bytesToInt(msgLengthBuf);
            if (type == 1) {
                dis.readFully(packetBuf);
                dis.readFully(msgIdBuf);
                byte[] contentBuf = new byte[msgLength - packetBuf.length - msgIdBuf.length];
                dis.readFully(contentBuf);
                callback.message(new String(contentBuf));

            } else if (type == 2) {
                int streamType = dis.read();
                byte[] streamBuf = new byte[msgLength - 1];
                dis.readFully(streamBuf);
                if (streamType == 1 || streamType == 6) {
                    callback.buffer(streamBuf);
                }
            }
        }
    }

    // type(1字节) | 消息长度(4字节) | packetBuf(4字节) | 消息id(2字节) | 内容(n字节)
    public void sendText(String msg) {
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
        sendQueue.offer(sendBytes);
    }

    // type(1字节) | 消息长度(4字节) | streamType(1字节) | 内容(n字节)
    public void sendControl(String control) {
        sendStream(Constants.PacketType.STREAM, Constants.StreamType.TOUCH, control);
    }

    // packType(1字节) | 消息长度(4字节) | streamType(1字节) | 内容(n字节)
    public void sendStream(byte packType, byte streamType, String control) {
        byte[] contentBuf = control.getBytes();
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
        sendQueue.offer(sendBytes);
    }

    private byte[] intToBytes(int num){
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(num).array();
    }

    private int bytesToInt(byte[] bytes){
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
