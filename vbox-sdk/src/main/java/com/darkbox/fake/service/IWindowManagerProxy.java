package com.darkbox.fake.service;

import android.os.IInterface;

import java.lang.reflect.Method;

import black.android.os.BRServiceManager;
import black.android.view.BRIWindowManagerStub;
import black.android.view.BRWindowManagerGlobal;
import com.darkbox.VBoxCore;
import com.darkbox.fake.hook.BinderInvocationStub;
import com.darkbox.fake.hook.MethodHook;
import com.darkbox.fake.hook.ProxyMethod;
import com.darkbox.utils.MethodParameterUtils;

/**
 * Created by @jagdish_vip on 4/6/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class IWindowManagerProxy extends BinderInvocationStub {
    public static final String TAG = "WindowManagerStub";

    public IWindowManagerProxy() {
        super(BRServiceManager.get().getService("window"));
    }

    @Override
    protected Object getWho() {
        return BRIWindowManagerStub.get().asInterface(BRServiceManager.get().getService("window"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("window");
        BRWindowManagerGlobal.get()._set_sWindowManagerService(null);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
    
    // ---------------- Proxy Methods ----------------

    @ProxyMethod("openSession")
    public static class OpenSession extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            IInterface session = (IInterface) method.invoke(who, args);
            IWindowSessionProxy proxy = new IWindowSessionProxy(session);
            proxy.injectHook();
            return proxy.getProxyInvocation();
        }
    }

    @ProxyMethod("setAppStartingWindow")
    public static class SetAppStartingWindow extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            IInterface session = (IInterface) method.invoke(who, args);
            IWindowSessionProxy proxy = new IWindowSessionProxy(session);
            proxy.injectHook();
            return proxy.getProxyInvocation();
        }
    }

    @ProxyMethod("overridePendingAppTransitionInPlace")
    public static class OverridePendingAppTransitionInPlace extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                args[0] = VBoxCore.getHostPkg();
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("overridePendingAppTransition")
    public static class OverridePendingAppTransition extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                args[0] = VBoxCore.getHostPkg();
            }
            IInterface session = (IInterface) method.invoke(who, args);
            IWindowSessionProxy proxy = new IWindowSessionProxy(session);
            proxy.injectHook();
            return proxy.getProxyInvocation();
        }
    }

    @ProxyMethod("addAppToken")
    public static class AddAppToken extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("setScreenCaptureDisabled")
    public static class SetScreenCaptureDisabled extends MethodHook {
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
    
}
