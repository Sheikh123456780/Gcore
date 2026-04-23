package com.gcore.fake.frameworks;

import android.app.job.JobInfo;
import android.os.RemoteException;

import com.gcore.app.GActivityThread;
import com.gcore.core.system.ServiceManager;
import com.gcore.core.system.am.IBJobManagerService;
import com.gcore.entity.JobRecord;

public class BJobManager extends BlackManager<IBJobManagerService> {

    private static final BJobManager sJobManager = new BJobManager();

    public static BJobManager get() {
        return sJobManager;
    }

    @Override
    protected String getServiceName() {
        return ServiceManager.JOB_MANAGER;
    }

    public JobInfo schedule(JobInfo info) {
        try {
            return getService().schedule(info, GActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JobRecord queryJobRecord(String processName, int jobId) {
        try {
            return getService().queryJobRecord(processName, jobId, GActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cancelAll(String processName) {
        try {
            getService().cancelAll(processName, GActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void cancel(String processName, int jobId) {
        try {
            getService().cancel(processName, jobId, GActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
