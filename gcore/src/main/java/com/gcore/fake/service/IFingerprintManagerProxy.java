package com.gcore.fake.service;

import android.content.Context;

import black.android.os.BRServiceManager;
import black.android.view.BRIGraphicsStatsStub;
import com.gcore.fake.hook.BinderInvocationStub;
import com.gcore.fake.service.base.PkgMethodProxy;

public class IFingerprintManagerProxy extends BinderInvocationStub {

    public IFingerprintManagerProxy() {
        super(BRServiceManager.get().getService(Context.FINGERPRINT_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIGraphicsStatsStub.get().asInterface(BRServiceManager.get().getService(Context.FINGERPRINT_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.FINGERPRINT_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new PkgMethodProxy("isHardwareDetected"));
        addMethodHook(new PkgMethodProxy("hasEnrolledFingerprints"));
        addMethodHook(new PkgMethodProxy("authenticate"));
        addMethodHook(new PkgMethodProxy("cancelAuthentication"));
        addMethodHook(new PkgMethodProxy("getEnrolledFingerprints"));
        addMethodHook(new PkgMethodProxy("getAuthenticatorId"));
    }
}
