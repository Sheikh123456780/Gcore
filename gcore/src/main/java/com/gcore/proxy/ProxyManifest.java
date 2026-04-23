package com.gcore.proxy;

import java.util.Locale;

import com.gcore.GreenBoxCore;

public class ProxyManifest {

    public static final int FREE_COUNT = 50;

    public static boolean isProxy(String msg) {
        return getBindProvider().equals(msg) || msg.contains("proxy_content_provider_");
    }

    public static String getBindProvider() {
        return GreenBoxCore.getHostPkg() + ".SystemCallProvider";
    }

    public static String getProxyAuthorities(int index) {
        return String.format(Locale.getDefault(), "%s.proxy_content_provider_%d", GreenBoxCore.getHostPkg(), index);
    }

    public static String getProxyPendingActivity(int index) {
        return String.format(Locale.getDefault(), "com.gcore.proxy.ProxyPendingActivity$P%d", index);
    }

    public static String getProxyActivity(int index) {
        return String.format(Locale.getDefault(), "com.gcore.proxy.ProxyActivity$P%d", index);
    }

    public static String TransparentProxyActivity(int index) {
        return String.format(Locale.getDefault(), "com.gcore.proxy.TransparentProxyActivity$P%d", index);
    }

    public static String getProxyService(int index) {
        return String.format(Locale.getDefault(), "com.gcore.proxy.ProxyService$P%d", index);
    }

    public static String getProxyJobService(int index) {
        return String.format(Locale.getDefault(), "com.gcore.proxy.ProxyJobService$P%d", index);
    }

    public static String getProxyFileProvider() {
        return GreenBoxCore.getHostPkg() + ".FileProvider";
    }

    public static String getProcessName(int bPid) {
        return GreenBoxCore.getHostPkg() + ":p" + bPid;
    }
}
