package com.gcore.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.gcore.GreenBoxCore;
import com.gcore.app.GActivityThread;

import org.lsposed.lsparanoid.Obfuscate;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Obfuscate
public class CrashMonitor {

    private static final String TAG = "CrashMonitor";

    private static boolean sIsInitialized = false;

    private static final AtomicInteger sTotalCrashes = new AtomicInteger(0);
    private static final AtomicInteger sJavaCrashes = new AtomicInteger(0);
    private static final AtomicInteger sNativeCrashes = new AtomicInteger(0);
    private static final AtomicInteger sRecoveredCrashes = new AtomicInteger(0);

    private static final Map<String, CrashInfo> sCrashHistory = new HashMap<>();

    private static final Map<String, RecoveryStrategy> sRecoveryStrategies = new HashMap<>();

    private static boolean sIsMonitoring = false;
    private static Handler sMainHandler;

    public static class CrashInfo {
        public final String crashType;
        public final String packageName;
        public final String errorMessage;
        public final String stackTrace;
        public final long timestamp;
        public final boolean wasRecovered;

        public CrashInfo(String crashType, String packageName, String errorMessage, String stackTrace, boolean wasRecovered) {
            this.crashType = crashType;
            this.packageName = packageName;
            this.errorMessage = errorMessage;
            this.stackTrace = stackTrace;
            this.timestamp = System.currentTimeMillis();
            this.wasRecovered = wasRecovered;
        }
    }

    public interface RecoveryStrategy {
        String getName();

        boolean canHandle(String crashType, String errorMessage);

        boolean attemptRecovery(CrashInfo crashInfo);

        int getPriority();
    }

    public static void initialize() {
        if (sIsInitialized) {
            return;
        }
        try {
            sMainHandler = new Handler(Looper.getMainLooper());
            registerRecoveryStrategies();
            installGlobalCrashHandlers();
            startMonitoring();
            sIsInitialized = true;
        } catch (Exception e) {
            Slog.e(TAG, "error: " + e.getMessage(), e);
        }
    }

    private static void registerRecoveryStrategies() {
        try {
            sRecoveryStrategies.put("JavaException", new JavaExceptionRecovery());
            sRecoveryStrategies.put("NativeCrash", new NativeCrashRecovery());
            sRecoveryStrategies.put("DexCorruption", new DexCorruptionRecovery());
            sRecoveryStrategies.put("WebViewCrash", new WebViewCrashRecovery());
            sRecoveryStrategies.put("MemoryCrash", new MemoryCrashRecovery());
        } catch (Exception e) {
            Slog.e(TAG, "error: " + e.getMessage(), e);
        }
    }

