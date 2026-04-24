//
// Created by Milk on 4/9/21.
//

#ifndef VIRTUALM_VMCORE_H
#define VIRTUALM_VMCORE_H

#include <jni.h>
#include <unistd.h>
//#include <fb/include/fb/ALog.h>
//#include <fb/include/fb/fbjni.h>
#define VMCORE_CLASS "com/darkbox/core/VNative"

class BoxCore {
public:
    static JavaVM *getJavaVM();
    static int getApiLevel();
    static int getCallingUid(JNIEnv *env, int orig);
    static jstring redirectPathString(JNIEnv *env, jstring path);
    static jobject redirectPathFile(JNIEnv *env, jobject path);
    static jlongArray loadEmptyDex(JNIEnv *env);
};

#endif //VIRTUALM_VMCORE_H