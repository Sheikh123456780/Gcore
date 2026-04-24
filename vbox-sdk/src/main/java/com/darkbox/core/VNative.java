package com.darkbox.core;

import com.darkbox.entity.AppConfig;

/**
 * Created by Milk on 4/9/21.
 * Des:
 */
public class VNative {
    static {
        System.loadLibrary("darkbox");
    }

    public static native void init(int apiLevel);

    public static native int getCallingUid(int orig);

    public static native String redirectPathString(String path);

    public static native Object redirectPathFile(Object path);

    public static native long[] loadEmptyDex();
    
    public static native void hideXposed();
    
    public static native void addIORule(String targetPath, String relocatePath);
    
    public static native void enableIO();
}
