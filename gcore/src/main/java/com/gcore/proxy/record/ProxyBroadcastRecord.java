package com.gcore.proxy.record;

import android.content.Intent;

public class ProxyBroadcastRecord {

    public Intent mIntent;
    public int mUserId;

    public ProxyBroadcastRecord(Intent intent, int userId) {
        mIntent = intent;
        mUserId = userId;
    }

    public static void saveStub(Intent shadow, Intent target, int userId) {
        shadow.putExtra("_G_|_target_", target);
        shadow.putExtra("_G_|_user_id_", userId);
    }

    public static ProxyBroadcastRecord create(Intent intent) {
        Intent target = intent.getParcelableExtra("_G_|_target_");
        int userId = intent.getIntExtra("_G_|_user_id_", 0);
        return new ProxyBroadcastRecord(target, userId);
    }
}
