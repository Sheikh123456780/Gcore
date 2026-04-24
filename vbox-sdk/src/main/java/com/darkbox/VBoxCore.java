package com.darkbox;

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
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import black.android.app.BRActivityThread;
import black.android.os.BRUserHandle;
import me.weishu.reflection.Reflection;

import com.darkbox.app.LauncherActivity;
import com.darkbox.app.configuration.AppLifecycleCallback;
import com.darkbox.app.configuration.ClientConfiguration;
import com.darkbox.core.GmsCore;
import com.darkbox.core.env.BEnvironment;
import com.darkbox.core.system.DaemonService;
import com.darkbox.core.system.ServiceManager;
import com.darkbox.core.system.BProcessManagerService;
import com.darkbox.core.system.user.BUserHandle;
import com.darkbox.core.system.user.BUserInfo;
import com.darkbox.entity.AppConfig;
import com.darkbox.entity.pm.InstallOption;
import com.darkbox.entity.pm.InstallResult;
import com.darkbox.entity.pm.InstalledModule;
import com.darkbox.fake.delegate.ContentProviderDelegate;
import com.darkbox.fake.frameworks.BActivityManager;
import com.darkbox.fake.frameworks.BJobManager;
import com.darkbox.fake.frameworks.BPackageManager;
import com.darkbox.fake.frameworks.BStorageManager;
import com.darkbox.fake.frameworks.BUserManager;
import com.darkbox.fake.frameworks.BXposedManager;
import com.darkbox.fake.hook.HookManager;
import com.darkbox.proxy.ProxyManifest;
import com.darkbox.utils.FileUtils;
import com.darkbox.utils.ShellUtils;
import com.darkbox.utils.Slog;
import com.darkbox.utils.compat.BuildCompat;
import com.darkbox.utils.compat.BundleCompat;
import com.darkbox.utils.compat.XposedParserCompat;
import com.darkbox.utils.provider.ProviderCall;
import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
@SuppressLint({"StaticFieldLeak", "NewApi"})
public class VBoxCore extends ClientConfiguration {
    public static final String TAG = "VBoxCore";

    private static final VBoxCore sVBoxCore = new VBoxCore();
    private static Context sContext;
    private ProcessType mProcessType;
    private final Map<String, IBinder> mServices = new HashMap<>();
    private Thread.UncaughtExceptionHandler mExceptionHandler;
    private ClientConfiguration mClientConfiguration;
    private final List<AppLifecycleCallback> mAppLifecycleCallbacks = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final int mHostUid = Process.myUid();
    private final int mHostUserId = BRUserHandle.get().myUserId();
    private AppConfig appConfig;
    
    // Intent cache for fast launch
    private static final Map<String, Intent> sIntentCache = new ConcurrentHashMap<>();
    
    // Pre-cached BGMI intent
    private static Intent sCachedBGMIIntent = null;
    
    // Local flags
    private static boolean sHideRoot = true;
    private static boolean sHideXposed = true;
    private static boolean sEnableDaemonService = false;
    
    public static VBoxCore get() {
        return sVBoxCore;
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

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public Thread.UncaughtExceptionHandler getExceptionHandler() {
        return mExceptionHandler;
    }

    public void setExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        mExceptionHandler = exceptionHandler;
    }
    
    public static void setHideRoot(boolean hideRoot) { 
        sHideRoot = hideRoot;
    }
    
    public static void setHideXposed(boolean hide) {
        sHideXposed = hide;
    }
    
    public static void setEnableDaemonService(boolean enableDaemonService) { 
        sEnableDaemonService = enableDaemonService;
    }
    
    public static boolean isHideRootFlag() {
        return sHideRoot;
    }
    
    public static boolean isHideXposedFlag() {
        return sHideXposed;
    }
    
    public static boolean isEnableDaemonServiceFlag() {
        return sEnableDaemonService;
    }

