//
// Created by zhangliucheng on 2019-09-09.
//

#pragma once

#ifndef PLAYINDECODER_ANDROIDLOG_H
#define PLAYINDECODER_ANDROIDLOG_H

#include <android/log.h>

#define LOG_TAG "PLAYIN_NATIVE"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


#endif //PLAYINDECODER_ANDROIDLOG_H
