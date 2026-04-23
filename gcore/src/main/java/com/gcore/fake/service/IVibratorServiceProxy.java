package com.gcore.fake.service;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

import black.android.os.BRIVibratorManagerServiceStub;
import black.android.os.BRServiceManager;
import black.com.android.internal.os.BRIVibratorServiceStub;

import com.gcore.fake.hook.BinderInvocationStub;
import com.gcore.utils.MethodParameterUtils;
import com.gcore.utils.compat.BuildCompat;

public class IVibratorServiceProxy extends BinderInvocationStub {

    private static final String VIBRATOR;

    static {
        if (BuildCompat.isS_V2()) {
            VIBRATOR = Context.VIBRATOR_MANAGER_SERVICE;
        } else {
            VIBRATOR = Context.VIBRATOR_SERVICE;
        }
    }

    public IVibratorServiceProxy() {
        super(BRServiceManager.get().getService(VIBRATOR));
    }

    @Override
    protected Object getWho() {
        IBinder service = BRServiceManager.get().getService(VIBRATOR);
        if (BuildCompat.isS()) {
            return BRIVibratorManagerServiceStub.get().asInterface(service);
        }
        return BRIVibratorServiceStub.get().asInterface(service);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(VIBRATOR);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstUid(args);
        MethodParameterUtils.replaceFirstAppPkg(args);
        return super.invoke(proxy, method, args);
    }
}
