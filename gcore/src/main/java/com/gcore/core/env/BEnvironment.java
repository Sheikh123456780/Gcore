package com.gcore.core.env;

import android.os.Environment;
import android.os.Process;

import java.io.File;
import java.util.Locale;

import com.gcore.GreenBoxCore;
import com.gcore.utils.FileUtils;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class BEnvironment {

    private static final File InternalDirectory = new File(GreenBoxCore.getContext().getCacheDir().getParent());

    public static void load() {
        FileUtils.mkdirs(InternalDirectory);
        FileUtils.mkdirs(getSystemDir());
        FileUtils.mkdirs(getCacheDir());
        FileUtils.mkdirs(getProcDir());
    }

    public static File getCacheDir() {
        return new File(InternalDirectory, "cache");
    }

    public static File getProcDir() {
        return new File(InternalDirectory, "proc");
    }

    public static File getSystemDir() {
        return new File(InternalDirectory, "system");
    }

    public static File getUserInfoConf() {
        return new File(getSystemDir(), "user.conf");
    }

    public static File getAccountsConf() {
        return new File(getSystemDir(), "accounts.conf");
    }

    public static File getUidConf() {
        return new File(getSystemDir(), "uid.conf");
    }

    public static File getSharedUserConf() {
        return new File(getSystemDir(), "shared-user.conf");
    }

    public static File getFakeLocationConf() {
        return new File(getSystemDir(), "fake-location.conf");
    }

    // ========== FIXED: Add userId parameter to match Score ==========
    public static File getExternalUserDir(int userId) {
        // Return standard external storage for now
        // You can add userId-based isolation later
        return Environment.getExternalStorageDirectory();
    }

    // Keep this for backward compatibility if needed
    public static File getExternalUserDir() {
        return Environment.getExternalStorageDirectory();
    }

    public static File getExternalDataDir(String packageName) {
        return new File(getExternalUserDir(), String.format(Locale.getDefault(), "Android/data/%s", packageName));
    }

    public static File getExternalObbDir(String packageName) {
        return new File(getExternalUserDir(), String.format(Locale.getDefault(), "Android/obb/%s", packageName));
    }

    public static File getAppDir(String packageName) {
        return new File(InternalDirectory, String.format(Locale.getDefault(), "data/app/%s", packageName));
    }

    public static File getDataDir(String packageName, int userId) {
        return new File(InternalDirectory, String.format(Locale.getDefault(), "data/user/%d/%s", userId, packageName));
    }

    public static File getDeDataDir(String packageName, int userId) {
        return new File(InternalDirectory, String.format(Locale.getDefault(), "data/user_de/%d/%s", userId, packageName));
    }

    public static File getFilesDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "files");
    }

    public static File getOatDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "oat");
    }

    public static File getAppRootDir() {
        return getAppDir("");
    }

    public static File getAppLibDir(String packageName) {
        return new File(getAppDir(packageName), "lib/arm64");
    }

    public static File getBaseApkDir(String packageName) {
        return new File(getAppDir(packageName), "base.apk");
    }

    public static File getPackageConf(String packageName) {
        return new File(getAppDir(packageName), "package.conf");
    }

    public static File getProcDir(int pid) {
        File file = new File(getProcDir(), String.format(Locale.getDefault(), "%d", pid));
        FileUtils.mkdirs(file);
        return file;
    }
    
    // Add this for compatibility with IOCore's getProcDir(int)
    public static File getProcDir() {
        return new File(InternalDirectory, "proc");
    }
}
