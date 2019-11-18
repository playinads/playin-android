//
// Created by zhangliucheng on 2019-09-09.
//

#ifndef PLAYINDECODER_FFMPEG_H
#define PLAYINDECODER_FFMPEG_H

#include <android/native_window_jni.h>
#include <android/native_window.h>

extern "C" {
#include "ffmpeg/include/libavcodec/avcodec.h"
#include "ffmpeg/include/libavformat/avformat.h"
#include "ffmpeg/include/libswscale/swscale.h"
#include "ffmpeg/include/libavutil/avutil.h"
#include "ffmpeg/include/libavutil/frame.h"
#include "ffmpeg/include/libavutil/imgutils.h"
}

class FFmpeg {

public:
    FFmpeg();
    ~FFmpeg();
    int init(JNIEnv *env, jobject instance, jint width, jint height, jint rotate, jobject surface);
    int decoding(JNIEnv *env, jobject instance, jbyteArray data);
    void close();
    void updateSurface(JNIEnv *env, jobject surface);
    void updateRotate(JNIEnv *env, jint rotate);

private:
    ANativeWindow *nativeWindow = NULL;
    ANativeWindow_Buffer windowBuffer;
    AVCodecContext *pCodecCtx = NULL;
    AVCodec *pCodec;
    jobject surface;
    int rotate, len, got_frame;

    void resetNativeWindow(JNIEnv *env);
    AVFrame* processYuv(AVFrame *yuvFrame);
    AVFrame* mallocRGBFrame(int width, int height);
    void renderWindow(AVFrame *destFrame);
};


#endif //PLAYINDECODER_FFMPEG_H
