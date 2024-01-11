package org.tranquility.dmodservices;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

@SuppressWarnings("unused")
public class DMSModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) DMSLunaUtil.addSettingsListener();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Boolean enabled = null;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
            enabled = DMSLunaUtil.getBoolean("dmodservices", "enableDMODServices");
        if (enabled == null) enabled = Global.getSettings().getBoolean("dmodservicesEnableDMODServices");

        // Add memory key only if disabling, since enable is default and likely to stay true for most players
        // (This could/should be transient, but it's only one boolean memory key, so it shouldn't be a serious problem either way)
        if (!enabled) Global.getSector().getMemoryWithoutUpdate().set("$DModServices_disabled", true);
        else Global.getSector().getMemoryWithoutUpdate().unset("$DModServices_disabled");

        Boolean enableAutomate = null;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
            enableAutomate = DMSLunaUtil.getBoolean("dmodservices", "enableAutomateOption");
        if (enableAutomate == null)
            enableAutomate = Global.getSettings().getBoolean("dmodservicesEnableAutomateOption");

        // Similar deal here
        if (enableAutomate) Global.getSector().getMemoryWithoutUpdate().set("$DModServices_enableAutomate", true);
        else Global.getSector().getMemoryWithoutUpdate().unset("$DModServices_enableAutomate");
    }
}