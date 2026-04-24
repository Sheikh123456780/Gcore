package com.darkbox.fake.delegate;

import android.net.Uri;
import android.os.IInterface;
import android.util.ArrayMap;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import black.android.app.BRActivityThread;
import black.android.app.BRActivityThreadProviderClientRecordP;
import black.android.app.BRIActivityManagerContentProviderHolder;
import black.android.content.BRContentProviderHolderOreo;
import black.android.providers.BRSettingsContentProviderHolder;
import black.android.providers.BRSettingsGlobal;
import black.android.providers.BRSettingsNameValueCache;
import black.android.providers.BRSettingsNameValueCacheOreo;
import black.android.providers.BRSettingsSecure;
import black.android.providers.BRSettingsSystem;

import com.darkbox.VBoxCore;
import com.darkbox.fake.service.context.providers.ContentProviderStub;
import com.darkbox.fake.service.context.providers.SystemProviderStub;
import com.darkbox.utils.compat.BuildCompat;

/**
 * FULL FIXED – Android 10 → 15+
 * Settings authority crash FIXED
 */
public class ContentProviderDelegate {

    public static final String TAG = "ContentProviderDelegate";
    private static final Set<String> sInjected = new HashSet<>();

    public static void update(Object holder, String auth) {
        IInterface base;

        if (BuildCompat.isOreo()) {
            base = BRContentProviderHolderOreo.get(holder).provider();
        } else {
            base = BRIActivityManagerContentProviderHolder.get(holder).provider();
        }

        if (base instanceof Proxy) return;

        IInterface proxy;
        if ("settings".equals(auth)) {
            proxy = new SystemProviderStub().wrapper(base, VBoxCore.getHostPkg());
        } else {
            proxy = new ContentProviderStub().wrapper(base, VBoxCore.getHostPkg());
        }

        if (BuildCompat.isOreo()) {
            BRContentProviderHolderOreo.get(holder)._set_provider(proxy);
        } else {
            BRIActivityManagerContentProviderHolder.get(holder)._set_provider(proxy);
        }
    }

    public static void init() {
        clearSettingProvider();

        // 🔒 SAFE warm-up (Android 12+ guard)
        try {
            VBoxCore.getContext().getContentResolver().call(Uri.parse("content://settings"), "", null, null);
        } catch (Throwable ignored) {
        }

        Object activityThread = VBoxCore.mainThread();
        ArrayMap<Object, Object> providerMap = (ArrayMap<Object, Object>) BRActivityThread.get(activityThread).mProviderMap();

        for (Object record : providerMap.values()) {

            String[] names = BRActivityThreadProviderClientRecordP.get(record).mNames();
            if (names == null || names.length == 0) continue;

            String providerName = names[0];
            if (sInjected.contains(providerName)) continue;
            sInjected.add(providerName);

            IInterface base = BRActivityThreadProviderClientRecordP.get(record).mProvider();
            IInterface proxy;

            // ⭐ MOST IMPORTANT FIX
            if ("settings".equals(providerName)) {
                proxy = new SystemProviderStub().wrapper(base, VBoxCore.getHostPkg());
            } else {
                proxy = new ContentProviderStub().wrapper(base, VBoxCore.getHostPkg());
            }

            BRActivityThreadProviderClientRecordP.get(record)._set_mProvider(proxy);
            BRActivityThreadProviderClientRecordP.get(record)._set_mNames(new String[]{providerName});
        }
    }

    public static void clearSettingProvider() {
        Object cache;

        cache = BRSettingsSystem.get().sNameValueCache();
        if (cache != null) clearContentProvider(cache);

        cache = BRSettingsSecure.get().sNameValueCache();
        if (cache != null) clearContentProvider(cache);

        if (BRSettingsGlobal.getRealClass() != null) {
            cache = BRSettingsGlobal.get().sNameValueCache();
            if (cache != null) clearContentProvider(cache);
        }
    }

    private static void clearContentProvider(Object cache) {
        if (BuildCompat.isOreo()) {
            Object holder = BRSettingsNameValueCacheOreo.get(cache).mProviderHolder();
            if (holder != null) {
                BRSettingsContentProviderHolder.get(holder)._set_mContentProvider(null);
            }
        } else {
            BRSettingsNameValueCache.get(cache)._set_mContentProvider(null);
        }
    }
}