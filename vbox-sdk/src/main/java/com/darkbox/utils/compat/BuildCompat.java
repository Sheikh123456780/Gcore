package com.darkbox.utils.compat;

import android.os.Build;
import android.view.Surface;

public class BuildCompat {

    public static int getPreviewSDKInt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return Build.VERSION.PREVIEW_SDK_INT;
            } catch (Throwable e) {
                // ignore
            }
        }
        return 0;
    }
    
    
    // 16
    public static boolean isBaklava() {
        return Build.VERSION.SDK_INT >= 36 || (Build.VERSION.SDK_INT >= 35 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }
    
    // 15
    public static boolean isVanillaIceCream() {
        return Build.VERSION.SDK_INT >= 35 || (Build.VERSION.SDK_INT >= 34 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }
    
    // 14
    public static boolean isUpsideDownCake() {
        return Build.VERSION.SDK_INT >= 34 || (Build.VERSION.SDK_INT >= 33 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }
    
    // 13
    public static boolean isT() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.VERSION.PREVIEW_SDK_INT == 1);
    }
    
    // 12.1
    public static boolean isS_V2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 12
    public static boolean isS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 11
    public static boolean isR() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 10
    public static boolean isQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 9
    public static boolean isPie() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }
    
    // 8.1
    public static boolean isOreo_MR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.PREVIEW_SDK_INT == 1);
    }
    
    // 8
    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 7.1
    public static boolean isN_MR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 7
    public static boolean isN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    // 6
    public static boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
    
    public static boolean isLollipop_MR1() {
        return Build.VERSION.SDK_INT >= 22;
    }

    // 5
    public static boolean isL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /* ---------------- ROM CHECK ---------------- */

    public static boolean isSamsung() {
        return "samsung".equalsIgnoreCase(Build.BRAND) || "samsung".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isEMUI() {
        try {
            if (Build.DISPLAY != null && Build.DISPLAY.toUpperCase().startsWith("EMUI")) {
                return true;
            }
            String prop = SystemPropertiesCompat.get("ro.build.version.emui");
            return prop != null && prop.contains("EmotionUI");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isMIUI() {
        try {
            return SystemPropertiesCompat.getInt("ro.miui.ui.version.code", 0) > 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isFlyme() {
        try {
            return Build.DISPLAY != null && Build.DISPLAY.toLowerCase().contains("flyme");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isColorOS() {
        try {
            return SystemPropertiesCompat.isExist("ro.build.version.opporom") || SystemPropertiesCompat.isExist("ro.rom.different.version");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean is360UI() {
        try {
            String prop = SystemPropertiesCompat.get("ro.build.uiversion");
            return prop != null && prop.toUpperCase().contains("360UI");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isLetv() {
        return "letv".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isVivo() {
        try {
            return SystemPropertiesCompat.isExist("ro.vivo.os.build.display.id");
        } catch (Throwable ignored) {
            return false;
        }
    }

    /* --------- TRANSSION (itel / Tecno / Infinix) --------- */

    public static boolean isTecno() {
        return "tecno".equalsIgnoreCase(Build.BRAND) || "tecno".equalsIgnoreCase(Build.MANUFACTURER) || SystemPropertiesCompat.isExist("ro.tecno.platform");
    }

    public static boolean isInfinix() {
        return "infinix".equalsIgnoreCase(Build.BRAND) || "infinix".equalsIgnoreCase(Build.MANUFACTURER) || SystemPropertiesCompat.isExist("ro.infinix.platform");
    }

    public static boolean isItel() {
        return "itel".equalsIgnoreCase(Build.BRAND) || "itel".equalsIgnoreCase(Build.MANUFACTURER) || SystemPropertiesCompat.isExist("ro.itel.platform");
    }

    public static boolean isHiOS() {
        if (Build.DISPLAY != null && Build.DISPLAY.toUpperCase().contains("HIOS")) {
            return true;
        }
        return SystemPropertiesCompat.isExist("ro.hios.version");
    }

    public static boolean isXOS() {
        if (Build.DISPLAY != null && Build.DISPLAY.toUpperCase().contains("XOS")) {
            return true;
        }
        return SystemPropertiesCompat.isExist("ro.xos.version");
    }

    /* ---------------- ROM TYPE ---------------- */

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
            } else if (isHiOS() || isTecno()) {
                sRomType = ROMType.HIOS;
            } else if (isXOS() || isInfinix()) {
                sRomType = ROMType.XOS;
            } else if (isItel()) {
                sRomType = ROMType.ITEL;
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
        SAMSUNG,
        HIOS,
        XOS,
        ITEL,
        _360,
        OTHER
    }
}