package com.darkbox.fake.hook;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.darkbox.VBoxCore;
import com.darkbox.fake.delegate.AppInstrumentation;

import com.darkbox.fake.service.IBluetoothManagerProxy;
import com.darkbox.fake.service.ISmsProxy;
import com.darkbox.fake.service.ISubProxy;
import com.darkbox.fake.service.HCallbackStub;
import com.darkbox.fake.service.IAccessibilityManagerProxy;
import com.darkbox.fake.service.IAccountManagerProxy;
import com.darkbox.fake.service.IActivityClientProxy;
import com.darkbox.fake.service.IActivityManagerProxy;
import com.darkbox.fake.service.IActivityTaskManagerProxy;
import com.darkbox.fake.service.IAlarmManagerProxy;
import com.darkbox.fake.service.IAppIntegrityManagerProxy;
import com.darkbox.fake.service.IAppOpsManagerProxy;
import com.darkbox.fake.service.IAppWidgetManagerProxy;
import com.darkbox.fake.service.IAudioManagerProxy;
import com.darkbox.fake.service.IAutofillManagerProxy;
import com.darkbox.fake.service.IBackupManagerProxy;
import com.darkbox.fake.service.IClipboardManagerProxy;
import com.darkbox.fake.service.IConnectivityManagerProxy;
import com.darkbox.fake.service.IContextHubServiceProxy;
import com.darkbox.fake.service.IDeviceIdentifiersPolicyProxy;
import com.darkbox.fake.service.IDevicePolicyManagerProxy;
import com.darkbox.fake.service.IDisplayManagerProxy;
import com.darkbox.fake.service.IFingerprintManagerProxy;
import com.darkbox.fake.service.IGraphicsStatsProxy;
import com.darkbox.fake.service.IJobServiceProxy;
import com.darkbox.fake.service.ILauncherAppsProxy;
import com.darkbox.fake.service.ILocaleManagerProxy;
import com.darkbox.fake.service.ILocationManagerProxy;
import com.darkbox.fake.service.IMediaRouterServiceProxy;
import com.darkbox.fake.service.IMediaSessionManagerProxy;
import com.darkbox.fake.service.INetworkManagementServiceProxy;
import com.darkbox.fake.service.INotificationManagerProxy;
import com.darkbox.fake.service.IPackageManagerProxy;
import com.darkbox.fake.service.IPermissionManagerProxy;
import com.darkbox.fake.service.IPersistentDataBlockServiceProxy;
import com.darkbox.fake.service.IPhoneSubInfoProxy;
import com.darkbox.fake.service.IPowerManagerProxy;
import com.darkbox.fake.service.IShortcutManagerProxy;
import com.darkbox.fake.service.IStorageManagerProxy;
import com.darkbox.fake.service.IStorageStatsManagerProxy;
import com.darkbox.fake.service.ISystemUpdateProxy;
import com.darkbox.fake.service.ITelephonyManagerProxy;
import com.darkbox.fake.service.ITelephonyRegistryProxy;
import com.darkbox.fake.service.IUserManagerProxy;
import com.darkbox.fake.service.IVibratorServiceProxy;
import com.darkbox.fake.service.IVpnManagerProxy;
import com.darkbox.fake.service.IWifiManagerProxy;
import com.darkbox.fake.service.IWifiScannerProxy;
import com.darkbox.fake.service.IWindowManagerProxy;
import com.darkbox.fake.service.context.ContentServiceStub;
import com.darkbox.fake.service.context.RestrictionsManagerStub;
import com.darkbox.fake.service.libcore.OsStub;
import com.darkbox.fake.service.vivo.IVivoPermissionServiceProxy;
import com.darkbox.utils.Slog;
import com.darkbox.utils.compat.BuildCompat;

