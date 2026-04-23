#include <jni.h>
#include <sys/system_properties.h>
#include "hidden_api.h"
#include "Log.h"
#include "SandHook/ElfImg.h"

bool disable_hidden_api(JNIEnv *env) {
    char version_str[PROP_VALUE_MAX];
    if (!__system_property_get("ro.build.version.sdk", version_str)) {
        ALOGE("Failed to obtain SDK int");
        return JNI_ERR;
    }
    long android_version = std::strtol(version_str, nullptr, 10);

    // Hidden api introduced in sdk 29
    if (android_version < 29) {
        return true;
    }

    SandHook::ElfImg *elf_img = new SandHook::ElfImg("libart.so");

    void *addr = nullptr;
    
    // Different symbol names for different Android versions
    if (android_version <= 30) {
        // Android 10-11 (API 29-30)
        addr = (void*)elf_img->getSymbAddress("_ZN3artL32VMRuntime_setHiddenApiExemptionsEP7_JNIEnvP7_jclassP13_jobjectArray");
    } else {
        // Android 12+ (API 31+)
        // Try the new symbol name first
        addr = (void*)elf_img->getSymbAddress("_ZN3art9VMRuntime28setHiddenApiExemptionsEP7_JNIEnvP7_jclassP13_jobjectArray");
        
        // Fallback for Android 12/13
        if (!addr) {
            addr = (void*)elf_img->getSymbAddress("_ZN3art3_VM28setHiddenApiExemptionsEP7_JNIEnvP7_jclassP13_jobjectArray");
        }
        
        // Another fallback for some Android 12 variants
        if (!addr) {
            addr = (void*)elf_img->getSymbAddress("_ZN3artL28setHiddenApiExemptionsEP7_JNIEnvP7_jclassP13_jobjectArray");
        }
    }
    
    delete elf_img;
    
    if (!addr) {
        ALOGE("HiddenAPI: Didn't find setHiddenApiExemptions for API %ld", android_version);
        return false;
    }

    jclass stringClass = env->FindClass("java/lang/String");
    if (stringClass == nullptr) {
        ALOGE("HiddenAPI: Failed to find String class");
        return false;
    }
    
    jstring wildcard = env->NewStringUTF("L");
    if (wildcard == nullptr) {
        ALOGE("HiddenAPI: Failed to create wildcard string");
        return false;
    }
    
    jobjectArray args = env->NewObjectArray(1, stringClass, wildcard);
    if (args == nullptr) {
        ALOGE("HiddenAPI: Failed to create args array");
        return false;
    }

    auto func = reinterpret_cast<void (*)(JNIEnv *, jclass, jobjectArray)>(addr);
    func(env, stringClass, args);
    
    ALOGI("HiddenAPI: Successfully disabled hidden API checks for API %ld", android_version);
    return true;
}