#ifndef SPEED_LOG_H
#define SPEED_LOG_H

#include <android/log.h>

#define TAG "NativeCore"

// Enable or disable logging by setting this macro (0 = OFF, 1 = ON)
#define ENABLE_NATIVE_LOG 1

#if ENABLE_NATIVE_LOG
    #define log_print_error(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
    #define log_print_debug(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#else
    #define log_print_error(...)
    #define log_print_debug(...)
#endif

#define ALOGE(...) log_print_error(__VA_ARGS__)
#define ALOGD(...) log_print_debug(__VA_ARGS__)

#endif // SPEED_LOG_H
