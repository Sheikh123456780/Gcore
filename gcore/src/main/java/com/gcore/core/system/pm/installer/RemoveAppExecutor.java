package com.gcore.core.system.pm.installer;

import com.gcore.core.env.BEnvironment;
import com.gcore.core.system.pm.BPackageSettings;
import com.gcore.entity.pm.InstallOption;
import com.gcore.utils.FileUtils;

public class RemoveAppExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        return 0;
    }
}
