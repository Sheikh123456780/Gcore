package com.gcore.core;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.security.MessageDigest;

import com.android.apksig.ApkVerifier;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class NativeCore {

    public static final String TAG = "NativeCore";

    static {
        System.loadLibrary("gcore");
    }

    private static native String getSha256();

    public static final String SHA256 = getSha256();

    public boolean runApk(Context ctx) {
        try {
            File apkFile = new File(ctx.getApplicationInfo().sourceDir);
            ApkVerifier verifier = new ApkVerifier.Builder(apkFile).build();
            ApkVerifier.Result result = verifier.verify();

            if (!result.isVerified()) {
                throw new SecurityException();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                result.getSignerCertificates().forEach(cert -> {
                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        String hash = bytesToHex(md.digest(cert.getEncoded()));
                        if (!SHA256.equalsIgnoreCase(hash)) {
                            throw new SecurityException();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            throw new SecurityException(e);
        }
        return true;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

}
