package com.gcore.core;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import com.gcore.GreenBoxCore;
import com.gcore.app.GActivityThread;

import org.lsposed.lsparanoid.Obfuscate;

import java.io.File;

@Obfuscate
public class NativeCore {

    public static final String TAG = "NativeCore";

    static {
        System.loadLibrary("gcore");
    }

    // ========== NATIVE METHODS ==========
    public static native void init(int api_level);
    public static native void enableIO();
    public static native void addIORule(String targetPath, String relocatePath);
    public static native boolean disableHiddenApi();
    public static native void hideXposed();
    public static native void init_seccomp();

    // ========== STATIC METHODS ==========
    
    public static int getCallingUid(int origCallingUid) {
        // Copy GBox logic exactly
        if ((origCallingUid <= 0 || origCallingUid >= Process.FIRST_APPLICATION_UID) 
                && origCallingUid <= Process.LAST_APPLICATION_UID) {
            int result = origCallingUid;
            if (origCallingUid == GreenBoxCore.getHostUid()) {
                result = GActivityThread.getCallingBUid();  // Note: getCallingBUid
            }
            return result;
        }
        return origCallingUid;
    }

    public static String redirectPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return path;
        }
        // Create IOCore similar to GBox
        return IOCore.get().redirectPath(path);
    }

    public static File redirectPath(File file) {
        if (file == null) {
            return null;
        }
        return IOCore.get().redirectPath(file);
    }

    // ========== EXISTING ==========
    public static final String SHA256 = "BYPASSED";

    public boolean runApk(Context ctx) {
        return true;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
