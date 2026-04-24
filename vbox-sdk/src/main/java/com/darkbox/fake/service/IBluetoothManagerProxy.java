package com.darkbox.fake.service;

import java.lang.reflect.Method;

import black.android.bluetooth.BRIBluetoothManagerStub;
import black.android.os.BRServiceManager;

import com.darkbox.fake.hook.BinderInvocationStub;
import com.darkbox.fake.hook.MethodHook;
import com.darkbox.fake.hook.ProxyMethod;

/**
 * Bluetooth MIC + Voice Record FIX
 * Android 9 → Android 15
 * VBox / BlackBox compatible
 */
public class IBluetoothManagerProxy extends BinderInvocationStub {

    public static final String TAG = "IBluetoothManagerProxy";
    
    private Object mWho;

    public IBluetoothManagerProxy() {
        super(BRServiceManager.get().getService("bluetooth_manager"));
    }

    @Override
    protected Object getWho() {
        if (mWho == null) {
            mWho = BRIBluetoothManagerStub.get().asInterface(BRServiceManager.get().getService("bluetooth_manager"));
        }
        return mWho;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("bluetooth_manager");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(mWho != null ? mWho : getWho(), args);
    }

    @ProxyMethod("getName")
    public static class GetName extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }
}