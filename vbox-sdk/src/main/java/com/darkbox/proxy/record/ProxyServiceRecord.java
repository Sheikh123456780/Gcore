package com.darkbox.proxy.record;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;

import com.darkbox.utils.compat.BundleCompat;

/**
 * Created by Milk on 4/1/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class ProxyServiceRecord {
    public Intent mServiceIntent;
    public ServiceInfo mServiceInfo;
    public IBinder mToken;
    public int mUserId;
    public int mStartId;

    public ProxyServiceRecord(Intent serviceIntent, ServiceInfo serviceInfo, IBinder token, int userId, int startId) {
        mServiceIntent = serviceIntent;
        mServiceInfo = serviceInfo;
        mUserId = userId;
        mStartId = startId;
        mToken = token;
    }

    public static void saveStub(Intent shadow, Intent target, ServiceInfo serviceInfo, IBinder token, int userId, int startId) {
        shadow.putExtra("_V_|_target_", target);
        shadow.putExtra("_V_|_service_info_", serviceInfo);
        shadow.putExtra("_V_|_user_id_", userId);
        shadow.putExtra("_V_|_start_id_", startId);
        BundleCompat.putBinder(shadow, "_V_|_token_", token);
    }

    public static ProxyServiceRecord create(Intent intent) {
        Intent target = intent.getParcelableExtra("_V_|_target_");
        ServiceInfo serviceInfo = intent.getParcelableExtra("_V_|_service_info_");
        int userId = intent.getIntExtra("_V_|_user_id_", 0);
        int startId = intent.getIntExtra("_V_|_start_id_", 0);
        IBinder token = BundleCompat.getBinder(intent, "_V_|_token_");
        return new ProxyServiceRecord(target, serviceInfo, token, userId, startId);
    }
}
