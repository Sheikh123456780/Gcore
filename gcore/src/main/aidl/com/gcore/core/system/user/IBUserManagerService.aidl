package com.gcore.core.system.user;

import com.gcore.core.system.user.BUserInfo;
import java.util.List;

interface IBUserManagerService {
    BUserInfo getUserInfo(int userId);
    boolean exists(int userId);
    BUserInfo createUser(int userId);
    List<BUserInfo> getUsers();
    void deleteUser(int userId);
}