    private static void installGlobalCrashHandlers() {
        try {
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> handleCrash("JavaException", thread, throwable));
            installSystemErrorHandler();
        } catch (Exception e) {
            Slog.e(TAG, "error: " + e.getMessage(), e);
        }
    }

    private static void installSystemErrorHandler() {

    }

    private static void startMonitoring() {
        if (sIsMonitoring) {
            return;
        }
        try {
            sIsMonitoring = true;
            startPeriodicHealthChecks();
            startCrashPatternAnalysis();
        } catch (Exception e) {
            Slog.e(TAG, "error: " + e.getMessage(), e);
        }
    }

    private static void startPeriodicHealthChecks() {
        if (sMainHandler != null) {
            sMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sMainHandler.postDelayed(this, 30000);
                }
            }, 30000);
        }
    }

    private static void startCrashPatternAnalysis() {
        if (sMainHandler != null) {
            sMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sMainHandler.postDelayed(this, 60000);
                }
            }, 60000);
        }
    }

    public static void handleCrash(String crashType, Thread thread, Throwable throwable) {
        try {
            sTotalCrashes.incrementAndGet();
            if (crashType.equals("JavaException")) {
                sJavaCrashes.incrementAndGet();
            } else if (crashType.equals("NativeCrash")) {
                sNativeCrashes.incrementAndGet();
            }
            CrashInfo crashInfo = createCrashInfo(crashType, thread, throwable);
            String crashKey = crashType + "_" + System.currentTimeMillis();
            sCrashHistory.put(crashKey, crashInfo);
            boolean recovered = attemptCrashRecovery(crashInfo);

            if (recovered) {
                sRecoveredCrashes.incrementAndGet();
                crashInfo = new CrashInfo(crashInfo.crashType, crashInfo.packageName, crashInfo.errorMessage, crashInfo.stackTrace, true);
                sCrashHistory.put(crashKey, crashInfo);
            } else {
                Slog.w(TAG, "error");
            }
        } catch (Exception e) {
            Slog.e(TAG, "error: " + e.getMessage(), e);
        }
    }

    private static CrashInfo createCrashInfo(String crashType, Thread thread, Throwable throwable) {
        try {
            String packageName = getCurrentPackageName();
            String errorMessage = throwable != null ? throwable.getMessage() : "Unknown error";
            String stackTrace = getStackTrace(throwable);
            return new CrashInfo(crashType, packageName, errorMessage, stackTrace, false);
        } catch (Exception e) {
            Slog.e(TAG, "error: " + e.getMessage(), e);
            return new CrashInfo(crashType, "unknown", "Error creating crash info", "", false);
        }
    }

    private static String getCurrentPackageName() {
        try {
            return GActivityThread.getAppPackageName();
        } catch (Exception e) {
            try {
                Context context = GreenBoxCore.getContext();
                if (context != null) {
                    return context.getPackageName();
                }
            } catch (Exception ex) {
                // Ignore
            }
            return "unknown";
        }
    }

    private static String getStackTrace(Throwable throwable) {
        if (throwable == null) return "";

        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    private static boolean attemptCrashRecovery(CrashInfo crashInfo) {
        try {
            for (RecoveryStrategy strategy : sRecoveryStrategies.values()) {
                if (strategy.canHandle(crashInfo.crashType, crashInfo.errorMessage)) {
                    if (strategy.attemptRecovery(crashInfo)) {
                        return true;
                    } else {
                        Slog.w(TAG, "error: " + strategy.getName());
                    }
                }
            }
            return false;

        } catch (Exception e) {
            Slog.e(TAG, "error: " + e.getMessage(), e);
            return false;
        }
    }

    private static class JavaExceptionRecovery implements RecoveryStrategy {
        @Override
        public String getName() {
            return "Java Exception Recovery";
        }

        @Override
        public boolean canHandle(String crashType, String errorMessage) {
            return crashType.equals("JavaException");
        }

        @Override
        public boolean attemptRecovery(CrashInfo crashInfo) {
            try {
                return true;
            } catch (Exception e) {
                Slog.w(TAG, "Java exception recovery failed: " + e.getMessage());
                return false;
            }
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }

    private static class NativeCrashRecovery implements RecoveryStrategy {
        @Override
        public String getName() {
            return "Native Crash Recovery";
        }

        @Override
        public boolean canHandle(String crashType, String errorMessage) {
            return crashType.equals("NativeCrash");
        }

        @Override
        public boolean attemptRecovery(CrashInfo crashInfo) {
            try {
                return true;
            } catch (Exception e) {
                Slog.e(TAG, "error: " + e.getMessage());
                return false;
            }
        }

        @Override
        public int getPriority() {
            return 90;
        }
    }

    private static class DexCorruptionRecovery implements RecoveryStrategy {
        @Override
        public String getName() {
            return "DEX Corruption Recovery";
        }

        @Override
        public boolean canHandle(String crashType, String errorMessage) {
            return errorMessage != null && (errorMessage.contains("classes.dex") || errorMessage.contains("ClassNotFoundException"));
        }

        @Override
        public boolean attemptRecovery(CrashInfo crashInfo) {
            try {
                return true;
            } catch (Exception e) {
                Slog.e(TAG, "error: " + e.getMessage());
                return false;
            }
        }

        @Override
        public int getPriority() {
            return 80;
        }
    }

    private static class WebViewCrashRecovery implements RecoveryStrategy {
        @Override
        public String getName() {
            return "WebView Crash Recovery";
        }

        @Override
        public boolean canHandle(String crashType, String errorMessage) {
            return errorMessage != null && (errorMessage.contains("WebView") || errorMessage.contains("webview"));
        }

        @Override
        public boolean attemptRecovery(CrashInfo crashInfo) {
            try {
                return true;
            } catch (Exception e) {
                Slog.e(TAG, "error: " + e.getMessage());
                return false;
            }
        }

        @Override
        public int getPriority() {
            return 70;
        }
    }

    private static class MemoryCrashRecovery implements RecoveryStrategy {
        @Override
        public String getName() {
            return "Memory Crash Recovery";
        }

        @Override
        public boolean canHandle(String crashType, String errorMessage) {
            return errorMessage != null && (errorMessage.contains("OutOfMemoryError") || errorMessage.contains("Memory") || errorMessage.contains("SIGSEGV"));
        }

        @Override
        public boolean attemptRecovery(CrashInfo crashInfo) {
            try {
                System.gc();
                return true;
            } catch (Exception e) {
                Slog.e(TAG, "error: " + e.getMessage());
                return false;
            }
        }

        @Override
        public int getPriority() {
            return 60;
        }
    }
}
