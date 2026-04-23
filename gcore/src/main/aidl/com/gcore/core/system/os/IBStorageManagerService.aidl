package com.gcore.core.system.os;

import android.os.storage.StorageVolume;
import java.lang.String;
import android.net.Uri;

interface IBStorageManagerService {
      StorageVolume[] getVolumeList(int uid, String packageName, int flags, int userId);
      Uri getUriForFile(String file);
}
