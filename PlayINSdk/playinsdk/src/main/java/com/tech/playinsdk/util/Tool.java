package com.tech.playinsdk.util;

import java.io.Closeable;
import java.io.IOException;

public class Tool {

    public static void closeStream(Closeable... closeables) {
        for (Closeable stream: closeables) {
            if (null != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
