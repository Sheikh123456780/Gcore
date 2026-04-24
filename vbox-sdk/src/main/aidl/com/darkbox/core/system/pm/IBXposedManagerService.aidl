// IBXposedManagerService.aidl

package com.darkbox.core.system.pm;

import java.util.List;
import com.darkbox.entity.pm.InstalledModule;

interface IBXposedManagerService {
    boolean isXPEnable();
    void setXPEnable(boolean enable);
    boolean isModuleEnable(String packageName);
    void setModuleEnable(String packageName, boolean enable);
    List<InstalledModule> getInstalledModules();
}