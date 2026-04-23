package com.gcore.fake.hook;

import java.lang.reflect.Method;

import com.gcore.GreenBoxCore;

public abstract class MethodHook {

    protected String getMethodName() {
        return null;
    }

    protected Object afterHook(Object result) {
        return result;
    }

    protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
        return null;
    }

    protected abstract Object hook(Object who, Method method, Object[] args) throws Throwable;

    protected boolean isEnable() {
        return GreenBoxCore.get().isBlackProcess();
    }
}
