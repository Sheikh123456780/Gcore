package com.gcore.core.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gcore.GreenBoxCore;
import com.gcore.core.env.AppSystemEnv;
import com.gcore.core.env.BEnvironment;
import com.gcore.core.system.accounts.BAccountManagerService;
import com.gcore.core.system.am.BActivityManagerService;
import com.gcore.core.system.am.BJobManagerService;
import com.gcore.core.system.location.BLocationManagerService;
import com.gcore.core.system.notification.BNotificationManagerService;
import com.gcore.core.system.os.BStorageManagerService;
import com.gcore.core.system.pm.BPackageInstallerService;
import com.gcore.core.system.pm.BPackageManagerService;
import com.gcore.core.system.user.BUserHandle;
import com.gcore.core.system.user.BUserManagerService;
import com.gcore.entity.pm.InstallOption;

public class BlackBoxSystem {

    private static volatile BlackBoxSystem sBlackBoxSystem;
    private final List<ISystemService> mServices = new ArrayList<>();
    private final static AtomicBoolean isStartup = new AtomicBoolean(false);

    public static BlackBoxSystem getSystem() {
        if (sBlackBoxSystem == null) {
            synchronized (BlackBoxSystem.class) {
                if (sBlackBoxSystem == null) {
                    sBlackBoxSystem = new BlackBoxSystem();
                }
            }
        }
        return sBlackBoxSystem;
    }

    public void startup() {
        if (isStartup.getAndSet(true)) {
            return;
        }
        BEnvironment.load();

        mServices.add(BPackageManagerService.get());
        mServices.add(BUserManagerService.get());
        mServices.add(BActivityManagerService.get());
        mServices.add(BJobManagerService.get());
        mServices.add(BStorageManagerService.get());
        mServices.add(BPackageInstallerService.get());
        mServices.add(BProcessManagerService.get());
        mServices.add(BAccountManagerService.get());
        mServices.add(BLocationManagerService.get());
        mServices.add(BNotificationManagerService.get());

        for (ISystemService service : mServices) {
            service.systemReady();
        }

        List<String> preInstallPackages = AppSystemEnv.getPreInstallPackages();
        for (String preInstallPackage : preInstallPackages) {
            try {
                if (!BPackageManagerService.get().isInstalled(preInstallPackage, BUserHandle.USER_ALL)) {
                    PackageInfo packageInfo = GreenBoxCore.getPackageManager().getPackageInfo(preInstallPackage, 0);
                    BPackageManagerService.get().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), BUserHandle.USER_ALL);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
    }
}
