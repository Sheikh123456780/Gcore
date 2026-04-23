package com.gcore.core.system.pm.installer;

import com.gcore.core.env.BEnvironment;
import com.gcore.core.system.pm.BPackageSettings;
import com.gcore.entity.pm.InstallOption;
import com.gcore.utils.FileUtils;

public class CreateUserExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        FileUtils.mkdirs(BEnvironment.getDataDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDeDataDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getFilesDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getOatDir(packageName, userId));

        /*/try {
            // /data/data/xx/lib -> /data/app/xx/lib
            FileUtils.createSymlink(BEnvironment.getAppLibDir(ps.pkg.packageName).getAbsolutePath(), BEnvironment.getDataLibDir(packageName, userId).getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }*/
        return 0;
    }
}
