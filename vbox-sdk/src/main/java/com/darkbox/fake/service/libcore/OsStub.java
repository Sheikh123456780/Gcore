package com.darkbox.fake.service.libcore;

import android.os.Process;

import java.lang.reflect.Method;

import black.libcore.io.BRLibcore;
import java.util.Objects;
import com.darkbox.VBoxCore;
import com.darkbox.app.BActivityThread;
import com.darkbox.core.VCore;
import com.darkbox.fake.hook.ClassInvocationStub;
import com.darkbox.fake.hook.MethodHook;
import com.darkbox.fake.hook.ProxyMethod;
import com.darkbox.fake.hook.ProxyMethods;
import com.darkbox.utils.Reflector;
import com.darkbox.utils.Slog;

/**
 * Created by @jagdish_vip on 4/9/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class OsStub extends ClassInvocationStub {
    public static final String TAG = "OsStub";
    private Object mBase;

    public OsStub() {
        mBase = BRLibcore.get().os();
    }

    @Override
    protected Object getWho() {
        return mBase;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        BRLibcore.get()._set_os(proxyInvocation);
    }

    @Override
    protected void onBindMethod() {
    }

    @Override
    public boolean isBadEnv() {
        return BRLibcore.get().os() != getProxyInvocation();
    }

    @ProxyMethod("getuid")
    public static class getuid extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int callUid = (int) method.invoke(who, args);
            return getFakeUid(callUid);
        }
    }

    @ProxyMethod("stat")
    public static class stat extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Object invoke = null;
            try {
                invoke = method.invoke(who, args);
            } catch (Throwable e) {
                throw e.getCause();
            }
            Reflector.with(invoke).field("st_uid").set(getFakeUid(-1));
            return invoke;
        }
    }

    @ProxyMethod("lstat")
    public static class lstat extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Object invoke = null;
            try {
                invoke = method.invoke(who, args);
            } catch (Throwable e) {
                throw e.getCause();
            }
            Reflector.with(invoke).field("st_uid").set(getFakeUid(-1));
            return invoke;
        }
    }

    @ProxyMethod("fstat")
    public static class fstat extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Object invoke = null;
            try {
                invoke = method.invoke(who, args);
            } catch (Throwable e) {
                throw e.getCause();
            }
            Reflector.with(invoke).field("st_uid").set(getFakeUid(-1));
            return invoke;
        }
    }

    @ProxyMethod("getpwnam")
    public static class getpwnam extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Object invoke = null;
            try {
                invoke = method.invoke(who, args);
            } catch (Throwable e) {
                throw e.getCause();
            }
            Reflector.with(invoke).field("pw_uid").set(getFakeUid(-1));
            return invoke;
        }
    }


    @ProxyMethod("getsockoptUcred")
    public static class getsockoptUcred extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Object invoke = null;
            try {
                invoke = method.invoke(who, args);
            } catch (Throwable e) {
                throw e.getCause();
            }
            Reflector.with(invoke).field("uid").set(getFakeUid(-1));
            return invoke;
        }
    }

    private static int getFakeUid(int callUid) {
        if (callUid > 0 && callUid <= Process.FIRST_APPLICATION_UID) {
            return callUid;
        }

        if (BActivityThread.isThreadInit() && BActivityThread.currentActivityThread().isInit()) {
            return BActivityThread.getBAppId();
        }
        return VBoxCore.getHostUid();
    }
}
