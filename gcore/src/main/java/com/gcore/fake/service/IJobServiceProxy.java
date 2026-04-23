package com.gcore.fake.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

import black.android.app.job.BRIJobSchedulerStub;
import black.android.os.BRServiceManager;

import com.gcore.GreenBoxCore;
import com.gcore.app.GActivityThread;
import com.gcore.fake.hook.BinderInvocationStub;
import com.gcore.fake.hook.MethodHook;
import com.gcore.fake.hook.ProxyMethod;

public class IJobServiceProxy extends BinderInvocationStub {

    public IJobServiceProxy() {
        super(BRServiceManager.get().getService(Context.JOB_SCHEDULER_SERVICE));
    }

    @Override
    protected Object getWho() {
        IBinder jobScheduler = BRServiceManager.get().getService(Context.JOB_SCHEDULER_SERVICE);
        return BRIJobSchedulerStub.get().asInterface(jobScheduler);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @ProxyMethod("schedule")
    public static class Schedule extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (args != null && args.length > 0 && args[0] instanceof JobInfo jobInfo) {
                    JobInfo proxyJobInfo = GreenBoxCore.getBJobManager().schedule(jobInfo);
                    args[0] = proxyJobInfo;
                }
                return method.invoke(who, args);
            } catch (Throwable e) {
                return JobScheduler.RESULT_FAILURE;
            }
        }
    }

    @ProxyMethod("enqueue")
    public static class Enqueue extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (args != null && args.length > 0 && args[0] instanceof JobInfo jobInfo) {
                    JobInfo proxyJobInfo = GreenBoxCore.getBJobManager().schedule(jobInfo);
                    args[0] = proxyJobInfo;
                }
                return method.invoke(who, args);
            } catch (Throwable e) {
                return JobScheduler.RESULT_FAILURE;
            }
        }
    }

    @ProxyMethod("cancel")
    public static class Cancel extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof Integer jobId) {
                GreenBoxCore.getBJobManager().cancel(GActivityThread.getAppConfig().processName, jobId);
            }
            method.invoke(who, args);
            return null;
        }
    }

    @ProxyMethod("cancelAll")
    public static class CancelAll extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            GreenBoxCore.getBJobManager().cancelAll(GActivityThread.getAppConfig().processName);
            method.invoke(who, args);
            return null;
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
