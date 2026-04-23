package com.gcore.core.system.os;

import android.net.Uri;
import android.os.Process;
import android.os.storage.StorageVolume;

import androidx.core.content.FileProvider;

import java.io.File;

import black.android.os.storage.BRStorageManager;
import black.android.os.storage.BRStorageVolume;

import com.gcore.GreenBoxCore;
import com.gcore.core.env.BEnvironment;
import com.gcore.core.system.ISystemService;
import com.gcore.core.system.user.BUserHandle;
import com.gcore.proxy.ProxyManifest;
import com.gcore.utils.compat.BuildCompat;

public class BStorageManagerService extends IBStorageManagerService.Stub implements ISystemService {

    private static final BStorageManagerService sService = new BStorageManagerService();

    public static BStorageManagerService get() {
        return sService;
    }

    public BStorageManagerService() {
    }

    @Override
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags, int userId) {
        if (BRStorageManager.get().getVolumeList(0, 0) == null) {
            return null;
        }
        try {
            StorageVolume[] storageVolumes = BRStorageManager.get().getVolumeList(BUserHandle.getUserId(Process.myUid()), 0);
            if (storageVolumes == null) {
                return null;
            }
            for (StorageVolume storageVolume : storageVolumes) {
                BRStorageVolume.get(storageVolume)._set_mPath(BEnvironment.getExternalUserDir());
                if (BuildCompat.isPie()) {
                    BRStorageVolume.get(storageVolume)._set_mInternalPath(BEnvironment.getExternalUserDir());
                }
            }
            return storageVolumes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Uri getUriForFile(String file) {
        return FileProvider.getUriForFile(GreenBoxCore.getContext(), ProxyManifest.getProxyFileProvider(), new File(file));
    }

    @Override
    public void systemReady() {

    }
}
