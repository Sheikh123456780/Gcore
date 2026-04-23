package com.gcore.app.configuration;

import java.io.File;

public abstract class ClientConfiguration {

    public abstract String getHostPackageName();

    public boolean isHideRoot() {
        return false;
    }

    public boolean isEnableDaemonService() {
        return false;
    }

    public boolean isEnableLauncherActivity() {
        return true;
    }

    public boolean requestInstallPackage(File file) {
        return false;
    }
}
