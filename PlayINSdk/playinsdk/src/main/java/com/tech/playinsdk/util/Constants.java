package com.tech.playinsdk.util;

public class Constants {

    public static final String HOST = "https://playinads.com";

    public static final String OS_TYPE = "2";
    public static final String VERSION = "1.0.0";

    public final static class Report {
        public final static String DOWN_LOAD = "AppStore";
        public final static String END_PLAY = "endplay";
    }


    public final static class PacketType {
        public final static byte CONTROL = 1;
        public final static byte STREAM = 2;
    }

    public final static class StreamType {
        public final static byte TOUCH = 0;
        public final static byte H264 = 1;
        public final static byte AAC = 2;
        public final static byte PCM = 3;
        public final static byte ANDROID_VIDEO_START = 6;
    }
}
