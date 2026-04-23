package com.gcore.fake.service;

import android.content.Context;
import android.util.Log;

import black.android.content.BRIClipboardStub;
import black.android.os.BRServiceManager;

import com.gcore.GreenBoxCore;
import com.gcore.fake.hook.BinderInvocationStub;

import java.lang.reflect.Method;

public class IClipboardManagerProxy extends BinderInvocationStub {

    private static final String TAG = "IClipboardManagerProxy";

    public IClipboardManagerProxy() {
        super(BRServiceManager.get().getService(Context.CLIPBOARD_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIClipboardStub.get().asInterface(BRServiceManager.get().getService(Context.CLIPBOARD_SERVICE));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int argIndex = getPackNameIndex(args);
        if (argIndex != -1) {
            args[argIndex] = GreenBoxCore.getHostPkg();
        }
        return super.invoke(proxy, method, args);
    }

    private int getPackNameIndex(Object[] args) {
        if (args == null) {
            return -1;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                Log.d(TAG, "args[" + i + "] " + args[i]);
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
