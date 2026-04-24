package com.darkbox.core.system.pm.installer;

import com.darkbox.core.system.pm.BPackageSettings;
import com.darkbox.entity.pm.InstallOption;

public interface Executor {
    public static final String TAG = "InstallExecutor";

    int exec(BPackageSettings bPackageSettings, InstallOption installOption, int i);
}
