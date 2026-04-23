package com.gcore.core.system.pm.installer;

import com.gcore.core.env.BEnvironment;
import com.gcore.core.system.pm.BPackageSettings;
import com.gcore.entity.pm.InstallOption;
import com.gcore.utils.FileUtils;

public class RemoveUserExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        // delete user dir
        FileUtils.deleteDir(BEnvironment.getDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getDeDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getExternalDataDir(packageName));
        FileUtils.deleteDir(BEnvironment.getExternalObbDir(packageName));
        return 0;
    }
}
