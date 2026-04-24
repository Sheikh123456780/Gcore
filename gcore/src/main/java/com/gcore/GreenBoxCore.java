package com.gcore;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import black.android.app.BRActivityThread;
import black.android.os.BRUserHandle;

import com.gcore.app.LauncherActivity;
import com.gcore.app.configuration.AppLifecycleCallback;
import com.gcore.app.configuration.ClientConfiguration;
import com.gcore.core.GmsCore;
import com.gcore.core.env.BEnvironment;
import com.gcore.core.system.DaemonService;
import com.gcore.core.system.ServiceManager;
import com.gcore.core.system.user.BUserInfo;
import com.gcore.entity.pm.InstallOption;
import com.gcore.entity.pm.InstallResult;
import com.gcore.fake.delegate.ContentProviderDelegate;
import com.gcore.fake.frameworks.BActivityManager;
import com.gcore.fake.frameworks.BJobManager;
import com.gcore.fake.frameworks.BPackageManager;
import com.gcore.fake.frameworks.BStorageManager;
import com.gcore.fake.frameworks.BUserManager;
import com.gcore.fake.hook.HookManager;
import com.gcore.proxy.ProxyManifest;
import com.gcore.utils.CrashMonitor;
import com.gcore.utils.FileUtils;
import com.gcore.utils.Reflection;
import com.gcore.utils.ShellUtils;
import com.gcore.utils.compat.BuildCompat;
import com.gcore.utils.compat.BundleCompat;
import com.gcore.utils.provider.ProviderCall;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
@SuppressLint("StaticFieldLeak")
public class GreenBoxCore extends ClientConfiguration {