    public void doAttachBaseContext(Context context, ClientConfiguration clientConfiguration) {
        if (clientConfiguration == null) {
            throw new IllegalArgumentException("ClientConfiguration is null!");
        }
        
        Reflection.unseal(context);
        sContext = context;
        mClientConfiguration = clientConfiguration;
        
        // Disable slow operations
        // initNotificationManager();
        // startLogcat();

        String processName = getProcessName(getContext());
        if (processName.equals(VBoxCore.getHostPkg())) {
            mProcessType = ProcessType.Main;
        } else if (processName.endsWith(getContext().getString(R.string.black_box_service_name))) {
            mProcessType = ProcessType.Server;
        } else {
            mProcessType = ProcessType.BAppClient;
        }

        if (VBoxCore.get().isBlackProcess()) {
            BEnvironment.load();
        }
        
        if (isServerProcess()) {
            if (sEnableDaemonService) {
                Intent intent = new Intent();
                intent.setClass(getContext(), DaemonService.class);
                if (BuildCompat.isOreo_MR1()) {
                    getContext().startForegroundService(intent);
                } else {
                    getContext().startService(intent);
                }
            }
        }
        
        // Async hook init
        new Thread(() -> {
            try {
                HookManager.get().init();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void doCreate() {
        if (isBlackProcess()) {
            ContentProviderDelegate.init();
        }
        if (!isServerProcess()) {
            ServiceManager.initBlackManager();
        }
        
        // Pre-cache BGMI intent for fast launch
        preCacheBGMIIntent();
    }
    
    private void preCacheBGMIIntent() {
        if (sCachedBGMIIntent != null) return;
        
        new Thread(() -> {
            try {
                Thread.sleep(100);
                sCachedBGMIIntent = getBPackageManager().getLaunchIntentForPackage("com.pubg.imobile", 0);
                if (sCachedBGMIIntent != null) {
                    Slog.d(TAG, "BGMI intent pre-cached successfully");
                }
            } catch (Exception e) {
                Slog.e(TAG, "Failed to pre-cache BGMI intent", e);
            }
        }).start();
    }

    public static Object mainThread() {
        return BRActivityThread.get().currentActivityThread();
    }

    public void startActivity(Intent intent, int userId) {
        // Direct start - no LauncherActivity
        getBActivityManager().startActivity(intent, userId);
    }
    
    public void onBeforeMainLaunchApk(String packageName, int userid) {
        for (AppLifecycleCallback appLifecycleCallback : VBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.beforeMainLaunchApk(packageName, userid);
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
        try {
            // Use pre-cached intent for BGMI
            Intent launchIntent = null;
            if (packageName.equals("com.pubg.imobile") && sCachedBGMIIntent != null) {
                launchIntent = sCachedBGMIIntent;
            } else {
                // Use cache map for other apps
                String cacheKey = packageName + "_0";
                launchIntent = sIntentCache.get(cacheKey);
                if (launchIntent == null) {
                    launchIntent = getBPackageManager().getLaunchIntentForPackage(packageName, 0);
                    if (launchIntent != null) {
                        sIntentCache.put(cacheKey, launchIntent);
                    }
                }
            }
            
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(launchIntent, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public InstallResult installXPModule(File apk) {
        return getBPackageManager().installPackageAsUser(apk.getAbsolutePath(), InstallOption.installByStorage().makeXposed(), BUserHandle.USER_XPOSED);
    }

    public InstallResult installXPModule(Uri apk) {
        return getBPackageManager().installPackageAsUser(apk.toString(), InstallOption.installByStorage().makeXposed().makeUriFile(), BUserHandle.USER_XPOSED);
    }

    public InstallResult installXPModule(String packageName) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            String path = packageInfo.applicationInfo.sourceDir;
            return getBPackageManager().installPackageAsUser(path, InstallOption.installBySystem().makeXposed(), BUserHandle.USER_XPOSED);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new InstallResult().installError(e.getMessage());
        }
    }

    public void uninstallXPModule(String packageName) {
        uninstallPackage(packageName);
    }

    public boolean isXPEnable() {
        return BXposedManager.get().isXPEnable();
    }

    public void setXPEnable(boolean enable) {
        BXposedManager.get().setXPEnable(enable);
    }

    public boolean isXposedModule(File file) {
        return XposedParserCompat.isXPModule(file.getAbsolutePath());
    }

    public boolean isInstalledXposedModule(String packageName) {
        return isInstalled(packageName, BUserHandle.USER_XPOSED);
    }

    public boolean isModuleEnable(String packageName) {
        return BXposedManager.get().isModuleEnable(packageName);
    }

    public void setModuleEnable(String packageName, boolean enable) {
        BXposedManager.get().setModuleEnable(packageName, enable);
    }

    public List<InstalledModule> getInstalledXPModules() {
        return BXposedManager.get().getInstalledModules();
    }

    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return getBPackageManager().getInstalledApplications(flags, userId);
    }

    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        return getBPackageManager().getInstalledPackages(flags, userId);
    }

    public void clearPackage(String packageName, int userId) {
        BPackageManager.get().clearPackage(packageName, userId);
    }

    public void stopPackage(String packageName, int userId) {
        BPackageManager.get().stopPackage(packageName, userId);
    }
    
    public boolean isAppRunning(String packageName, int userId) {
        return getBPackageManager().isAppRunning(packageName, userId);
    }

    public List<BUserInfo> getUsers() {
        return BUserManager.get().getUsers();
    }

    public BUserInfo createUser(int userId) {
        return BUserManager.get().createUser(userId);
    }

    public ApplicationInfo getApplicationInfo(String packageName) {
        return BPackageManager.get().getApplicationInfo(packageName, 0, 0);
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
        bundle.putString("_V_|_server_name_", name);
        Bundle vm = ProviderCall.callSafely(ProxyManifest.getBindProvider(), "VM", null, bundle);
        binder = BundleCompat.getBinder(vm, "_V_|_server_");
        Slog.d(TAG, "getService: " + name + ", " + binder);
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
    public boolean setHideRoot() {
        return mClientConfiguration.setHideRoot();
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
        // Disabled
    }

    private static String getProcessName(Context context) {
        int myPid = Process.myPid();
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == myPid) {
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
        // Disabled
    }

    public boolean checkSelfPermission(String permission) {
        return getPackageManager().checkPermission(permission, getHostPackageName()) == 0;
    }
}
