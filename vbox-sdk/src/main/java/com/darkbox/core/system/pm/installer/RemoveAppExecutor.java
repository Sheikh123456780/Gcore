package com.darkbox.core.system.pm.installer;

import com.darkbox.core.env.BEnvironment;
import com.darkbox.core.system.pm.BPackageSettings;
import com.darkbox.entity.pm.InstallOption;
import com.darkbox.utils.FileUtils;

public class RemoveAppExecutor implements Executor {
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        return 0;
    }
}
