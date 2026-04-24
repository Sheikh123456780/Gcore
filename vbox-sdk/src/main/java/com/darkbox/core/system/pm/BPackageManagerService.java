package com.darkbox.core.system.pm;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.os.Bundle;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.darkbox.VBoxCore;
import com.darkbox.core.GmsCore;
import com.darkbox.core.env.BEnvironment;
import com.darkbox.core.system.BProcessManagerService;
import com.darkbox.core.system.ISystemService;
import com.darkbox.core.system.ProcessRecord;
import com.darkbox.core.system.user.BUserHandle;
import com.darkbox.core.system.user.BUserInfo;
import com.darkbox.core.system.user.BUserManagerService;
import com.darkbox.entity.pm.InstallOption;
import com.darkbox.entity.pm.InstallResult;
import com.darkbox.entity.pm.InstalledPackage;
import com.darkbox.utils.AbiUtils;
import com.darkbox.utils.FileUtils;
import com.darkbox.utils.PermissionUtils;
import com.darkbox.utils.Slog;
import com.darkbox.utils.compat.PackageParserCompat;
import com.darkbox.utils.compat.XposedParserCompat;

import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_UNAWARE;

public class BPackageManagerService extends IBPackageManagerService.Stub implements ISystemService {
    public static final String TAG = "BPackageManagerService";
    public static BPackageManagerService sService = new BPackageManagerService();
    private final Settings mSettings = new Settings();
    private final ComponentResolver mComponentResolver;
    private static final BUserManagerService sUserManager = BUserManagerService.get();
    private final List<PackageMonitor> mPackageMonitors = new ArrayList<>();
    private final HashMap<String, BPackage.Permission> mPermissions = new HashMap<>();

    final Map<String, BPackageSettings> mPackages = mSettings.mPackages;
    private final Map<String, String[]> mDangerousPermissions = new HashMap<>();

    final Object mInstallLock = new Object();
    
    // Cache for fast lookup
    private final ConcurrentHashMap<String, PackageInfo> mPackageInfoCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ApplicationInfo> mAppInfoCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> mInstalledCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Intent> mLaunchIntentCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ResolveInfo> mResolveInfoCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 60000;
    private final Handler mCacheHandler = new Handler(Looper.getMainLooper());

    public static BPackageManagerService get() {
        return sService;
    }

    public BPackageManagerService() {
        mComponentResolver = new ComponentResolver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        VBoxCore.getContext().registerReceiver(mPackageChangedHandler, filter);
    }

