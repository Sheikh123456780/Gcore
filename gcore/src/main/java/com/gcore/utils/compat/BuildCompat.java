package com.gcore.utils.compat;

import android.os.Build;

public class BuildCompat {

    // 16
    public static boolean isBaklava() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA || (Build.VERSION.SDK_INT >= 35 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 15
    public static boolean isVanillaIceCream() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM || (Build.VERSION.SDK_INT >= 34 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 14
    public static boolean isUpsideDownCake() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE || (Build.VERSION.SDK_INT >= 33 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 13
    public static boolean isTiramisu() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || (Build.VERSION.SDK_INT >= 32 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 12.1
    public static boolean isS_V2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 || (Build.VERSION.SDK_INT >= 31 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 12
    public static boolean isS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || (Build.VERSION.SDK_INT >= 30 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 11
    public static boolean isR() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || (Build.VERSION.SDK_INT >= 29 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 10
    public static boolean isQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || (Build.VERSION.SDK_INT >= 28 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 9
    public static boolean isPie() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || (Build.VERSION.SDK_INT >= 27 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 8.1
    public static boolean isOreo_MR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 || (Build.VERSION.SDK_INT >= 26 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 8
    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || (Build.VERSION.SDK_INT >= 25 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 7.1
    public static boolean isN_MR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 || (Build.VERSION.SDK_INT >= 24 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 7
    public static boolean isN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || (Build.VERSION.SDK_INT >= 23 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 6
    public static boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    // 5.1
    public static boolean isLollipop_MR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    // 5
    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isSamsung() {
        return "samsung".equalsIgnoreCase(Build.BRAND) || "samsung".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isEMUI() {
        if (Build.DISPLAY.toUpperCase().startsWith("EMUI")) {
            return true;
        }
        String property = SystemPropertiesCompat.get("ro.build.version.emui");
        return property != null && property.contains("EmotionUI");
    }

    public static boolean isMIUI() {
        return SystemPropertiesCompat.getInt("ro.miui.ui.version.code", 0) > 0;
    }

    public static boolean isFlyme() {
        return Build.DISPLAY.toLowerCase().contains("flyme");
    }

    public static boolean isColorOS() {
        return SystemPropertiesCompat.isExist("ro.build.version.opporom") || SystemPropertiesCompat.isExist("ro.rom.different.version");
    }

    public static boolean is360UI() {
        String property = SystemPropertiesCompat.get("ro.build.uiversion");
        return property != null && property.toUpperCase().contains("360UI");
    }

    public static boolean isLetv() {
        return Build.MANUFACTURER.equalsIgnoreCase("Letv");
    }

    public static boolean isVivo() {
        return SystemPropertiesCompat.isExist("ro.vivo.os.build.display.id");
    }

    private static ROMType sRomType;

    public static ROMType getROMType() {
        if (sRomType == null) {
            if (isEMUI()) {
                sRomType = ROMType.EMUI;
            } else if (isMIUI()) {
                sRomType = ROMType.MIUI;
            } else if (isFlyme()) {
                sRomType = ROMType.FLYME;
            } else if (isColorOS()) {
                sRomType = ROMType.COLOR_OS;
            } else if (is360UI()) {
                sRomType = ROMType._360;
            } else if (isLetv()) {
                sRomType = ROMType.LETV;
            } else if (isVivo()) {
                sRomType = ROMType.VIVO;
            } else if (isSamsung()) {
                sRomType = ROMType.SAMSUNG;
            } else {
                sRomType = ROMType.OTHER;
            }
        }
        return sRomType;
    }

    public enum ROMType {
        EMUI,
        MIUI,
        FLYME,
        COLOR_OS,
        LETV,
        VIVO,
        _360,
        SAMSUNG,
        OTHER
    }
}