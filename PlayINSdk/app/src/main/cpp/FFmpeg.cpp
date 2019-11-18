//
// Created by zhangliucheng on 2019-09-09.
//

#include "FFmpeg.h"
#include "PILog.h"

extern "C" {
#include "yuv/include/libyuv/convert_argb.h"
#include "yuv/include/libyuv.h"
}

FFmpeg::FFmpeg() {
};


FFmpeg::~FFmpeg() {
}

int FFmpeg::init(JNIEnv *env, jobject instance, jint width, jint height, jint rotate, jobject surface) {
    this->rotate = rotate;
    this->surface = surface;

    av_register_all();
    pCodec = avcodec_find_decoder(AV_CODEC_ID_H264);
    if (pCodec == NULL) {
        LOGE("%s","无法解码");
        return -1;
    }
    pCodecCtx = avcodec_alloc_context3(pCodec);
    pCodecCtx->frame_number = 1;
    pCodecCtx->width = width;
    pCodecCtx->height = height;
    pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
    int ret = avcodec_open2(pCodecCtx, pCodec, NULL);
    if (ret < 0) {
        LOGE("%s","打开解码器失败  " + ret);
        return -1;
    }
    resetNativeWindow(env);
    return 0;
}

int FFmpeg::decoding(JNIEnv *env, jobject instance, jbyteArray data) {
    jsize len = env->GetArrayLength(data);
    jbyte *jbarray = env->GetByteArrayElements(data, 0);
    AVPacket packet;
    av_new_packet(&packet, len);
    memcpy(packet.data, jbarray, len);

    AVFrame *yuvFrame = av_frame_alloc();
    avcodec_decode_video2(pCodecCtx, yuvFrame, &got_frame, &packet);

    if (got_frame && nativeWindow != NULL) {
        ANativeWindow_lock(nativeWindow, &windowBuffer, NULL);
        // yuv转rgb，并且旋转
        AVFrame *destFrame = processYuv(yuvFrame);
        // 渲染数据
        renderWindow(destFrame);
        ANativeWindow_unlockAndPost(nativeWindow);
        av_free(destFrame->data[0]);
        av_free(destFrame);
    }
    av_free(yuvFrame);
    av_free_packet(&packet);
    env->ReleaseByteArrayElements(data, jbarray, 0);
    return got_frame;
}

void FFmpeg::close() {
    if (NULL != pCodecCtx) {
        avcodec_close(pCodecCtx);
        avcodec_free_context(&pCodecCtx);
    }

    if (NULL != nativeWindow) {
        ANativeWindow_release(nativeWindow);
    }
}

void FFmpeg::updateSurface(JNIEnv *env, jobject surface) {
    this->surface = surface;
    resetNativeWindow(env);
}

void FFmpeg::updateRotate(JNIEnv *env, jint rotate) {
    LOGE("updateRotate  ----------  %d", rotate);
    if (this->rotate == rotate) {
        return;
    }
    this->rotate = rotate;
    resetNativeWindow(env);
}


void FFmpeg::resetNativeWindow(JNIEnv *env) {
    int width = pCodecCtx->width;
    int height = pCodecCtx->height;
    if (rotate % 180 != 0) {
        width = pCodecCtx->height;
        height = pCodecCtx->width;
    }
    ANativeWindow *nw = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_setBuffersGeometry(nw, width, height, WINDOW_FORMAT_RGBA_8888);
    nativeWindow = nw;
}

AVFrame* FFmpeg::processYuv(AVFrame *yuvFrame) {
    int videoWidth = pCodecCtx->width;
    int videoHeight = pCodecCtx->height;

    AVFrame *rgbFrame = mallocRGBFrame(videoWidth, videoHeight);
    libyuv::I420ToARGB(yuvFrame->data[0], yuvFrame->linesize[0],
                       yuvFrame->data[2], yuvFrame->linesize[2],
                       yuvFrame->data[1], yuvFrame->linesize[1],
                       rgbFrame->data[0], rgbFrame->linesize[0],
                       videoWidth, videoHeight);

    AVFrame *destFrame = NULL;
    if (rotate % 180 == 0) {
        destFrame = mallocRGBFrame(videoWidth, videoHeight);
    } else {
        // 调换宽和高
        destFrame = mallocRGBFrame(videoHeight, videoWidth);
    }
    libyuv::ARGBRotate(rgbFrame->data[0], rgbFrame->linesize[0], destFrame->data[0], destFrame->linesize[0],
                       videoWidth, videoHeight, (libyuv::RotationMode)rotate);
    av_free(rgbFrame->data[0]);
    av_free(rgbFrame);
    return destFrame;
}

AVFrame* FFmpeg::mallocRGBFrame(int width, int height) {
    AVFrame *rgbFrame = av_frame_alloc();
    uint8_t *rgbBuffer = (uint8_t *) av_malloc(av_image_get_buffer_size(AV_PIX_FMT_RGBA, width, height, 1));
    av_image_fill_arrays(rgbFrame->data, rgbFrame->linesize, rgbBuffer, AV_PIX_FMT_RGBA, width, height, 1);
    return rgbFrame;
}

void FFmpeg::renderWindow(AVFrame *destFrame) {
    int videoWidth = pCodecCtx->width;
    int videoHeight = pCodecCtx->height;

    uint8_t *dst = (uint8_t *) windowBuffer.bits;
    int dstStride = windowBuffer.stride * 4;
    uint8_t *src = destFrame->data[0];
    int srcStride = destFrame->linesize[0];

    if (rotate % 180 == 0) {
        int h;
        for (h = 0; h < videoHeight; h++) {
            memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
        }
    } else {
        int h;
        for (h = 0; h < videoWidth; h++) {
            memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
        }
    }
}