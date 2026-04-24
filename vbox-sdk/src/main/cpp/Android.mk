LOCAL_PATH := $(call my-dir)
MAIN_LOCAL_PATH := $(LOCAL_PATH)

# ========== Main Shared Library (blackbox) ==========
include $(CLEAR_VARS)

# --- Module Name ---
LOCAL_MODULE := darkbox

# -------- C FLAGS (SAFE) --------
LOCAL_CFLAGS := \
-O3 \
-Wno-error=format-security \
-fvisibility=hidden \
-ffunction-sections \
-fdata-sections

# -------- CPP FLAGS (NO CODE REMOVED) --------
LOCAL_CPPFLAGS := \
-std=c++17 \
-Wno-error=format-security \
-Wno-error=c++11-narrowing \
-fvisibility=hidden \
-ffunction-sections \
-fdata-sections \
-fexceptions \
-fno-rtti \
-fno-unwind-tables \
-fno-asynchronous-unwind-tables

# -------- LINKER FLAGS --------
LOCAL_LDFLAGS := \
-Wl,--gc-sections \
-Wl,--strip-all

LOCAL_LDLIBS := -llog -landroid -lz
LOCAL_ARM_MODE := arm

# --- Include Paths ---
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Hook
LOCAL_C_INCLUDES += $(LOCAL_PATH)/IO
LOCAL_C_INCLUDES += $(LOCAL_PATH)/JniHook
LOCAL_C_INCLUDES += $(LOCAL_PATH)/SandHook

#LOCAL_C_INCLUDES += $(LOCAL_PATH)/And64InlineHook
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/KittyMemory
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/Substrate
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/sqlite

# --- Source Files ---
VBOX_SRC := $(wildcard $(LOCAL_PATH)/*.cpp)
VBOX_SRC += $(wildcard $(LOCAL_PATH)/Hook/*.cpp)
VBOX_SRC += $(wildcard $(LOCAL_PATH)/IO/*.cpp)
VBOX_SRC += $(wildcard $(LOCAL_PATH)/JniHook/*.cpp)
VBOX_SRC += $(wildcard $(LOCAL_PATH)/SandHook/*.cpp)

#VBOX_SRC += $(wildcard $(LOCAL_PATH)/And64InlineHook/*.cpp)
#VBOX_SRC += $(wildcard $(LOCAL_PATH)/KittyMemory/*.cpp)
#VBOX_SRC += $(wildcard $(LOCAL_PATH)/Substrate/*.cpp)
#VBOX_SRC += $(wildcard $(LOCAL_PATH)/sqlite/*.cpp)

# Add rootkiller source files
#VBOX_SRC += $(LOCAL_PATH)/sqlite/sqlite3.c
#VBOX_SRC += $(LOCAL_PATH)/SandHook/HideHook.c

LOCAL_SRC_FILES := $(VBOX_SRC:$(LOCAL_PATH)/%=%)

# --- Static Libraries ---
#LOCAL_STATIC_LIBRARIES := 

# --- Build Shared Library ---
include $(BUILD_SHARED_LIBRARY)