package com.gcore.utils.compat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.Locale;

import com.gcore.GreenBoxCore;
import com.gcore.app.GActivityThread;
import com.gcore.utils.DrawableUtils;

public class TaskDescriptionCompat {

    public static ActivityManager.TaskDescription fix(ActivityManager.TaskDescription td) {
        String label = td.getLabel();
        Bitmap icon = td.getIcon();
        if (label != null && icon != null) {
            return td;
        }
        label = getTaskDescriptionLabel(getApplicationLabel());
        Drawable drawable = getApplicationIcon();
        if (drawable == null) {
            return td;
        }
        ActivityManager am = (ActivityManager) GreenBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int iconSize = am.getLauncherLargeIconSize();
        icon = DrawableUtils.drawableToBitmap(drawable, iconSize, iconSize);
        td = new ActivityManager.TaskDescription(label, icon, td.getPrimaryColor());
        return td;
    }

    public static String getTaskDescriptionLabel(CharSequence label) {
        return String.format(Locale.getDefault(), "%s", label);
    }

    private static CharSequence getApplicationLabel() {
        try {
            PackageManager pm = GreenBoxCore.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(GActivityThread.getAppPackageName(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static Drawable getApplicationIcon() {
        try {
            return GreenBoxCore.getPackageManager().getApplicationIcon(GActivityThread.getAppPackageName());
        } catch (PackageManager.NameNotFoundException ignore) {
            return null;
        }
    }
}
