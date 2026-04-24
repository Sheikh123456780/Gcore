package com.darkbox.fake.service;

import android.os.IInterface;
import android.os.storage.StorageVolume;

import java.lang.reflect.Method;

import black.android.os.BRServiceManager;
import black.android.os.mount.BRIMountServiceStub;
import black.android.os.storage.BRIStorageManagerStub;

import com.darkbox.VBoxCore;
import com.darkbox.app.BActivityThread;
import com.darkbox.fake.hook.BinderInvocationStub;
import com.darkbox.fake.hook.MethodHook;
import com.darkbox.fake.hook.ProxyMethod;
import com.darkbox.utils.MethodParameterUtils;
import com.darkbox.utils.Slog;
import com.darkbox.utils.compat.BuildCompat;

/**
 * Storage compatibility proxy
 * Android 8 → Android 17
 * PUBG / BGMI update fix
 */
public class IStorageManagerProxy extends BinderInvocationStub {

	public IStorageManagerProxy() {
		super(BRServiceManager.get().getService("mount"));
	}

	@Override
	protected Object getWho() {
		IInterface mount;
		if (BuildCompat.isOreo()) {
			mount = BRIStorageManagerStub.get().asInterface(BRServiceManager.get().getService("mount"));
		} else {
			mount = BRIMountServiceStub.get().asInterface(BRServiceManager.get().getService("mount"));
		}
		return mount;
	}

	@Override
	protected void inject(Object baseInvocation, Object proxyInvocation) {
		replaceSystemService("mount");
	}

	@Override
	public boolean isBadEnv() {
		return false;
	}

	@ProxyMethod("getVolumeList")
	public static class GetVolumeList extends MethodHook {
		@Override
		protected Object hook(Object who, Method method, Object[] args) throws Throwable {
			if (args == null) {
				StorageVolume[] volumeList = VBoxCore.getBStorageManager().getVolumeList(BActivityThread.getBUid(), null, 0, BActivityThread.getUserId());
				if (volumeList == null) {
					return method.invoke(who, args);
				}
				return volumeList;
			}
			try {
				int uid = Integer.parseInt(args[0] + "");
				String packageName = (String) args[1];
				int flags = Integer.parseInt(args[2] + "");
				StorageVolume[] volumeList = VBoxCore.getBStorageManager().getVolumeList(uid, packageName, flags, BActivityThread.getUserId());
				if (volumeList == null) {
					return method.invoke(who, args);
				}
				return volumeList;
			} catch (Throwable t) {
				return method.invoke(who, args);
			}
		}
	}

	@ProxyMethod("mkdirs")
	public static class mkdirs extends MethodHook {
		@Override
		protected Object hook(Object who, Method method, Object[] args) throws Throwable {
			return 0;
		}
	}
	
	private static int getFlags(Object arg) {
		if (arg instanceof Integer) {
			return (Integer) arg;
		}
		if (arg instanceof Long) {
			return ((Long) arg).intValue();
		}
		return 0;
	}
}
