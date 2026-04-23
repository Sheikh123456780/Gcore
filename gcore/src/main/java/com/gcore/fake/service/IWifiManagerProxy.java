package com.gcore.fake.service;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.os.WorkSource;

import java.lang.reflect.Method;

import black.android.net.wifi.BRIWifiManagerStub;
import black.android.os.BRServiceManager;
import com.gcore.fake.hook.BinderInvocationStub;
import com.gcore.fake.hook.MethodHook;
import com.gcore.fake.hook.ProxyMethod;
import com.gcore.utils.ArrayUtils;
import com.gcore.utils.MethodParameterUtils;

public class IWifiManagerProxy extends BinderInvocationStub {

    public IWifiManagerProxy() {
        super(BRServiceManager.get().getService(Context.WIFI_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIWifiManagerStub.get().asInterface(BRServiceManager.get().getService(Context.WIFI_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getConnectionInfo")
    public static class GetConnectionInfo extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            WifiInfo wifiInfo = (WifiInfo) method.invoke(who, args);
            return wifiInfo;
        }

    }

    @ProxyMethod("getBatchedScanResults")
    public static class getBatchedScanResults extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("acquireWifiLock")
    public static class acquireWifiLock extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
            if (index >= 0) {
                args[index] = null;
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("updateWifiLockWorkSource")
    public static class updateWifiLockWorkSource extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
            if (index >= 0) {
                args[index] = null;
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("startLocationRestrictedScan")
    public static class startLocationRestrictedScan extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
            if (index >= 0) {
                args[index] = null;
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("requestBatchedScan")
    public static class requestBatchedScan extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
            if (index >= 0) {
                args[index] = null;
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("setWifiEnabled")
    public static class setWifiEnabled extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getConfiguredNetworks")
    public static class getConfiguredNetworks extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("setWifiApConfiguration")
    public static class setWifiApConfiguration extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod("startLocalOnlyHotspot")
    public static class startLocalOnlyHotspot extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("startScan")
    public static class startScan extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
            if (index >= 0) {
                args[index] = null;
            }
            return method.invoke(who, args);
        }
    }
}
