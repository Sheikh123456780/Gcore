package com.gcore.core.env;

import android.content.ComponentName;

import java.util.ArrayList;
import java.util.List;

import com.gcore.GreenBoxCore;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class AppSystemEnv {

    private static final List<String> SystemPackages = new ArrayList<>();
    private static final List<String> SuPackages = new ArrayList<>();
    private static final List<String> PreInstallPackages = new ArrayList<>();

    static {
        SystemPackages.add(GreenBoxCore.getHostPkg());
        SystemPackages.add("android");
        SystemPackages.add("com.google.android.webview");
        SystemPackages.add("com.google.android.webview.dev");
        SystemPackages.add("com.google.android.webview.beta");
        SystemPackages.add("com.google.android.webview.canary");
        SystemPackages.add("com.android.webview");
        SystemPackages.add("com.le.android.webview");
        SystemPackages.add("com.android.camera");
        SystemPackages.add("com.android.talkback");
        // Miui
        SystemPackages.add("com.miui.gallery");
        SystemPackages.add("com.lbe.security.miui");
        SystemPackages.add("com.miui.contentcatcher");
        SystemPackages.add("com.miui.catcherpatch");
        // Permission
        SystemPackages.add("com.android.permissioncontroller");
        SystemPackages.add("com.google.android.permissioncontroller");
        // Gboard
        SystemPackages.add("com.google.android.inputmethod.latin");
        // Huawei
        SystemPackages.add("com.huawei.webview");
        // Oppo Realme
        SystemPackages.add("com.heytap.openid");
        SystemPackages.add("com.coloros.safecenter");
        // Samsung
        SystemPackages.add("com.samsung.android.deviceidservice");
        // Asus
        SystemPackages.add("com.asus.msa.SupplementaryDID");
        // Lenovo
        SystemPackages.add("com.zui.deviceidservice");
        // Meizu
        SystemPackages.add("com.mdid.msa");
        // Su
        SuPackages.add("com.noshufou.android.su");
        SuPackages.add("com.noshufou.android.su.elite");
        SuPackages.add("eu.chainfire.supersu");
        SuPackages.add("com.koushikdutta.superuser");
        SuPackages.add("com.thirdparty.superuser");
        SuPackages.add("com.yellowes.su");
        SuPackages.add("com.topjohnwu.magisk");

        PreInstallPackages.add("com.huawei.hwid");
    }

    public static boolean isOpenPackage(String packageName) {
        return SystemPackages.contains(packageName);
    }

    public static boolean isOpenPackage(ComponentName componentName) {
        return componentName != null && isOpenPackage(componentName.getPackageName());
    }

    public static boolean isBlackPackage(String packageName) {
        return GreenBoxCore.get().isHideRoot() && SuPackages.contains(packageName);
    }

    public static List<String> getPreInstallPackages() {
        return PreInstallPackages;
    }
}
