package com.darkbox.core.system;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import com.darkbox.VBoxCore;
import com.darkbox.core.IBActivityThread;
import com.darkbox.core.env.BEnvironment;
import com.darkbox.core.system.pm.BPackageManagerService;
import com.darkbox.core.system.user.BUserHandle;
import com.darkbox.entity.AppConfig;
import com.darkbox.proxy.ProxyManifest;
import com.darkbox.utils.FileUtils;
import com.darkbox.utils.Slog;
import com.darkbox.utils.compat.ApplicationThreadCompat;
import com.darkbox.utils.compat.BundleCompat;
import com.darkbox.utils.provider.ProviderCall;

public class BProcessManagerService implements ISystemService {
    public static final String TAG = "BProcessManager";
    private static final int PROCESS_POOL_SIZE = 3;

    public static BProcessManagerService sBProcessManagerService = new BProcessManagerService();
    private final Map<Integer, Map<String, ProcessRecord>> mProcessMap = new HashMap<>();
    private final List<ProcessRecord> mPidsSelfLocked = new ArrayList<>();
    private final Object mProcessLock = new Object();
    
    // Process pool for fast allocation
    private static final ArrayBlockingQueue<Integer> sFreeProcessPool = new ArrayBlockingQueue<>(PROCESS_POOL_SIZE);
    private static volatile boolean sPoolInitialized = false;
    
    // Static initializer for process pool
    static {
        Thread poolInit = new Thread(() -> {
            try {
                Thread.sleep(500); // Wait for system ready
                initProcessPool();
            } catch (Exception e) {
                Slog.e(TAG, "Process pool init error", e);
            }
        }, "ProcessPoolInit");
        poolInit.setDaemon(true);
        poolInit.start();
    }
    
    private static void initProcessPool() {
        try {
            Slog.d(TAG, "Initializing process pool...");
            for (int i = 0; i < PROCESS_POOL_SIZE; i++) {
                int bpid = getUsingBPidL();
                if (bpid != -1) {
                    sFreeProcessPool.offer(bpid);
                    Slog.d(TAG, "Added process to pool: " + bpid);
                }
            }
            sPoolInitialized = true;
            Slog.d(TAG, "Process pool initialized with " + sFreeProcessPool.size() + " processes");
        } catch (Exception e) {
            Slog.e(TAG, "Failed to init process pool", e);
        }
    }
    
    private static int getUsingBPidL() {
        try {
            ActivityManager manager = (ActivityManager) VBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            if (runningAppProcesses == null) return -1;
            
            Set<Integer> usingPs = new HashSet<>();
            for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                int i = parseBPid(runningAppProcess.processName);
                if (i != -1) {
                    usingPs.add(i);
                }
            }
            for (int i = 0; i < ProxyManifest.FREE_COUNT; i++) {
                if (!usingPs.contains(i)) {
                    return i;
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "Error getting BPid", e);
        }
        return -1;
    }

    public static BProcessManagerService get() {
        return sBProcessManagerService;
    }

    // Optimized process start with pool
    public ProcessRecord startProcessLocked(String packageName, String processName, int userId, int bpid, int callingPid) {
        ApplicationInfo info = BPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
        if (info == null) {
            return null;
        }
        ProcessRecord app;
        int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
        synchronized (mProcessLock) {
            Map<String, ProcessRecord> bProcess = mProcessMap.get(buid);
            if (bProcess == null) {
                bProcess = new HashMap<>();
            }
            if (bpid == -1) {
    app = bProcess.get(processName);
    if (app != null) {
        if (app.bActivityThread != null) {
            return app;
        }
    }
    // Get from pool first
    bpid = sFreeProcessPool.poll();
    if (bpid == -1 && sPoolInitialized) {
        bpid = getUsingBPidL();
    } else if (bpid == -1) {
        bpid = getUsingBPidL();
    }
    Slog.d(TAG, "init bUid = " + buid + ", bPid = " + bpid);
}
            if (bpid == -1) {
                throw new RuntimeException("No processes available");
            }
            app = new ProcessRecord(info, processName);
            app.uid = Process.myUid();
            app.bpid = bpid;
            app.buid = BPackageManagerService.get().getAppId(packageName);
            app.callingBUid = getBUidByPidOrPackageName(callingPid, packageName);
            app.userId = userId;

            bProcess.put(processName, app);
            mPidsSelfLocked.add(app);

            mProcessMap.put(buid, bProcess);
            if (!initAppProcessL(app)) {
                bProcess.remove(processName);
                mPidsSelfLocked.remove(app);
                // Return to pool if init failed
                if (bpid != -1) {
                    sFreeProcessPool.offer(bpid);
                }
                app = null;
            } else {
                app.pid = getPid(VBoxCore.getContext(), ProxyManifest.getProcessName(app.bpid));
            }
        }
        return app;
    }

