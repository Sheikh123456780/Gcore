package com.gcore.fake.service;

import android.content.Context;
import android.os.IInterface;

import java.lang.reflect.Method;

import black.android.os.BRServiceManager;
import black.android.view.BRIWindowManagerStub;
import black.android.view.BRWindowManagerGlobal;

import com.gcore.GreenBoxCore;
import com.gcore.fake.hook.BinderInvocationStub;
import com.gcore.fake.hook.MethodHook;
import com.gcore.fake.hook.ProxyMethod;
import com.gcore.utils.MethodParameterUtils;

public class IWindowManagerProxy extends BinderInvocationStub {

    public IWindowManagerProxy() {
        super(BRServiceManager.get().getService(Context.WINDOW_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIWindowManagerStub.get().asInterface(BRServiceManager.get().getService(Context.WINDOW_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.WINDOW_SERVICE);
        BRWindowManagerGlobal.get()._set_sWindowManagerService(null);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("openSession")
    public static class OpenSession extends BasePatchSession {

    }

    @ProxyMethod("setAppStartingWindow")
    public static class setAppStartingWindow extends BasePatchSession {

    }

    @ProxyMethod("overridePendingAppTransitionInPlace")
    public static class overridePendingAppTransitionInPlace extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args[0] instanceof String) {
                args[0] = GreenBoxCore.getHostPkg();
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("overridePendingAppTransition")
    public static class overridePendingAppTransition extends BasePatchSession {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args[0] instanceof String) {
                args[0] = GreenBoxCore.getHostPkg();
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("addAppToken")
    public static class addAppToken extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("setScreenCaptureDisabled")
    public static class setScreenCaptureDisabled extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("isPackageWaterfallExpanded")
    public static class isPackageWaterfallExpanded extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    abstract static class BasePatchSession extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            IInterface session = (IInterface) method.invoke(who, args);
            IWindowSessionProxy IWindowSessionProxy = new IWindowSessionProxy(session);
            IWindowSessionProxy.injectHook();
            return IWindowSessionProxy.getProxyInvocation();
        }
    }
}
