package com.gcore.fake.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import java.io.File;
import java.lang.reflect.Method;

import com.gcore.GreenBoxCore;
import com.gcore.app.GActivityThread;
import com.gcore.fake.hook.MethodHook;
import com.gcore.fake.hook.ProxyMethod;
import com.gcore.fake.provider.FileProviderHandler;
import com.gcore.utils.ComponentUtils;
import com.gcore.utils.MethodParameterUtils;
import com.gcore.utils.Slog;
import com.gcore.utils.compat.BuildCompat;
import com.gcore.utils.compat.StartActivityCompat;

import static android.content.pm.PackageManager.GET_META_DATA;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class ActivityManagerCommonProxy {

    public static final String TAG = "ActivityManagerCommonProxy";

    @ProxyMethod("startActivity")
    public static class StartActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            Intent intent = getIntent(args);
            Slog.d(TAG, "Hook in : " + intent);
            assert intent != null;
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                String url = intent.getDataString();
                if (url != null && url.startsWith("http")) {
                    return -1;
                }
            }
            /*/if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                String url = intent.getDataString();
                if (url != null && url.startsWith("http")) {
                    Intent webIntent = new Intent(BActivityThread.getApplication(), WebViewActivity.class);
                    webIntent.putExtra("url", url);
                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    BActivityThread.getApplication().startActivity(webIntent);
                    return 0;
                }
            }*/
            if (intent.getParcelableExtra("_G_|_target_") != null) {
                return method.invoke(who, args);
            }
            if (ComponentUtils.isRequestInstall(intent)) {
                Context context = GActivityThread.getApplication();
                Uri originalUri = intent.getData();
                if (originalUri == null) {
                    return method.invoke(who, args);
                }
                File apkFile = FileProviderHandler.uriToFile(context, originalUri);
                if (apkFile != null && GreenBoxCore.get().requestInstallPackage(apkFile)) {
                    return 0;
                }
                Uri safeUri = FileProviderHandler.getUriForFile(context, apkFile != null ? apkFile : new File(originalUri.getPath()));
                intent.setData(safeUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                return method.invoke(who, args);
            }
            String dataString = intent.getDataString();
            if (dataString != null && dataString.equals("package:" + GActivityThread.getAppPackageName())) {
                intent.setData(Uri.parse("package:" + GreenBoxCore.getHostPkg()));
            }
            ResolveInfo resolveInfo = GreenBoxCore.getBPackageManager().resolveActivity(intent, GET_META_DATA, StartActivityCompat.getResolvedType(args), GActivityThread.getUserId());
            if (resolveInfo == null) {
                String origPackage = intent.getPackage();
                if (intent.getPackage() == null && intent.getComponent() == null) {
                    intent.setPackage(GActivityThread.getAppPackageName());
                } else {
                    origPackage = intent.getPackage();
                }
                resolveInfo = GreenBoxCore.getBPackageManager().resolveActivity(intent, GET_META_DATA, StartActivityCompat.getResolvedType(args), GActivityThread.getUserId());
                if (resolveInfo == null) {
                    intent.setPackage(origPackage);
                    return method.invoke(who, args);
                }
            }
            intent.setExtrasClassLoader(who.getClass().getClassLoader());
            intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
            GreenBoxCore.getBActivityManager().startActivityAms(GActivityThread.getUserId(), StartActivityCompat.getIntent(args), StartActivityCompat.getResolvedType(args), StartActivityCompat.getResultTo(args), StartActivityCompat.getResultWho(args), StartActivityCompat.getRequestCode(args), StartActivityCompat.getFlags(args), StartActivityCompat.getOptions(args));
            return 0;
        }

        private Intent getIntent(Object[] args) {
            int index;
            if (BuildCompat.isR()) {
                index = 3;
            } else {
                index = 2;
            }
            if (args[index] instanceof Intent) {
                return (Intent) args[index];
            }
            for (Object arg : args) {
                if (arg instanceof Intent) {
                    return (Intent) arg;
                }
            }
            return null;
        }
    }

    @ProxyMethod("startActivities")
    public static class StartActivities extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int index = getIntents();
            Intent[] intents = (Intent[]) args[index++];
            String[] resolvedTypes = (String[]) args[index++];
            IBinder resultTo = (IBinder) args[index++];
            Bundle options = (Bundle) args[index];
            if (!ComponentUtils.isSelf(intents)) {
                return method.invoke(who, args);
            }
            for (Intent intent : intents) {
                intent.setExtrasClassLoader(who.getClass().getClassLoader());
            }
            return GreenBoxCore.getBActivityManager().startActivities(GActivityThread.getUserId(), intents, resolvedTypes, resultTo, options);
        }

        public int getIntents() {
            if (BuildCompat.isR()) {
                return 3;
            }
            return 2;
        }
    }

    @ProxyMethod("startIntentSenderForResult")
    public static class StartIntentSenderForResult extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("activityResumed")
    public static class ActivityResumed extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            GreenBoxCore.getBActivityManager().onActivityResumed((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("activityDestroyed")
    public static class ActivityDestroyed extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            GreenBoxCore.getBActivityManager().onActivityDestroyed((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("finishActivity")
    public static class FinishActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            GreenBoxCore.getBActivityManager().onFinishActivity((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getAppTasks")
    public static class GetAppTasks extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getCallingPackage")
    public static class getCallingPackage extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return GreenBoxCore.getBActivityManager().getCallingPackage((IBinder) args[0], GActivityThread.getUserId());
        }
    }

    @ProxyMethod("getCallingActivity")
    public static class getCallingActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return GreenBoxCore.getBActivityManager().getCallingActivity((IBinder) args[0], GActivityThread.getUserId());
        }
    }
}
