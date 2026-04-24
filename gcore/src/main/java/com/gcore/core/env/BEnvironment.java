
package com.gcore.core.env;

import android.os.Environment;

import java.io.File;
import java.util.Locale;

import com.gcore.GreenBoxCore;
import com.gcore.utils.FileUtils;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class BEnvironment {

    // Make it non-final and initialize lazily
    private static File InternalDirectory = null;

    private static File getInternalDirectory() {
        if (InternalDirectory == null) {
            // Use files directory parent instead of cache directory parent
            // This gives /data/data/package.name instead of /data/user/0/package.name
            File filesDir = GreenBoxCore.getContext().getFilesDir();
            if (filesDir != null) {
                InternalDirectory = filesDir.getParentFile();
            } else {
                // Fallback
                InternalDirectory = new File("/data/data/" + GreenBoxCore.getContext().getPackageName());
            }
        }
        return InternalDirectory;
    }

    // Allow external code to set the directory (called from GreenBoxCore)
    public static void setInternalDirectory(File dir) {
        InternalDirectory = dir;
        load(); // Recreate directories
    }

    public static void load() {
        File dir = getInternalDirectory();
        FileUtils.mkdirs(dir);
        FileUtils.mkdirs(getSystemDir());
        FileUtils.mkdirs(getCacheDir());
        FileUtils.mkdirs(getProcDir());
    }

    public static File getCacheDir() {
        return new File(getInternalDirectory(), "cache");
    }

    public static File getProcDir() {
        return new File(getInternalDirectory(), "proc");
    }

    public static File getSystemDir() {
        return new File(getInternalDirectory(), "system");
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

    public static File getExternalUserDir() {
        return new File(Environment.getExternalStorageDirectory(), "sdcard");
    }

    public static File getExternalDataDir(String packageName) {
        return new File(getExternalUserDir(), String.format(Locale.getDefault(), "Android/data/%s", packageName));
    }

    public static File getExternalObbDir(String packageName) {
        return new File(getExternalUserDir(), String.format(Locale.getDefault(), "Android/obb/%s", packageName));
    }

    public static File getAppDir(String packageName) {
        return new File(getInternalDirectory(), String.format(Locale.getDefault(), "data/app/%s", packageName));
    }

    public static File getDataDir(String packageName, int userId) {
        return new File(getInternalDirectory(), String.format(Locale.getDefault(), "data/user/%d/%s", userId, packageName));
    }

    public static File getDeDataDir(String packageName, int userId) {
        return new File(getInternalDirectory(), String.format(Locale.getDefault(), "data/user_de/%d/%s", userId, packageName));
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
}
