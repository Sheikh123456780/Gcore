package com.gcore.fake.service;

import android.content.Context;

import black.android.net.BRIVpnManagerStub;
import black.android.os.BRServiceManager;

import com.gcore.fake.hook.BinderInvocationStub;
import com.gcore.fake.hook.ScanClass;

@ScanClass(VpnCommonProxy.class)
public class IVpnManagerProxy extends BinderInvocationStub {

    public static final String TAG = "IVpnManagerProxy";

    public IVpnManagerProxy() {
        super(BRServiceManager.get().getService(Context.VPN_MANAGEMENT_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIVpnManagerStub.get().asInterface(BRServiceManager.get().getService(Context.VPN_MANAGEMENT_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.VPN_MANAGEMENT_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
