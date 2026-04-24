package com.darkbox.fake.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import com.darkbox.core.system.user.BUserHandle;
import com.darkbox.utils.ArrayUtils;
import java.io.File;
import java.lang.reflect.Method;

import com.darkbox.VBoxCore;
import com.darkbox.app.BActivityThread;
import com.darkbox.fake.hook.MethodHook;
import com.darkbox.fake.hook.ProxyMethod;
import com.darkbox.fake.provider.FileProviderHandler;
import com.darkbox.proxy.ProxyWebActivity;
import com.darkbox.utils.ComponentUtils;
import com.darkbox.utils.FileUtils;
import com.darkbox.utils.MethodParameterUtils;
import com.darkbox.utils.Slog;
import com.darkbox.utils.compat.BuildCompat;
import com.darkbox.utils.compat.StartActivityCompat;
import static android.content.pm.PackageManager.GET_META_DATA;
import org.lsposed.lsparanoid.Obfuscate;
/**
 * Created by @jagdish_vip on 3/30/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
@Obfuscate
public class ActivityManagerCommonProxy {
    public static final String TAG = "ActivityManagerCommonProxy";

    @ProxyMethod("startActivity")
    public static class StartActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int intentIndex = ArrayUtils.indexOfObject(args, Intent.class, 1);
            int resultToIndex = ArrayUtils.indexOfObject(args, IBinder.class, 2);
            String resolvedType = (String) args[intentIndex + 1];
            Intent intent = new Intent((Intent) args[intentIndex]);
            intent.setDataAndType(intent.getData(), resolvedType);
            IBinder resultTo = resultToIndex >= 0 ? (IBinder) args[resultToIndex] : null;
            String resultWho = null;
            int requestCode = 0;
            Bundle options = ArrayUtils.getFirst(args, Bundle.class);
            if (resultTo != null) {
                resultWho = (String) args[resultToIndex + 1];
                requestCode = (int) args[resultToIndex + 2];
            }
            int userId = BUserHandle.myUserId();
            if (Intent.ACTION_MAIN.equals(intent.getAction()) && intent.hasCategory(Intent.CATEGORY_HOME)) {
                Intent homeIntent = null;
                if (homeIntent != null) {
                    args[intentIndex] = homeIntent;
                }
                return method.invoke(who, args);
            }
            String pkg = intent.getPackage();
            if (pkg != null && !VBoxCore.get().isInstalled(pkg,BActivityThread.getUserId())) {
                if (BuildCompat.isR() && "android.content.pm.action.REQUEST_PERMISSIONS".equals(intent.getAction())) {
                    args[intentIndex - 2] = VBoxCore.getHostPkg();
                }
                return method.invoke(who, args);
            }
            
            // Web view handling - HTTP/HTTPS URLs ko intercept karein
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                String url = intent.getDataString();
                if (url != null && (url.startsWith("http") || url.startsWith("https"))) {
                    // WebViewActivity launch karein
                    Intent webIntent = new Intent(BActivityThread.getApplication(), ProxyWebActivity.class);
                    webIntent.putExtra("url", url);
                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    BActivityThread.getApplication().startActivity(webIntent);
                    return 0;
                }
            }

            assert intent != null;
            if (intent.getParcelableExtra("_V_|_target_") != null) {
                return method.invoke(who, args);
            }
            if (ComponentUtils.isRequestInstall(intent)) {
                File file = FileProviderHandler.convertFile(BActivityThread.getApplication(), intent.getData());
                if (VBoxCore.get().requestInstallPackage(file)) {
                    return 0;
                }
                intent.setData(FileProviderHandler.convertFileUri(BActivityThread.getApplication(), intent.getData()));
                return method.invoke(who, args);
            }
            String dataString = intent.getDataString();
            if (dataString != null && dataString.equals("package:" + BActivityThread.getAppPackageName())) {
                intent.setData(Uri.parse("package:" + VBoxCore.getHostPkg()));
            }
            ResolveInfo resolveInfo = VBoxCore.getBPackageManager().resolveActivity(intent,GET_META_DATA,StartActivityCompat.getResolvedType(args),BActivityThread.getUserId());
            if (resolveInfo == null) {
                String origPackage = intent.getPackage();
                if (intent.getPackage() == null && intent.getComponent() == null) {
                    intent.setPackage(BActivityThread.getAppPackageName());
                } else {
                    origPackage = intent.getPackage();
                }
                resolveInfo = VBoxCore.getBPackageManager().resolveActivity(intent,GET_META_DATA,StartActivityCompat.getResolvedType(args),BActivityThread.getUserId());
                if (resolveInfo == null) {
                    intent.setPackage(origPackage);
                    return method.invoke(who, args);
                }
            }
            intent.setExtrasClassLoader(who.getClass().getClassLoader());
            intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
            VBoxCore.getBActivityManager().startActivityAms(BActivityThread.getUserId(),StartActivityCompat.getIntent(args),StartActivityCompat.getResolvedType(args),StartActivityCompat.getResultTo(args),StartActivityCompat.getResultWho(args),StartActivityCompat.getRequestCode(args),StartActivityCompat.getFlags(args),StartActivityCompat.getOptions(args));
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
            new Exception().printStackTrace();
            Intent[] intents = ArrayUtils.getFirst(args, Intent[].class);
            String[] resolvedTypes = ArrayUtils.getFirst(args, String[].class);
            IBinder token = null;
            int tokenIndex = ArrayUtils.indexOfObject(args, IBinder.class, 2);
            if (tokenIndex != -1) {
                token = (IBinder) args[tokenIndex];
            }
            Bundle options = ArrayUtils.getFirst(args, Bundle.class);
            return VBoxCore.getBActivityManager().startActivities(BActivityThread.getUserId(),intents, resolvedTypes, token, options);
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
            VBoxCore.getBActivityManager().onActivityResumed((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("activityDestroyed")
    public static class ActivityDestroyed extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            VBoxCore.getBActivityManager().onActivityDestroyed((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("finishActivity")
    public static class FinishActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            VBoxCore.getBActivityManager().onFinishActivity((IBinder) args[0]);
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
            return VBoxCore.getBActivityManager().getCallingPackage((IBinder) args[0], BActivityThread.getUserId());
        }
    }

    @ProxyMethod("getCallingActivity")
    public static class getCallingActivity extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return VBoxCore.getBActivityManager().getCallingActivity((IBinder) args[0], BActivityThread.getUserId());
        }
    }
}
