package com.gcore.core.system.pm.installer;

import com.gcore.core.system.pm.BPackageSettings;
import com.gcore.entity.pm.InstallOption;

public interface Executor {

    int exec(BPackageSettings ps, InstallOption option, int userId);
}
