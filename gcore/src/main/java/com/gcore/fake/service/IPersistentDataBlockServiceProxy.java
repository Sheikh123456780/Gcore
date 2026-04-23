package com.gcore.fake.service;

import android.content.Context;

import black.android.os.BRServiceManager;
import black.android.service.persistentdata.BRIPersistentDataBlockServiceStub;

import com.gcore.fake.hook.BinderInvocationStub;
import com.gcore.fake.service.base.ValueMethodProxy;

public class IPersistentDataBlockServiceProxy extends BinderInvocationStub {

    public IPersistentDataBlockServiceProxy() {
        super(BRServiceManager.get().getService(Context.PERSISTENT_DATA_BLOCK_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIPersistentDataBlockServiceStub.get().asInterface(BRServiceManager.get().getService(Context.PERSISTENT_DATA_BLOCK_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("write", -1));
        addMethodHook(new ValueMethodProxy("read", new byte[0]));
        addMethodHook(new ValueMethodProxy("wipe", null));
        addMethodHook(new ValueMethodProxy("getDataBlockSize", 0));
        addMethodHook(new ValueMethodProxy("getMaximumDataBlockSize", 0));
        addMethodHook(new ValueMethodProxy("setOemUnlockEnabled", 0));
        addMethodHook(new ValueMethodProxy("getOemUnlockEnabled", false));
    }
}
