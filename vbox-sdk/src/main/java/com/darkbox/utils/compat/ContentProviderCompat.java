package com.darkbox.utils.compat;

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;

public class ContentProviderCompat {

    /* =========================================
       SAFE CALL (A10 → A17)
    ========================================= */
    public static Bundle call(Context context,Uri uri,String method,String arg,Bundle extras,int retryCount) throws IllegalAccessException {
        ContentProviderClient client = acquireContentProviderClientRetry(context, uri, retryCount);

        if (client == null) {
            throw new IllegalAccessException("ContentProviderClient == null");
        }

        try {
            return client.call(method, arg, extras);
        } catch (RemoteException | RuntimeException e) {
            // RuntimeException = DeadObjectException / IllegalStateException (A12+)
            throw new IllegalAccessException(e.getMessage());
        } finally {
            releaseQuietly(client);
        }
    }

    /* =========================================
       Acquire by URI
    ========================================= */
    private static ContentProviderClient acquireContentProviderClient(Context context, Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+
                return context.getContentResolver().acquireUnstableContentProviderClient(uri);
            } else {
                return context.getContentResolver().acquireContentProviderClient(uri);
            }
        } catch (Throwable e) {
            return null;
        }
    }

    /* =========================================
       Acquire by AUTHORITY
    ========================================= */
    private static ContentProviderClient acquireContentProviderClient(Context context, String name) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return context.getContentResolver().acquireUnstableContentProviderClient(name);
            } else {
                return context.getContentResolver().acquireContentProviderClient(name);
            }
        } catch (Throwable e) {
            return null;
        }
    }

    /* =========================================
       Retry (URI)
    ========================================= */
    public static ContentProviderClient acquireContentProviderClientRetry(Context context, Uri uri, int retryCount) {
        ContentProviderClient client = null;
        for (int i = 0; i < retryCount; i++) {
            client = acquireContentProviderClient(context, uri);
            if (client != null) break;
            SystemClock.sleep(300L);
        }
        return client;
    }

    /* =========================================
       Retry (AUTHORITY)
    ========================================= */
    public static ContentProviderClient acquireContentProviderClientRetry(Context context, String name, int retryCount) {
        ContentProviderClient client = null;
        for (int i = 0; i < retryCount; i++) {
            client = acquireContentProviderClient(context, name);
            if (client != null) break;
            SystemClock.sleep(300L);
        }
        return client;
    }

    /* =========================================
       Safe release (A10 → A17)
    ========================================= */
    private static void releaseQuietly(ContentProviderClient client) {
        if (client == null) return;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                client.close();   // N+
            } else {
                client.release(); // < N
            }
        } catch (Throwable ignored) {
        }
    }
}