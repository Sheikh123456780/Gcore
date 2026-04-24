package com.darkbox.core.system.pm.installer;

import com.darkbox.core.env.BEnvironment;
import com.darkbox.core.system.pm.BPackageSettings;
import com.darkbox.entity.pm.InstallOption;
import com.darkbox.utils.FileUtils;

public class CreateUserExecutor implements Executor {
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        FileUtils.mkdirs(BEnvironment.getDataDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDeDataDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataCacheDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataDatabasesDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataFilesDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getExternalDataCacheDir(packageName));
        FileUtils.mkdirs(BEnvironment.getExternalDataFilesDir(packageName));
        return 0;
    }
}
