package com.darkbox.core.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.File;
import com.darkbox.VBoxCore;
import com.darkbox.core.env.AppSystemEnv;
import com.darkbox.core.env.BEnvironment;
import com.darkbox.core.system.accounts.BAccountManagerService;
import com.darkbox.core.system.am.BActivityManagerService;
import com.darkbox.core.system.am.BJobManagerService;
import com.darkbox.core.system.location.BLocationManagerService;
import com.darkbox.core.system.notification.BNotificationManagerService;
import com.darkbox.core.system.os.BStorageManagerService;
import com.darkbox.core.system.pm.BPackageInstallerService;
import com.darkbox.core.system.pm.BPackageManagerService;
import com.darkbox.core.system.pm.BXposedManagerService;
import com.darkbox.core.system.user.BUserHandle;
import com.darkbox.core.system.user.BUserManagerService;
import com.darkbox.entity.pm.InstallOption;
import com.darkbox.utils.FileUtils;


public class VBoxSystem {

    private static VBoxSystem sVBoxSystem;
    private final List<ISystemService> mServices = new ArrayList<>();
    private final static AtomicBoolean isStartup = new AtomicBoolean(false);
    
    // Local JAR files (replaced RemoteManager)
    public static File JUNIT_JAR;
    public static File EMPTY_JAR;

    public static VBoxSystem getSystem() {
        if (sVBoxSystem == null) {
            synchronized (VBoxSystem.class) {
                if (sVBoxSystem == null) {
                    sVBoxSystem = new VBoxSystem();
                }
            }
        }
        return sVBoxSystem;
    }
    
    private void initJarPaths() {
        if (JUNIT_JAR == null) {
            JUNIT_JAR = new File(BEnvironment.getCacheDir(), "junit.apk");
        }
        if (EMPTY_JAR == null) {
            EMPTY_JAR = new File(BEnvironment.getCacheDir(), "empty.apk");
        }
    }

    public void startup() {
        if (!isStartup.getAndSet(true)) {
            BEnvironment.load();
            mServices.add(BPackageManagerService.get());
            mServices.add(BUserManagerService.get());
            mServices.add(BActivityManagerService.get());
            mServices.add(BJobManagerService.get());
            mServices.add(BStorageManagerService.get());
            mServices.add(BPackageInstallerService.get());
            mServices.add(BXposedManagerService.get());
            mServices.add(BProcessManagerService.get());
            mServices.add(BAccountManagerService.get());
            mServices.add(BLocationManagerService.get());
            mServices.add(BNotificationManagerService.get());

            for (ISystemService systemReady : mServices) {
                systemReady.systemReady();
            }

            List<String> preInstallPackages = AppSystemEnv.getPreInstallPackages();
            for (String preInstallPackage : preInstallPackages) {
                try {
                    if (!BPackageManagerService.get().isInstalled(preInstallPackage, BUserHandle.USER_ALL)) {
                        PackageInfo packageInfo = VBoxCore.getPackageManager().getPackageInfo(preInstallPackage, 0);
                        BPackageManagerService.get().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), BUserHandle.USER_ALL);
                    }
                } catch (PackageManager.NameNotFoundException ignored) { }
            }
        }
        initJarEnv();
    }

    private void initJarEnv() {
        try {
            initJarPaths();
            
            // Copy junit.jar
            if (JUNIT_JAR != null && !JUNIT_JAR.exists()) {
                InputStream junit = VBoxCore.getContext().getAssets().open("junit.jar");
                FileUtils.copyFile(junit, JUNIT_JAR);
                junit.close();
            }

            // Copy empty.jar
            if (EMPTY_JAR != null && !EMPTY_JAR.exists()) {
                InputStream empty = VBoxCore.getContext().getAssets().open("empty.jar");
                FileUtils.copyFile(empty, EMPTY_JAR);
                empty.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
