package com.gcore.core.system.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import com.gcore.GreenBoxCore;
import com.gcore.utils.compat.BuildCompat;

public class NotificationChannelManager {

    private final static NotificationChannelManager sManager = new NotificationChannelManager();

    public static NotificationChannel APP_CHANNEL;

    public static NotificationChannelManager get() {
        return sManager;
    }

    public NotificationChannelManager() {
        if (BuildCompat.isOreo_MR1()) {
            registerAppChannel();
        }
    }

    private void registerAppChannel() {
        NotificationManager nm = (NotificationManager) GreenBoxCore.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ONE_ID = GreenBoxCore.getContext().getPackageName();
        if (BuildCompat.isOreo_MR1()) {
            APP_CHANNEL = new NotificationChannel(CHANNEL_ONE_ID, "GreenBoxCore", NotificationManager.IMPORTANCE_HIGH);
            APP_CHANNEL.enableLights(true);
            APP_CHANNEL.setLightColor(Color.RED);
            APP_CHANNEL.setShowBadge(true);
            APP_CHANNEL.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(APP_CHANNEL);
        }
    }
}
