package com.gcore.core;

import android.content.Context;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class NativeCore {

    public static final String TAG = "NativeCore";

    // ❌ Removed JNI
    // static { System.loadLibrary("Game-Loader"); }

    // ❌ Removed native method
    // private static native String getSha256();

    // Dummy value
    public static final String SHA256 = "";

    // ✅ Disable verification (for testing)
    public boolean runApk(Context ctx) {
        return true;
    }
}
