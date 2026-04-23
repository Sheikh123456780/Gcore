package com.gcore.fake.service.context.providers;

import android.os.Bundle;
import android.os.IInterface;

import java.lang.reflect.Method;

import black.android.content.BRAttributionSource;

import com.gcore.GreenBoxCore;
import com.gcore.fake.hook.ClassInvocationStub;
import com.gcore.utils.compat.ContextCompat;

public class ContentProviderStub extends ClassInvocationStub implements BContentProvider {

    public static final String TAG = "ContentProviderStub";

    private IInterface mBase;
    private String mAppPkg;

    public IInterface wrapper(final IInterface contentProviderProxy, final String appPkg) {
        mBase = contentProviderProxy;
        mAppPkg = appPkg;
        injectHook();
        return (IInterface) getProxyInvocation();
    }

    @Override
    protected Object getWho() {
        return mBase;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("asBinder".equals(method.getName())) {
            return method.invoke(mBase, args);
        }
        if (args != null && args.length > 0) {
            Object arg = args[0];
            if (arg instanceof String) {
                args[0] = mAppPkg;
            } else if (arg.getClass().getName().equals(BRAttributionSource.getRealClass().getName())) {
                ContextCompat.fixAttributionSourceState(arg, GreenBoxCore.getHostUid());
            }
        }
        try {
            return method.invoke(mBase, args);
        } catch (Throwable e) {
            return getSafeDefaultValue(method.getReturnType());
        }
    }

    private Object getSafeDefaultValue(Class<?> returnType) {
        if (String.class.equals(returnType)) {
            return "true";
        } else if (int.class.equals(returnType) || Integer.class.equals(returnType)) {
            return 1;
        } else  if (long.class.equals(returnType) || Long.class.equals(returnType)) {
            return 1L;
        } else if (float.class.equals(returnType) || Float.class.equals(returnType)) {
            return 1.0f;
        } else if (boolean.class.equals(returnType) || Boolean.class.equals(returnType)) {
            return true;
        } else if (Bundle.class.equals(returnType)) {
            return new Bundle();
        }
        return null;
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