    private final BroadcastReceiver mPackageChangedHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    clearAllCaches();
                    mSettings.scanPackage();
                }
            }
        }
    };
    
    private void clearAllCaches() {
        mPackageInfoCache.clear();
        mAppInfoCache.clear();
        mInstalledCache.clear();
        mLaunchIntentCache.clear();
        mResolveInfoCache.clear();
    }
    
    private ResolveInfo chooseBestActivity(Intent intent, String resolvedType, int flags, List<ResolveInfo> query) {
        if (query != null) {
            final int N = query.size();
            if (N == 1) {
                return query.get(0);
            } else if (N > 1) {
                ResolveInfo r0 = query.get(0);
                ResolveInfo r1 = query.get(1);
                if (r0.priority != r1.priority || r0.preferredOrder != r1.preferredOrder || r0.isDefault != r1.isDefault) {
                    return query.get(0);
                }
            }
        }
        return null;
    }
    
    // Add missing resolveContentProvider method
    @Override
    public ProviderInfo resolveContentProvider(String authority, int flags, int userId) throws RemoteException {
        if (!sUserManager.exists(userId)) return null;
        return mComponentResolver.queryProvider(authority, flags, userId);
    }
    
    // Add missing querySyncSetting method if needed
    @Override
    public Bundle querySyncSetting(String authority, int userId) throws RemoteException {
        Bundle result = new Bundle();
        result.putBoolean("syncable", true);
        return result;
    }
    
    public Intent getLaunchIntentForPackage(String packageName, int userId) {
        String cacheKey = packageName + "_" + userId;
        Intent cached = mLaunchIntentCache.get(cacheKey);
        if (cached != null) {
            return new Intent(cached);
        }
        
        PackageInfo pkgInfo = getPackageInfo(packageName, 0, userId);
        if (pkgInfo == null) {
            return null;
        }
        
        Intent launchIntent = null;
        if (pkgInfo.activities != null && pkgInfo.activities.length > 0) {
            for (ActivityInfo activity : pkgInfo.activities) {
                if (Intent.ACTION_MAIN.equals(activity.name)) {
                    launchIntent = new Intent();
                    launchIntent.setComponent(new ComponentName(packageName, activity.name));
                    launchIntent.setAction(Intent.ACTION_MAIN);
                    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                }
            }
        }
        
        if (launchIntent != null) {
            mLaunchIntentCache.put(cacheKey, launchIntent);
            mCacheHandler.postDelayed(() -> mLaunchIntentCache.remove(cacheKey), CACHE_TTL);
        }
        return launchIntent;
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        if (Objects.equals(packageName, VBoxCore.getHostPkg())) {
            try {
                return VBoxCore.getPackageManager().getApplicationInfo(packageName, flags);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        String cacheKey = packageName + "_" + flags + "_" + userId;
        ApplicationInfo cached = mAppInfoCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        flags = updateFlags(flags, userId);
        synchronized (mPackages) {
            BPackageSettings ps = mPackages.get(packageName);
            if (ps != null) {
                BPackage p = ps.pkg;
                ApplicationInfo result = PackageManagerCompat.generateApplicationInfo(p, flags, ps.readUserState(userId), userId);
                if (result != null) {
                    mAppInfoCache.put(cacheKey, result);
                    mCacheHandler.postDelayed(() -> mAppInfoCache.remove(cacheKey), CACHE_TTL);
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public ResolveInfo resolveService(Intent intent, int flags, String resolvedType, int userId) {
        if (!sUserManager.exists(userId)) return null;
        List<ResolveInfo> query = queryIntentServicesInternal(intent, resolvedType, flags, userId);
        if (query != null && query.size() >= 1) {
            return query.get(0);
        }
        return null;
    }

    private List<ResolveInfo> queryIntentServicesInternal(Intent intent, String resolvedType, int flags, int userId) {
        ComponentName comp = intent.getComponent();
        if (comp == null && intent.getSelector() != null) {
            intent = intent.getSelector();
            comp = intent.getComponent();
        }
        if (comp != null) {
            final List<ResolveInfo> list = new ArrayList<>(1);
            final ServiceInfo si = getServiceInfo(comp, flags, userId);
            if (si != null) {
                final ResolveInfo ri = new ResolveInfo();
                ri.serviceInfo = si;
                list.add(ri);
            }
            return list;
        }

        synchronized (mPackages) {
            String pkgName = intent.getPackage();
            if (pkgName != null) {
                BPackageSettings bPackageSettings = mPackages.get(pkgName);
                if (bPackageSettings != null) {
                    final BPackage pkg = bPackageSettings.pkg;
                    return mComponentResolver.queryServices(intent, resolvedType, flags, pkg.services, userId);
                }
            } else {
                return mComponentResolver.queryServices(intent, resolvedType, flags, userId);
            }
            return Collections.emptyList();
        }
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags, String resolvedType, int userId) {
        if (!sUserManager.exists(userId)) return null;
        List<ResolveInfo> resolves = queryIntentActivities(intent, resolvedType, flags, userId);
        return chooseBestActivity(intent, resolvedType, flags, resolves);
    }

    @Override
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        List<ResolveInfo> resolves = queryIntentActivities(intent, resolvedType, flags, userId);
        return chooseBestActivity(intent, resolvedType, flags, resolves);
    }

    private List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        ComponentName comp = intent.getComponent();
        if (comp == null && intent.getSelector() != null) {
            intent = intent.getSelector();
            comp = intent.getComponent();
        }

        if (comp != null) {
            final List<ResolveInfo> list = new ArrayList<>(1);
            final ActivityInfo ai = getActivity(comp, flags, userId);
            if (ai != null) {
                final ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                list.add(ri);
                return list;
            }
        }

        synchronized (mPackages) {
            return mComponentResolver.queryActivities(intent, resolvedType, flags, userId);
        }
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags, int userId) {
        final String resolvedType = intent.resolveTypeIfNeeded(VBoxCore.getContext().getContentResolver());
        return this.queryIntentServicesInternal(intent, resolvedType, flags, userId);
    }

    private ActivityInfo getActivity(ComponentName component, int flags, int userId) {
        flags = updateFlags(flags, userId);
        synchronized (mPackages) {
            BPackage.Activity a = mComponentResolver.getActivity(component);
            if (a != null) {
                BPackageSettings ps = mSettings.mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateActivityInfo(a, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        if (Objects.equals(packageName, VBoxCore.getHostPkg())) {
            try {
                return VBoxCore.getPackageManager().getPackageInfo(packageName, flags);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        String cacheKey = packageName + "_" + flags + "_" + userId;
        PackageInfo cached = mPackageInfoCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        flags = updateFlags(flags, userId);
        synchronized (mPackages) {
            BPackageSettings ps = mPackages.get(packageName);
            if (ps != null) {
                PackageInfo result = PackageManagerCompat.generatePackageInfo(ps, flags, ps.readUserState(userId), userId);
                if (result != null) {
                    mPackageInfoCache.put(cacheKey, result);
                    mCacheHandler.postDelayed(() -> mPackageInfoCache.remove(cacheKey), CACHE_TTL);
                }
                return result;
            }
        }
        return null;
    }
    
    @Override
    public ServiceInfo getServiceInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        synchronized (mPackages) {
            BPackage.Service s = mComponentResolver.getService(component);
            if (s != null) {
                BPackageSettings ps = mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateServiceInfo(s, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        synchronized (mPackages) {
            BPackage.Activity a = mComponentResolver.getReceiver(component);
            if (a != null) {
                BPackageSettings ps = mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateActivityInfo(a, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        synchronized (mPackages) {
            BPackage.Activity a = mComponentResolver.getActivity(component);
            if (a != null) {
                BPackageSettings ps = mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateActivityInfo(a, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    @Override
    public ProviderInfo getProviderInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) return null;
        synchronized (mPackages) {
            BPackage.Provider p = mComponentResolver.getProvider(component);
            if (p != null) {
                BPackageSettings ps = mPackages.get(component.getPackageName());
                if (ps == null) return null;
                return PackageManagerCompat.generateProviderInfo(p, flags, ps.readUserState(userId), userId);
            }
        }
        return null;
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return getInstalledApplicationsListInternal(flags, userId, Binder.getCallingUid());
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        if (!sUserManager.exists(userId)) return Collections.emptyList();

        synchronized (mPackages) {
            ArrayList<PackageInfo> list = new ArrayList<>(mPackages.size());
            for (BPackageSettings ps : mPackages.values()) {
                PackageInfo pi = getPackageInfo(ps.pkg.packageName, flags, userId);
                if (pi != null) {
                    list.add(pi);
                }
            }
            return new ArrayList<>(list);
        }
    }

    private List<ApplicationInfo> getInstalledApplicationsListInternal(int flags, int userId, int callingUid) {
        if (!sUserManager.exists(userId)) return Collections.emptyList();
        synchronized (mPackages) {
            ArrayList<ApplicationInfo> list = new ArrayList<>(mPackages.size());
            for (BPackageSettings ps : mPackages.values()) {
                ApplicationInfo ai = PackageManagerCompat.generateApplicationInfo(ps.pkg, flags, ps.readUserState(userId), userId);
                if (ai != null) {
                    list.add(ai);
                }
            }
            return list;
        }
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags, String resolvedType, int userId) throws RemoteException {
        return queryIntentActivities(intent, resolvedType, flags, userId);
    }

    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, String resolvedType, int userId) throws RemoteException {
        if (!sUserManager.exists(userId)) return Collections.emptyList();

        ComponentName comp = intent.getComponent();
        if (comp == null && intent.getSelector() != null) {
            intent = intent.getSelector();
            comp = intent.getComponent();
        }
        if (comp != null) {
            final List<ResolveInfo> list = new ArrayList<>(1);
            final ActivityInfo ai = getReceiverInfo(comp, flags, userId);
            if (ai != null) {
                ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                list.add(ri);
            }
            return list;
        }

        synchronized (mPackages) {
            String pkgName = intent.getPackage();
            BPackageSettings bPackageSettings = mPackages.get(pkgName);
            if (bPackageSettings != null) {
                final BPackage pkg = bPackageSettings.pkg;
                return mComponentResolver.queryReceivers(intent, resolvedType, flags, pkg.receivers, userId);
            } else {
                return mComponentResolver.queryReceivers(intent, resolvedType, flags, userId);
            }
        }
    }

    @Override
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags, int userId) throws RemoteException {
        if (!sUserManager.exists(userId)) return Collections.emptyList();

        List<ProviderInfo> providers = new ArrayList<>();
        if (TextUtils.isEmpty(processName))
            return providers;
        providers.addAll(mComponentResolver.queryProviders(processName, null, flags, userId));
        return providers;
    }

    @Override
    public InstallResult installPackageAsUser(String file, InstallOption option, int userId) {
        synchronized (mInstallLock) {
            return installPackageAsUserLocked(file, option, userId);
        }
    }

    @Override
    public void uninstallPackageAsUser(String packageName, int userId) throws RemoteException {
        synchronized (mInstallLock) {
            synchronized (mPackages) {
                BPackageSettings ps = mPackages.get(packageName);
                if (ps == null) return;
                if (ps.installOption.isFlag(InstallOption.FLAG_XPOSED) && userId != BUserHandle.USER_XPOSED) {
                    return;
                }
                if (!isInstalled(packageName, userId)) {
                    return;
                }
                boolean removeApp = ps.getUserState().size() <= 1;
                BProcessManagerService.get().killPackageAsUser(packageName, userId);
                BPackageInstallerService.get().uninstallPackageAsUser(ps, removeApp, userId);

                if (removeApp) {
                    mSettings.removePackage(packageName);
                    mComponentResolver.removeAllComponents(ps.pkg);
                } else {
                    ps.removeUser(userId);
                    ps.save();
                }
                clearPackageCache(packageName, userId);
                onPackageUninstalled(packageName, removeApp, userId);
            }
        }
    }
    
    private void clearPackageCache(String packageName, int userId) {
        mPackageInfoCache.clear();
        mAppInfoCache.clear();
        mInstalledCache.clear();
        mLaunchIntentCache.clear();
        mResolveInfoCache.clear();
    }

    @Override
    public void uninstallPackage(String packageName) {
        synchronized (mInstallLock) {
            synchronized (mPackages) {
                BPackageSettings ps = mPackages.get(packageName);
                if (ps == null) return;
                BProcessManagerService.get().killAllByPackageName(packageName);
                if (ps.installOption.isFlag(InstallOption.FLAG_XPOSED)) {
                    for (BUserInfo user : BUserManagerService.get().getAllUsers()) {
                        BPackageInstallerService.get().uninstallPackageAsUser(ps, true, user.id);
                        onPackageUninstalled(packageName, true, user.id);
                    }
                } else {
                    for (Integer userId : ps.getUserIds()) {
                        BPackageInstallerService.get().uninstallPackageAsUser(ps, true, userId);
                        onPackageUninstalled(packageName, true, userId);
                    }
                }
                mSettings.removePackage(packageName);
                mComponentResolver.removeAllComponents(ps.pkg);
                clearAllCaches();
            }
        }
    }

    @Override
    public void clearPackage(String packageName, int userId) {
        if (!isInstalled(packageName, userId)) {
            return;
        }
        BProcessManagerService.get().killPackageAsUser(packageName, userId);
        BPackageSettings ps = mPackages.get(packageName);
        if (ps == null) return;
        BPackageInstallerService.get().clearPackage(ps, userId);
    }

    @Override
    public void stopPackage(String packageName, int userId) {
        BProcessManagerService.get().killPackageAsUser(packageName, userId);
    }
    
    @Override
    public boolean isAppRunning(String packageName, int userId) {
        ActivityManager activityManager = (ActivityManager) VBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        if (processes == null) return false;

        for (ActivityManager.RunningAppProcessInfo process : processes) {
            if (process.pkgList != null && Arrays.asList(process.pkgList).contains(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteUser(int userId) throws RemoteException {
        synchronized (mPackages) {
            for (BPackageSettings ps : mPackages.values()) {
                uninstallPackageAsUser(ps.pkg.packageName, userId);
            }
        }
    }

    @Override
    public boolean isInstalled(String packageName, int userId) {
        if (!sUserManager.exists(userId)) return false;
        
        String cacheKey = packageName + "_" + userId;
        Boolean cached = mInstalledCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        synchronized (mPackages) {
            BPackageSettings ps = mPackages.get(packageName);
            if (ps == null) {
                mInstalledCache.put(cacheKey, false);
                return false;
            }
            boolean result = ps.getInstalled(userId);
            mInstalledCache.put(cacheKey, result);
            return result;
        }
    }

    @Override
    public List<InstalledPackage> getInstalledPackagesAsUser(int userId) {
        if (!sUserManager.exists(userId)) return Collections.emptyList();
        synchronized (mPackages) {
            List<InstalledPackage> installedPackages = new ArrayList<>();
            for (BPackageSettings ps : mPackages.values()) {
                if (ps.getInstalled(userId)) {
                    InstalledPackage installedPackage = new InstalledPackage();
                    installedPackage.userId = userId;
                    installedPackage.packageName = ps.pkg.packageName;
                    installedPackages.add(installedPackage);
                }
            }
            return installedPackages;
        }
    }

    @Override
    public String[] getPackagesForUid(int uid, int userId) throws RemoteException {
        if (!sUserManager.exists(userId)) return new String[]{};
        synchronized (mPackages) {
            List<String> packages = new ArrayList<>();
            for (BPackageSettings ps : mPackages.values()) {
                String packageName = ps.pkg.packageName;
                if (ps.getInstalled(userId) && getAppId(packageName) == uid) {
                    packages.add(packageName);
                }
            }
            if (packages.isEmpty()) {
                ProcessRecord processByPid = BProcessManagerService.get().findProcessByPid(getCallingPid());
                if (processByPid != null) {
                    packages.add(processByPid.getPackageName());
                }
            }
            return packages.toArray(new String[]{});
        }
    }

    public int checkUidPermission(String permission, int uid, String packageName) throws RemoteException {
        PermissionInfo info = getPermissionInfo(permission, 0);
        if (info != null) {
            return PackageManager.PERMISSION_GRANTED;
        }
        return VBoxCore.getPackageManager().checkPermission(permission, packageName);
    }

    @Override
    public int checkPermission(String permName, String pkgName, int userId) throws RemoteException {
        if ("android.permission.INTERACT_ACROSS_USERS".equals(permName) || "android.permission.INTERACT_ACROSS_USERS_FULL".equals(permName)) {
            return PackageManager.PERMISSION_DENIED;
        }
        PermissionInfo permissionInfo = getPermissionInfo(permName, 0);
        if (permissionInfo != null) {
            return PackageManager.PERMISSION_GRANTED;
        }
        return VBoxCore.getPackageManager().checkPermission(permName, pkgName);
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws RemoteException {
        synchronized (mPackages) {
            BPackage.Permission p = mPermissions.get(name);
            if (p != null) {
                return new PermissionInfo(p.info);
            }
        }
        return null;
    }

    private InstallResult installPackageAsUserLocked(String file, InstallOption option, int userId) {
        InstallResult result = new InstallResult();
        File apkFile = null;
        try {
            if (!sUserManager.exists(userId)) {
                sUserManager.createUser(userId);
            }
            if (option.isFlag(InstallOption.FLAG_URI_FILE)) {
                apkFile = new File(BEnvironment.getCacheDir(), UUID.randomUUID().toString() + ".apk");
                InputStream inputStream = VBoxCore.getContext().getContentResolver().openInputStream(Uri.parse(file));
                FileUtils.copyFile(inputStream, apkFile);
            } else {
                apkFile = new File(file);
            }

            if (option.isFlag(InstallOption.FLAG_XPOSED) && userId != BUserHandle.USER_XPOSED) {
                return new InstallResult().installError("Please install the XP module in XP module management");
            }
            if (option.isFlag(InstallOption.FLAG_XPOSED) && !XposedParserCompat.isXPModule(apkFile.getAbsolutePath())) {
                return new InstallResult().installError("not a XP module");
            }

            PackageInfo packageArchiveInfo = VBoxCore.getPackageManager().getPackageArchiveInfo(apkFile.getAbsolutePath(), 0);
            if (packageArchiveInfo == null) {
                return result.installError("getPackageArchiveInfo error");
            }

            boolean support = AbiUtils.isSupport(apkFile);
            if (!support) {
                return result.installError(packageArchiveInfo.packageName, "ABI not supported");
            }
            
            PackageParser.Package aPackage = parserApk(apkFile.getAbsolutePath());
            if (aPackage == null) {
                return result.installError("parser apk error");
            }
            result.packageName = aPackage.packageName;

            if (option.isFlag(InstallOption.FLAG_SYSTEM)) {
                aPackage.applicationInfo = VBoxCore.getPackageManager().getPackageInfo(aPackage.packageName, 0).applicationInfo;
            }
            BPackageSettings bPackageSettings = mSettings.getPackageLPw(aPackage.packageName, aPackage, option);

            BProcessManagerService.get().killPackageAsUser(aPackage.packageName, userId);

            int i = BPackageInstallerService.get().installPackageAsUser(bPackageSettings, userId);
            if (i < 0) {
                return result.installError("install apk error");
            }
            synchronized (mPackages) {
                bPackageSettings.setInstalled(true, userId);
                bPackageSettings.save();
            }
            mComponentResolver.removeAllComponents(bPackageSettings.pkg);
            mComponentResolver.addAllComponents(bPackageSettings.pkg);
            mSettings.scanPackage(aPackage.packageName);
            
            onPackageInstalled(bPackageSettings.pkg.packageName, userId);
            result.success = true;
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            return result.installError(t.getMessage());
        } finally {
            if (apkFile != null && option.isFlag(InstallOption.FLAG_URI_FILE)) {
                FileUtils.deleteDir(apkFile);
            }
        }
    }

    private PackageParser.Package parserApk(String file) {
        try {
            PackageParser parser = PackageParserCompat.createParser(new File(file));
            PackageParser.Package aPackage = PackageParserCompat.parsePackage(parser, new File(file), 0);
            PackageParserCompat.collectCertificates(parser, aPackage, 0);
            return aPackage;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    static String fixProcessName(String defProcessName, String processName) {
        if (processName == null) {
            return defProcessName;
        }
        return processName;
    }

    private int updateFlags(int flags, int userId) {
        if ((flags & (PackageManager.MATCH_DIRECT_BOOT_UNAWARE | PackageManager.MATCH_DIRECT_BOOT_AWARE)) == 0) {
            flags |= PackageManager.MATCH_DIRECT_BOOT_AWARE | MATCH_DIRECT_BOOT_UNAWARE;
        }
        return flags;
    }

    public int getAppId(String packageName) {
        BPackageSettings bPackageSettings = mPackages.get(packageName);
        if (bPackageSettings != null)
            return bPackageSettings.appId;
        return -1;
    }

    Settings getSettings() {
        return mSettings;
    }

    public void addPackageMonitor(PackageMonitor monitor) {
        mPackageMonitors.add(monitor);
    }

    public void removePackageMonitor(PackageMonitor monitor) {
        mPackageMonitors.remove(monitor);
    }

    void onPackageUninstalled(String packageName, boolean isRemove, int userId) {
        for (PackageMonitor packageMonitor : mPackageMonitors) {
            packageMonitor.onPackageUninstalled(packageName, isRemove, userId);
        }
        Slog.d(TAG, "onPackageUninstalled: " + packageName + ", userId: " + userId);
    }

    void onPackageInstalled(String packageName, int userId) {
        for (PackageMonitor packageMonitor : mPackageMonitors) {
            packageMonitor.onPackageInstalled(packageName, userId);
        }
        Slog.d(TAG, "onPackageInstalled: " + packageName + ", userId: " + userId);
    }

    public BPackageSettings getBPackageSetting(String packageName) {
        return mPackages.get(packageName);
    }

    public List<BPackageSettings> getBPackageSettings() {
        return new ArrayList<>(mPackages.values());
    }

    @Override
    public void systemReady() {
        mSettings.scanPackage();
        for (BPackageSettings value : mPackages.values()) {
            mComponentResolver.removeAllComponents(value.pkg);
            mComponentResolver.addAllComponents(value.pkg);
        }
    }

    public String[] getDangerousPermissions(String packageName) {
        synchronized (mDangerousPermissions) {
            return mDangerousPermissions.get(packageName);
        }
    }

    public void analyzePackageLocked(BPackageSettings bPackageSettings) {
        synchronized (mDangerousPermissions) {
            mDangerousPermissions.put(bPackageSettings.pkg.packageName, PermissionUtils.findDangerousPermissions(bPackageSettings.pkg.requestedPermissions));
        }
    }
}
