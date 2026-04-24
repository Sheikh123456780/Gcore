package com.darkbox.fake.service;

import android.app.job.JobInfo;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;

import android.util.Log;
import java.lang.reflect.Method;
import android.content.pm.PackageManager;
import black.android.app.job.BRIJobSchedulerStub;
import black.android.os.BRServiceManager;
import com.darkbox.VBoxCore;
import com.darkbox.app.BActivityThread;
import com.darkbox.fake.hook.BinderInvocationStub;
import com.darkbox.fake.hook.MethodHook;
import com.darkbox.fake.hook.ProxyMethod;
import com.darkbox.utils.Slog;

public class IJobServiceProxy extends BinderInvocationStub {
    public static final String TAG = "JobServiceStub";

    public IJobServiceProxy() {
        super(BRServiceManager.get().getService(Context.JOB_SCHEDULER_SERVICE));
    }

    @Override
    protected Object getWho() {
        IBinder jobScheduler = BRServiceManager.get().getService("jobscheduler");
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
            return invokeSafely(who, method, args);
        }
    }

    @ProxyMethod("cancel")
    public static class Cancel extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            args[0] = VBoxCore.getBJobManager().cancel(BActivityThread.getAppConfig().processName, (Integer) args[0]);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("cancelAll")
    public static class CancelAll extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            VBoxCore.getBJobManager().cancelAll(BActivityThread.getAppConfig().processName);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("enqueue")
    public static class Enqueue extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return invokeSafely(who, method, args);
        }
    }

    private static Object invokeSafely(Object who, Method method, Object[] args) throws Throwable {
        JobInfo jobInfo = (JobInfo) args[0];
        if (jobInfo == null || jobInfo.getService() == null) {
            Log.e(TAG, "JobInfo or Service component is null, skipping job scheduling.");
            return -1;
        }

        int callingUid = Binder.getCallingUid();
        String pkgName = jobInfo.getService().getPackageName();

        if (!validateUidForPackage(pkgName, callingUid)) {
            Log.w(TAG, "UID mismatch detected, forcing UID correction.");
            callingUid = VBoxCore.getHostUid();
            Binder.restoreCallingIdentity(Binder.clearCallingIdentity());
        }

        JobInfo proxyJobInfo = VBoxCore.getBJobManager().schedule(jobInfo);
        args[0] = proxyJobInfo;
        Object result = method.invoke(who, args);

        return result != null ? result : -1;
    }

    private static boolean validateUidForPackage(String pkgName, int uid) {
        try {
            int actualUid = VBoxCore.getContext().getPackageManager().getApplicationInfo(pkgName, 0).uid;
            return actualUid == uid;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found: " + pkgName, e);
            return false;
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
