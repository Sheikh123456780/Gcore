package com.darkbox.proxy.record;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import com.darkbox.utils.compat.BundleCompat;

/**
 * Created by @jagdish_vip on 3/31/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class ProxyActivityRecord {
    public final int mUserId;
    public final ActivityInfo mActivityInfo;
    public final Intent mTarget;
    public final String mActivityRecord;

    public ProxyActivityRecord(int userId, ActivityInfo activityInfo, Intent target, String activityRecord) {
        this.mUserId = userId;
        this.mActivityInfo = activityInfo;
        this.mTarget = target;
        this.mActivityRecord = activityRecord;
    }

    public static void saveStub(Intent shadow, Intent target, ActivityInfo activityInfo, String activityToken, int userId) {
        shadow.putExtra("_V_|_user_id_", userId);
        shadow.putExtra("_V_|_activity_info_", activityInfo);
        shadow.putExtra("_V_|_target_", target);
        shadow.putExtra("_V_|_activity_token_v_", activityToken);
    }

    public static ProxyActivityRecord create(Intent intent) {
        int userId = intent.getIntExtra("_V_|_user_id_", 0);
        ActivityInfo activityInfo = intent.getParcelableExtra("_V_|_activity_info_");

        Intent target = intent.getParcelableExtra("_V_|_target_");
        String activityToken = intent.getStringExtra("_V_|_activity_token_v_");
        return new ProxyActivityRecord(userId, activityInfo, target, activityToken);
    }
}
