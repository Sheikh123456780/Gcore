package com.darkbox.fake.service.context.providers;

import android.os.IInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import black.android.content.BRAttributionSource;
import com.darkbox.VBoxCore;
import com.darkbox.fake.hook.ClassInvocationStub;
import com.darkbox.utils.compat.ContextCompat;

/**
 * FULL SAFE SystemProviderStub
 * Android 10 → 15+
 */
public class SystemProviderStub extends ClassInvocationStub
        implements BContentProvider {

    private IInterface mBase;

    @Override
    public IInterface wrapper(IInterface contentProviderProxy, String appPkg) {
        mBase = contentProviderProxy;
        injectHook();
        return (IInterface) getProxyInvocation();
    }

    @Override
    protected Object getWho() {
        return mBase;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        // keep empty
    }

    @Override
    protected void onBindMethod() {
        // keep empty
    }

    @Override
    public boolean isBadEnv() {
        return mBase == null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("asBinder".equals(method.getName())) {
            return method.invoke(mBase, args);
        }

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) continue;
                if (arg instanceof String) {
                    args[i] = VBoxCore.getHostPkg();
                    continue;
                }
                if (BRAttributionSource.getRealClass() != null && arg.getClass().getName().equals(BRAttributionSource.getRealClass().getName())) {
                    ContextCompat.fixAttributionSourceState(arg, VBoxCore.getHostUid());
                }
            }
        }

        try {
            return method.invoke(mBase, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            // 🔒 MAIN CRASH FIX
            if (cause instanceof SecurityException) {
                return null;
            }
            throw cause;
        }
    }
}