    static {
        try {
            CrashMonitor.initialize();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    private static final GreenBoxCore sGreenBoxCore = new GreenBoxCore();
    private static Context sContext;
    private ProcessType mProcessType;
    private final Map<String, IBinder> mServices = new HashMap<>();
    private Thread.UncaughtExceptionHandler mExceptionHandler;
    private ClientConfiguration mClientConfiguration;
    private final List<AppLifecycleCallback> mAppLifecycleCallbacks = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final int mHostUid = Process.myUid();
    private final int mHostUserId = BRUserHandle.get().myUserId();

    public static GreenBoxCore get() {
        return sGreenBoxCore;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public static PackageManager getPackageManager() {
        return sContext.getPackageManager();
    }

    public static String getHostPkg() {
        return get().getHostPackageName();
    }

    public static int getHostUid() {
        return get().mHostUid;
    }

    public static int getHostUserId() {
        return get().mHostUserId;
    }

    public static Context getContext() {
        return sContext;
    }

    public Thread.UncaughtExceptionHandler getExceptionHandler() {
        return mExceptionHandler;
    }

    public void setExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        mExceptionHandler = exceptionHandler;
    }

    public void doAttachBaseContext(Context context, ClientConfiguration clientConfiguration) {
    if (clientConfiguration == null) {
        throw new IllegalArgumentException("ClientConfiguration is null!");
    }
    Reflection.unseal(context);
    sContext = context;
    
    // ========== ADD THIS FIX ==========
    // Fix BEnvironment directory to point to app's data directory
    try {
        File correctDir = new File(sContext.getFilesDir().getParent());
        // Use reflection to set the InternalDirectory
        java.lang.reflect.Field field = BEnvironment.class.getDeclaredField("InternalDirectory");
        field.setAccessible(true);
        // Remove final modifier
        java.lang.reflect.Field modifiersField = java.lang.reflect.Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        field.set(null, correctDir);
        // Also call setInternalDirectory if available
        try {
            Method setMethod = BEnvironment.class.getDeclaredMethod("setInternalDirectory", File.class);
            setMethod.invoke(null, correctDir);
        } catch (Exception e) {}
    } catch (Exception e) {
        e.printStackTrace();
    }
    // ========== END FIX ==========
    
    mClientConfiguration = clientConfiguration;
    initNotificationManager();
    

        String processName = getProcessName(getContext());
        if (processName.equals(GreenBoxCore.getHostPkg())) {
            mProcessType = ProcessType.Main;
            startLogcat();
        } else if (processName.endsWith(getContext().getString(R.string.green_box_service_name))) {
            mProcessType = ProcessType.Server;
        } else {
            mProcessType = ProcessType.BAppClient;
        }
        if (GreenBoxCore.get().isBlackProcess()) {
            BEnvironment.load();
        }
        if (isServerProcess()) {
            if (clientConfiguration.isEnableDaemonService()) {
                Intent intent = new Intent(getContext(), DaemonService.class);
                if (BuildCompat.isOreo_MR1()) {
                    getContext().startForegroundService(intent);
                } else {
                    getContext().startService(intent);
                }
            }
        }
        HookManager.get().init();
    }

    public void doCreate() {
        if (isBlackProcess()) {
            ContentProviderDelegate.init();
        }
        if (!isServerProcess()) {
            ServiceManager.initBlackManager();
        }
    }

    public static Object mainThread() {
        return BRActivityThread.get().currentActivityThread();
    }

    public void startActivity(Intent intent, int userId) {
        if (mClientConfiguration.isEnableLauncherActivity()) {
            LauncherActivity.launch(intent, userId);
        } else {
            getBActivityManager().startActivity(intent, userId);
        }
    }

    public static BJobManager getBJobManager() {
        return BJobManager.get();
    }

    public static BPackageManager getBPackageManager() {
        return BPackageManager.get();
    }

    public static BActivityManager getBActivityManager() {
        return BActivityManager.get();
    }

    public static BStorageManager getBStorageManager() {
        return BStorageManager.get();
    }

    public void launchApk(String packageName) {
        Intent launchIntent = getBPackageManager().getLaunchIntentForPackage(packageName, 0);
        if (launchIntent == null) {
            return;
        }
        startActivity(launchIntent, 0);
    }

    public boolean isInstalled(String packageName, int userId) {
        return getBPackageManager().isInstalled(packageName, userId);
    }

    public void uninstallPackageAsUser(String packageName, int userId) {
        getBPackageManager().uninstallPackageAsUser(packageName, userId);
    }

    public void uninstallPackage(String packageName) {
        getBPackageManager().uninstallPackage(packageName);
    }

    public InstallResult installPackageAsUser(String packageName, int userId) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            return getBPackageManager().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), userId);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new InstallResult().installError(e.getMessage());
        }
    }

    public InstallResult installPackageAsUser(File apk, int userId) {
        return getBPackageManager().installPackageAsUser(apk.getAbsolutePath(), InstallOption.installByStorage(), userId);
    }

    public InstallResult installPackageAsUser(Uri apk, int userId) {
        return getBPackageManager().installPackageAsUser(apk.toString(), InstallOption.installByStorage().makeUriFile(), userId);
    }

    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return getBPackageManager().getInstalledApplications(flags, userId);
    }

    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        return getBPackageManager().getInstalledPackages(flags, userId);
    }

    public void clearPackage(String packageName) {
        BPackageManager.get().clearPackage(packageName, 0);
    }

    public void stopPackage(String packageName) {
        BPackageManager.get().stopPackage(packageName, 0);
    }

    public List<BUserInfo> getUsers() {
        return BUserManager.get().getUsers();
    }

    public BUserInfo createUser(int userId) {
        return BUserManager.get().createUser(userId);
    }

    public void deleteUser(int userId) {
        BUserManager.get().deleteUser(userId);
    }

    public List<AppLifecycleCallback> getAppLifecycleCallbacks() {
        return mAppLifecycleCallbacks;
    }

    public void removeAppLifecycleCallback(AppLifecycleCallback appLifecycleCallback) {
        mAppLifecycleCallbacks.remove(appLifecycleCallback);
    }

    public void addAppLifecycleCallback(AppLifecycleCallback appLifecycleCallback) {
        mAppLifecycleCallbacks.add(appLifecycleCallback);
    }

    public boolean isSupportGms() {
        return GmsCore.isSupportGms();
    }

    public boolean isInstallGms(int userId) {
        return GmsCore.isInstalledGoogleService(userId);
    }

    public InstallResult installGms(int userId) {
        return GmsCore.installGApps(userId);
    }

    public boolean uninstallGms(int userId) {
        GmsCore.uninstallGApps(userId);
        return !GmsCore.isInstalledGoogleService(userId);
    }

    public IBinder getService(String name) {
        IBinder binder = mServices.get(name);
        if (binder != null && binder.isBinderAlive()) {
            return binder;
        }
        Bundle bundle = new Bundle();
        bundle.putString("_G_|_server_name_", name);
        Bundle vm = ProviderCall.callSafely(ProxyManifest.getBindProvider(), "VM", null, bundle);
        binder = BundleCompat.getBinder(vm, "_G_|_server_");
        mServices.put(name, binder);
        return binder;
    }

    private enum ProcessType {
        Server,
        BAppClient,
        Main,
    }

    public boolean isBlackProcess() {
        return mProcessType == ProcessType.BAppClient;
    }

    public boolean isMainProcess() {
        return mProcessType == ProcessType.Main;
    }

    public boolean isServerProcess() {
        return mProcessType == ProcessType.Server;
    }

    @Override
    public boolean isHideRoot() {
        return mClientConfiguration.isHideRoot();
    }

    @Override
    public String getHostPackageName() {
        return mClientConfiguration.getHostPackageName();
    }

    @Override
    public boolean requestInstallPackage(File file) {
        return mClientConfiguration.requestInstallPackage(file);
    }

    private void startLogcat() {
        new Thread(() -> {
            File file = new File(BEnvironment.getExternalUserDir() + "/Download", getContext().getPackageName() + "_logcat.txt");
            FileUtils.deleteDir(file);
            ShellUtils.execCommand("logcat -c", false);
            ShellUtils.execCommand("logcat -f " + file.getAbsolutePath(), false);
        }).start();
    }

    private static String getProcessName(Context context) {
        int pid = Process.myPid();
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }
        if (processName == null) {
            throw new RuntimeException("processName = null");
        }
        return processName;
    }

    public static boolean is64Bit() {
        if (BuildCompat.isM()) {
            return Process.is64Bit();
        } else {
            return Build.CPU_ABI.equals("arm64-v8a");
        }
    }

    private void initNotificationManager() {
        NotificationManager nm = (NotificationManager) GreenBoxCore.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ONE_ID = GreenBoxCore.getContext().getPackageName();
        if (BuildCompat.isOreo_MR1()) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, "GreenBoxCore", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(notificationChannel);
        }
    }
}