    public void restartAppProcess(String packageName, String processName, int userId) {
        synchronized (mProcessLock) {
            int callingPid = Binder.getCallingPid();
            ProcessRecord app;
            synchronized (mProcessLock) {
                app = findProcessByPid(callingPid);
            }
            if (app == null) {
                String stubProcessName = getProcessName(VBoxCore.getContext(), callingPid);
                int bpid = parseBPid(stubProcessName);
                startProcessLocked(packageName, processName, userId, bpid, callingPid);
            }
        }
    }

    private static int parseBPid(String stubProcessName) {
        if (stubProcessName == null) return -1;
        String prefix = VBoxCore.getHostPkg() + ":p";
        if (stubProcessName.startsWith(prefix)) {
            try {
                return Integer.parseInt(stubProcessName.substring(prefix.length()));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return -1;
    }

    private boolean initAppProcessL(ProcessRecord record) {
        Slog.d(TAG, "initProcess: " + record.processName);
        
        AppConfig appConfig = record.getClientConfig();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConfig.KEY, appConfig);
        Bundle init = ProviderCall.callSafely(record.getProviderAuthority(), "_Black_|_init_process_", null, bundle);
        IBinder appThread = BundleCompat.getBinder(init, "_Black_|_client_");
        if (appThread == null || !appThread.isBinderAlive()) {
            return false;
        }
        attachClientL(record, appThread);
        createProc(record);
        return true;
    }

    private void attachClientL(final ProcessRecord app, final IBinder appThread) {
        IBActivityThread activityThread = IBActivityThread.Stub.asInterface(appThread);
        if (activityThread == null) {
            app.kill();
            return;
        }
        try {
            appThread.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Slog.d(TAG, "App Died: " + app.processName);
                    appThread.unlinkToDeath(this, 0);
                    onProcessDie(app);
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.bActivityThread = activityThread;
        try {
            app.appThread = ApplicationThreadCompat.asInterface(activityThread.getActivityThread());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.initLock.open();
    }

    public void onProcessDie(ProcessRecord record) {
        synchronized (mProcessLock) {
            record.kill();
            Map<String, ProcessRecord> process = mProcessMap.get(record.buid);
            if (process != null) {
                process.remove(record.processName);
                if (process.isEmpty()) {
                    mProcessMap.remove(record.buid);
                }
            }
            mPidsSelfLocked.remove(record);
            removeProc(record);
            // Return to pool
            if (record.bpid != -1) {
                sFreeProcessPool.offer(record.bpid);
            }
        }
    }

    public ProcessRecord findProcessRecord(String packageName, String processName, int userId) {
        synchronized (mProcessLock) {
            int appId = BPackageManagerService.get().getAppId(packageName);
            int buid = BUserHandle.getUid(userId, appId);
            Map<String, ProcessRecord> processRecordMap = mProcessMap.get(buid);
            if (processRecordMap == null) {
                return null;
            }
            return processRecordMap.get(processName);
        }
    }

    public void killAllByPackageName(String packageName) {
        synchronized (mProcessLock) {
            synchronized (mPidsSelfLocked) {
                List<ProcessRecord> tmp = new ArrayList<>(mPidsSelfLocked);
                int appId = BPackageManagerService.get().getAppId(packageName);
                for (ProcessRecord processRecord : mPidsSelfLocked) {
                    int appId1 = BUserHandle.getAppId(processRecord.buid);
                    if (appId == appId1) {
                        mProcessMap.remove(processRecord.buid);
                        tmp.remove(processRecord);
                        processRecord.kill();
                        // Return to pool
                        if (processRecord.bpid != -1) {
                            sFreeProcessPool.offer(processRecord.bpid);
                        }
                    }
                }
                mPidsSelfLocked.clear();
                mPidsSelfLocked.addAll(tmp);
            }
        }
    }

    public void killPackageAsUser(String packageName, int userId) {
        synchronized (mProcessLock) {
            int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
            Map<String, ProcessRecord> process = mProcessMap.get(buid);
            if (process == null) {
                return;
            }
            for (ProcessRecord value : process.values()) {
                value.kill();
                mPidsSelfLocked.remove(value);
                // Return to pool
                if (value.bpid != -1) {
                    sFreeProcessPool.offer(value.bpid);
                }
            }
            mProcessMap.remove(buid);
        }
    }

    public List<ProcessRecord> getPackageProcessAsUser(String packageName, int userId) {
        synchronized (mProcessLock) {
            int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
            Map<String, ProcessRecord> process = mProcessMap.get(buid);
            if (process == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(process.values());
        }
    }

    public int getBUidByPidOrPackageName(int pid, String packageName) {
        synchronized (mProcessLock) {
            ProcessRecord callingProcess = BProcessManagerService.get().findProcessByPid(pid);
            if (callingProcess == null) {
                return BPackageManagerService.get().getAppId(packageName);
            }
            return BUserHandle.getAppId(callingProcess.buid);
        }
    }

    public int getUserIdByCallingPid(int callingPid) {
        synchronized (mProcessLock) {
            ProcessRecord callingProcess = BProcessManagerService.get().findProcessByPid(callingPid);
            if (callingProcess == null) {
                return 0;
            }
            return callingProcess.userId;
        }
    }

    public ProcessRecord findProcessByPid(int pid) {
        synchronized (mPidsSelfLocked) {
            for (ProcessRecord processRecord : mPidsSelfLocked) {
                if (processRecord.pid == pid) {
                    return processRecord;
                }
            }
            return null;
        }
    }

    private static String getProcessName(Context context, int pid) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            if (processes != null) {
                for (ActivityManager.RunningAppProcessInfo info : processes) {
                    if (info.pid == pid) {
                        return info.processName;
                    }
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "Error getting process name", e);
        }
        return null;
    }

    public static int getPid(Context context, String processName) {
        if (processName == null) return -1;
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            if (runningAppProcesses != null) {
                for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                    if (processName.equals(runningAppProcess.processName)) {
                        return runningAppProcess.pid;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void createProc(ProcessRecord record) {
        File cmdline = new File(BEnvironment.getProcDir(record.bpid), "cmdline");
        try {
            FileUtils.writeToFile(record.processName.getBytes(), cmdline);
        } catch (IOException ignored) {
        }
    }

    private static void removeProc(ProcessRecord record) {
        FileUtils.deleteDir(BEnvironment.getProcDir(record.bpid));
    }
    
    // Replenish process pool
    public static void replenishPool() {
        if (sFreeProcessPool.size() < PROCESS_POOL_SIZE / 2) {
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    while (sFreeProcessPool.size() < PROCESS_POOL_SIZE) {
                        int bpid = getUsingBPidL();
                        if (bpid != -1 && !sFreeProcessPool.contains(bpid)) {
                            sFreeProcessPool.offer(bpid);
                        }
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Pool replenish error", e);
                }
            }).start();
        }
    }

    @Override
    public void systemReady() {
        FileUtils.deleteDir(BEnvironment.getProcDir());
        replenishPool();
    }
}
