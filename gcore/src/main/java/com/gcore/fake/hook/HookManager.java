package com.gcore.fake.hook;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.gcore.GreenBoxCore;
import com.gcore.fake.delegate.AppInstrumentation;
import com.gcore.fake.service.HCallbackProxy;
import com.gcore.fake.service.IAccessibilityManagerProxy;
import com.gcore.fake.service.IAccountManagerProxy;
import com.gcore.fake.service.IActivityClientProxy;
import com.gcore.fake.service.IActivityManagerProxy;
import com.gcore.fake.service.IActivityTaskManagerProxy;
import com.gcore.fake.service.IAlarmManagerProxy;
import com.gcore.fake.service.IAppOpsManagerProxy;
import com.gcore.fake.service.IAppWidgetManagerProxy;
import com.gcore.fake.service.IAutofillManagerProxy;
import com.gcore.fake.service.IBluetoothManagerProxy;
import com.gcore.fake.service.IClipboardManagerProxy;
import com.gcore.fake.service.IConnectivityManagerProxy;
import com.gcore.fake.service.IDeviceIdentifiersPolicyProxy;
import com.gcore.fake.service.IDevicePolicyManagerProxy;
import com.gcore.fake.service.IDisplayManagerProxy;
import com.gcore.fake.service.IFingerprintManagerProxy;
import com.gcore.fake.service.IGraphicsStatsProxy;
import com.gcore.fake.service.IJobServiceProxy;
import com.gcore.fake.service.ILauncherAppsProxy;
import com.gcore.fake.service.ILocaleManagerProxy;
import com.gcore.fake.service.ILocationManagerProxy;
import com.gcore.fake.service.IMediaRouterServiceProxy;
import com.gcore.fake.service.IMediaSessionManagerProxy;
import com.gcore.fake.service.INetworkManagementServiceProxy;
import com.gcore.fake.service.INotificationManagerProxy;
import com.gcore.fake.service.IPackageManagerProxy;
import com.gcore.fake.service.IPermissionManagerProxy;
import com.gcore.fake.service.IPersistentDataBlockServiceProxy;
import com.gcore.fake.service.IPhoneSubInfoProxy;
import com.gcore.fake.service.IPowerManagerProxy;
import com.gcore.fake.service.IShortcutManagerProxy;
import com.gcore.fake.service.IStorageManagerProxy;
import com.gcore.fake.service.IStorageStatsManagerProxy;
import com.gcore.fake.service.ITelephonyManagerProxy;
import com.gcore.fake.service.ITelephonyRegistryProxy;
import com.gcore.fake.service.IUserManagerProxy;
import com.gcore.fake.service.IVibratorServiceProxy;
import com.gcore.fake.service.IVpnManagerProxy;
import com.gcore.fake.service.IWifiManagerProxy;
import com.gcore.fake.service.IWifiScannerProxy;
import com.gcore.fake.service.IWindowManagerProxy;
import com.gcore.fake.service.context.ContentServiceStub;
import com.gcore.fake.service.context.RestrictionsManagerStub;
import com.gcore.utils.Slog;
import com.gcore.utils.compat.BuildCompat;

public class HookManager {

    public static final String TAG = "HookManager";

    private static final HookManager sHookManager = new HookManager();

    private final Map<Class<?>, IInjectHook> mInjectors = new HashMap<>();

    public static HookManager get() {
        return sHookManager;
    }

    public void init() {
        if (GreenBoxCore.get().isBlackProcess() || GreenBoxCore.get().isServerProcess()) {
            addInjector(new HCallbackProxy());
            addInjector(new IAccessibilityManagerProxy());
            addInjector(new IAccountManagerProxy());
            addInjector(new IActivityManagerProxy());
            addInjector(new IAlarmManagerProxy());
            addInjector(new IAppOpsManagerProxy());
            addInjector(new IAppWidgetManagerProxy());
            addInjector(new IBluetoothManagerProxy());
            addInjector(new IClipboardManagerProxy());
            addInjector(new IConnectivityManagerProxy());
            addInjector(new IDevicePolicyManagerProxy());
            addInjector(new IDisplayManagerProxy());
            addInjector(new IJobServiceProxy());
            addInjector(new ILauncherAppsProxy());
            addInjector(new ILocationManagerProxy());
            addInjector(new IMediaRouterServiceProxy());
            addInjector(new IMediaSessionManagerProxy());
            addInjector(new INetworkManagementServiceProxy());
            addInjector(new INotificationManagerProxy());
            addInjector(new IPackageManagerProxy());
            addInjector(new IPhoneSubInfoProxy());
            addInjector(new IPowerManagerProxy());
            addInjector(new IStorageManagerProxy());
            addInjector(new ITelephonyManagerProxy());
            addInjector(new ITelephonyRegistryProxy());
            addInjector(new IUserManagerProxy());
            addInjector(new IVibratorServiceProxy());
            addInjector(new IWifiManagerProxy());
            addInjector(new IWifiScannerProxy());
            addInjector(new IWindowManagerProxy());
            addInjector(new ContentServiceStub());
            addInjector(new RestrictionsManagerStub());
            addInjector(AppInstrumentation.get());

            // 16.0
            if (BuildCompat.isBaklava()) {
                addInjector(new IPersistentDataBlockServiceProxy());
            }
            // 14.0
            if (BuildCompat.isUpsideDownCake()) {
                addInjector(new ILocaleManagerProxy());
            }
            // 12.0
            if (BuildCompat.isS()) {
                addInjector(new IActivityClientProxy(null));
                addInjector(new IVpnManagerProxy());
            }
            // 11.0
            if (BuildCompat.isR()) {
                addInjector(new IActivityTaskManagerProxy());
                addInjector(new IPermissionManagerProxy());
            }
            // 10.0
            if (BuildCompat.isQ()) {
                addInjector(new IDeviceIdentifiersPolicyProxy());
            }
            // 8.1
            if (BuildCompat.isOreo_MR1()) {
                addInjector(new IAutofillManagerProxy());
                addInjector(new IStorageStatsManagerProxy());
            }
            // 8.0
            if (BuildCompat.isOreo()) {
                addInjector(new IShortcutManagerProxy());
            }
            // 7.0
            if (BuildCompat.isN()) {
                addInjector(new IFingerprintManagerProxy());
                addInjector(new IGraphicsStatsProxy());
            }
        }
        injectAll();
    }

    public void checkEnv(Class<?> clazz) {
        IInjectHook iInjectHook = mInjectors.get(clazz);
        if (iInjectHook != null && iInjectHook.isBadEnv()) {
            Log.d(TAG, "checkEnv: " + clazz.getSimpleName() + " is bad env");
            iInjectHook.injectHook();
        }
    }

    void addInjector(IInjectHook injectHook) {
        mInjectors.put(injectHook.getClass(), injectHook);
    }

    void injectAll() {
        for (IInjectHook value : mInjectors.values()) {
            try {
                Slog.d(TAG, "hook: " + value);
                value.injectHook();
            } catch (Exception e) {
                Slog.d(TAG, "hook error: " + value);
            }
        }
    }
}
