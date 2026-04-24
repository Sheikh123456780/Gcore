package com.darkbox.proxy;

import java.util.Locale;

import com.darkbox.VBoxCore;

/**
 * Created by Milk on 4/1/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class ProxyManifest {
    public static final int FREE_COUNT = 50;

    public static boolean isProxy(String msg) {
        return getBindProvider().equals(msg) || msg.contains("proxy_content_provider_");
    }

    public static String getBindProvider() {
        return VBoxCore.getHostPkg() + ".SystemCallProvider";
    }

    public static String getProxyAuthorities(int index) {
        return String.format(Locale.CHINA, "%s.proxy_content_provider_%d", VBoxCore.getHostPkg(), index);
    }

    public static String getProxyPendingActivity(int index) {
        return String.format(Locale.CHINA, "com.darkbox.proxy.ProxyPendingActivity$P%d", index);
    }

    public static String getProxyActivity(int index) {
        return String.format(Locale.CHINA, "com.darkbox.proxy.ProxyActivity$P%d", index);
    }

    public static String TransparentProxyActivity(int index) {
        return String.format(Locale.CHINA, "com.darkbox.proxy.TransparentProxyActivity$P%d", index);
    }

    public static String getProxyService(int index) {
        return String.format(Locale.CHINA, "com.darkbox.proxy.ProxyService$P%d", index);
    }

    public static String getProxyJobService(int index) {
        return String.format(Locale.CHINA, "com.darkbox.proxy.ProxyJobService$P%d", index);
    }

    public static String getProxyFileProvider() {
        return VBoxCore.getHostPkg() + ".FileProvider";
    }

    public static String getProxyReceiver() {
        return VBoxCore.getHostPkg() + ".stub_receiver";
    }

    public static String getProcessName(int bPid) {
        return VBoxCore.getHostPkg() + ":p" + bPid;
    }
}