/**
 * Created by @jagdish_vip on 3/30/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class HookManager {
    public static final String TAG = "HookManager";

    private static final HookManager sHookManager = new HookManager();

    private final Map<Class<?>, IInjectHook> mInjectors = new HashMap<>();

    public static HookManager get() {
        return sHookManager;
    }

    public void init() {
        if (VBoxCore.get().isBlackProcess() || VBoxCore.get().isServerProcess()) {
            addInjector(new OsStub());
            addInjector(new IDisplayManagerProxy());
            //addInjector(new IDropBoxManagerProxy());
            //addInjector(new IInputMethodManagerProxy());
            addInjector(new IJobServiceProxy());
            addInjector(new IActivityManagerProxy());
            addInjector(new IPackageManagerProxy());
            addInjector(new ITelephonyManagerProxy());
            addInjector(new HCallbackStub());
            addInjector(new IWifiManagerProxy());
            addInjector(new IWifiScannerProxy());
            addInjector(new ISubProxy());
            addInjector(new IAppOpsManagerProxy());
            addInjector(new INotificationManagerProxy());
            addInjector(new IAlarmManagerProxy());
            addInjector(new IAppWidgetManagerProxy());
            addInjector(new IAudioManagerProxy());
            addInjector(new IBackupManagerProxy());
            addInjector(new IBluetoothManagerProxy());
            addInjector(new ContentServiceStub());
            addInjector(new IWindowManagerProxy());
            addInjector(new IUserManagerProxy());
          //  addInjector(new RestrictionsManagerStub());
            addInjector(new IMediaSessionManagerProxy());
            addInjector(new ILocationManagerProxy());
            addInjector(new ISmsProxy());
            addInjector(new IStorageManagerProxy());
            addInjector(new ILauncherAppsProxy());
            addInjector(new IAccessibilityManagerProxy());
            addInjector(new ITelephonyRegistryProxy());
            addInjector(new IDevicePolicyManagerProxy());
            addInjector(new IAccountManagerProxy());
            addInjector(new IConnectivityManagerProxy());
            addInjector(new IClipboardManagerProxy());
            addInjector(new IPhoneSubInfoProxy());
            addInjector(new IMediaRouterServiceProxy());
            addInjector(new INetworkManagementServiceProxy());
            addInjector(new IPowerManagerProxy());
           // addInjector(new ICrossProfileAppsProxy());
            addInjector(new IVibratorServiceProxy());
            addInjector(AppInstrumentation.get());
            
            if (BuildCompat.isVivo()) {
                addInjector(new IVivoPermissionServiceProxy());
            }
            if (BuildCompat.isBaklava()) {
                addInjector(new IPersistentDataBlockServiceProxy());
            }
            if (BuildCompat.isUpsideDownCake()) {
                addInjector(new IAppIntegrityManagerProxy());
                addInjector(new ILocaleManagerProxy());
            }
            
            if (BuildCompat.isS()) {
                addInjector(new IActivityClientProxy((Object) null));
                addInjector(new IVpnManagerProxy());
            }
            if (BuildCompat.isR()) {
                addInjector(new IActivityTaskManagerProxy());
                addInjector(new IPermissionManagerProxy());
            }
            if (BuildCompat.isQ()) {
                addInjector(new IDeviceIdentifiersPolicyProxy());
            }
            if (BuildCompat.isPie()) {
                addInjector(new ISystemUpdateProxy());
            }
            
            if (BuildCompat.isOreo_MR1()) {
                addInjector(new IAutofillManagerProxy());
                addInjector(new IContextHubServiceProxy());
                addInjector(new IStorageStatsManagerProxy());
                addInjector(new ISystemUpdateProxy());
            }
            
            if (BuildCompat.isOreo()) {
                addInjector(new IShortcutManagerProxy());
            }
            
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

    public void checkAll() {
        for (Class<?> aClass : mInjectors.keySet()) {
            IInjectHook iInjectHook = mInjectors.get(aClass);
            if (iInjectHook != null && iInjectHook.isBadEnv()) {
                Log.d(TAG, "checkEnv: " + aClass.getSimpleName() + " is bad env");
                iInjectHook.injectHook();
            }
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
