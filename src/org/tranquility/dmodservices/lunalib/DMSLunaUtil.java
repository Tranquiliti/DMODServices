package org.tranquility.dmodservices.lunalib;

import lunalib.lunaSettings.LunaSettings;

// Utility class to avoid a crash with importing LunaLib classes in an extended BaseModPlugin,
// causing the LunaLib soft dependency to become a hard dependency
public final class DMSLunaUtil {
    public static void addSettingsListener() {
        LunaSettings.addSettingsListener(new DMSLunaSettingsListener());
    }

    public static Boolean getBoolean(String modId, String fieldId) {
        return LunaSettings.getBoolean(modId, fieldId);
    }
}