//
// Created by zhangliucheng on 2019-08-21.
//

#include <jni.h>
#include "FFmpeg.h"
#include "PILog.h"


void saveFFmpeg(JNIEnv *env, jobject instance, FFmpeg *ffmpeg) {
    jclass clazz = env->GetObjectClass(instance);
    jfieldID field = env->GetFieldID(clazz, "ffmpegHandle", "J");
    jlong handle = reinterpret_cast<jlong>(ffmpeg);
    env->SetLongField(instance, field, handle);
}

FFmpeg* getFFmpeg(JNIEnv *env, jobject instance) {
    jclass clazz = env->GetObjectClass(instance);
    jfieldID field = env->GetFieldID(clazz, "ffmpegHandle", "J");
    jlong  handle = env->GetLongField(instance, field);
    FFmpeg *ffmpeg = reinterpret_cast<FFmpeg *>(handle);
    return ffmpeg;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tech_playinsdk_decoder_FFmpegDecoder_ffmpegInit(JNIEnv *env, jobject instance, jint width,
                                                         jint height, jint rotate, jobject surface) {
    FFmpeg *ffmpeg = getFFmpeg(env, instance);
    if (NULL == ffmpeg) {
        ffmpeg = new FFmpeg();
        saveFFmpeg(env, instance, ffmpeg);
        return ffmpeg->init(env, instance, width, height, rotate, surface);
    } else {
        ffmpeg->updateSurface(env, surface);
        return 1;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tech_playinsdk_decoder_FFmpegDecoder_ffmpegDecoding(JNIEnv *env, jobject instance, jbyteArray data) {
    FFmpeg *ffmpeg = getFFmpeg(env, instance);
    if (NULL != ffmpeg) {
        return ffmpeg->decoding(env, instance, data);
    }
    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tech_playinsdk_decoder_FFmpegDecoder_ffmpegClose(JNIEnv *env, jobject instance) {
    FFmpeg *ffmpeg = getFFmpeg(env, instance);
    if (NULL != ffmpeg) {
        ffmpeg->close();
        delete ffmpeg;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tech_playinsdk_decoder_FFmpegDecoder_ffmpegUpdateRotate(JNIEnv *env, jobject instance,
                                                                 jint rotate) {
    FFmpeg *ffmpeg = getFFmpeg(env, instance);
    if (NULL != ffmpeg) {
        ffmpeg->updateRotate(env, rotate);
    }
}