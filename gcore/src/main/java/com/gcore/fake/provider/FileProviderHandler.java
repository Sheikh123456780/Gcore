package com.gcore.fake.provider;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;

import com.gcore.GreenBoxCore;
import com.gcore.app.GActivityThread;
import com.gcore.utils.compat.BuildCompat;

public class FileProviderHandler {

    public static Uri getUriForFile(Context context, File file) {
        if (BuildCompat.isN()) {
            Uri uri = findContentUri(context, file);
            if (uri != null) {
                return uri;
            }
            return GreenBoxCore.getBStorageManager().getUriForFile(String.valueOf(file));
        } else {
            return Uri.fromFile(file);
        }
    }

    private static Uri findContentUri(Context context, File file) {
        List<ProviderInfo> providers = GActivityThread.getProviders();
        if (providers != null) {
            for (ProviderInfo provider : providers) {
                try {
                    if (provider == null || provider.authority == null) {
                        continue;
                    }
                    Uri uri = FileProvider.getUriForFile(context, provider.authority, file);
                    if (uri != null) {
                        return uri;
                    }
                } catch (IllegalArgumentException illegalArgumentException) {
                    //ignore
                }
            }
        }
        return null;
    }

    public static File uriToFile(Context context, Uri uri) {
        if (uri == null) return null;

        final String scheme = uri.getScheme();

        if (scheme == null) return null;

        if (scheme.equals("file")) {
            return new File(uri.getPath());
        }

        if (scheme.equals("content")) {
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, new String[]{android.provider.MediaStore.MediaColumns.DATA}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DATA);
                    String path = cursor.getString(columnIndex);
                    if (path != null) {
                        return new File(path);
                    }
                }
            } catch (Exception exception) {
                //ignore
            }

            try {
                java.io.InputStream in = context.getContentResolver().openInputStream(uri);
                if (in != null) {
                    File temp = new File(context.getCacheDir(), "tmp_" + System.currentTimeMillis());
                    try (java.io.OutputStream out = new java.io.FileOutputStream(temp)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    } finally {
                        in.close();
                    }
                    return temp;
                }
            } catch (Exception exception) {
                //ignore
            }
        }
        return null;
    }

}
