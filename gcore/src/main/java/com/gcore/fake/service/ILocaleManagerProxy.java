package com.gcore.fake.service;

import android.content.Context;

import black.android.app.BRILocaleManagerStub;
import black.android.os.BRServiceManager;

import com.gcore.fake.hook.BinderInvocationStub;
import com.gcore.fake.service.base.PkgMethodProxy;

public class ILocaleManagerProxy extends BinderInvocationStub {

    public static final String TAG = "ILocaleManagerProxy";

    public ILocaleManagerProxy() {
        super(BRServiceManager.get().getService(Context.LOCALE_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRILocaleManagerStub.get().asInterface(BRServiceManager.get().getService(Context.LOCALE_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.LOCALE_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new PkgMethodProxy("setApplicationLocales"));
        addMethodHook(new PkgMethodProxy("getApplicationLocales"));
    }
}
