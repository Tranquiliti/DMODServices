package org.tranquility.dmodservices;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import org.tranquility.dmodservices.lunalib.DMSLunaSettingsListener;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unused")
public class DMSModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        if (LUNALIB_ENABLED) LunaSettings.addSettingsListener(new DMSLunaSettingsListener());
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Boolean enabled = null;
        if (LUNALIB_ENABLED) enabled = LunaSettings.getBoolean(MOD_ID, "enableDMODServices");
        if (enabled == null) enabled = Global.getSettings().getBoolean("dmodservicesEnableDMODServices");

        // Add memory key only if disabling, since enable is default and likely to stay true for most players
        // (This could/should be transient, but it's only one boolean memory key, so it shouldn't be a serious problem either way)
        if (!enabled) Global.getSector().getMemoryWithoutUpdate().set(MEM_DISABLED, true);
        else Global.getSector().getMemoryWithoutUpdate().unset(MEM_DISABLED);

        Boolean enableAutomate = null;
        if (LUNALIB_ENABLED) enableAutomate = LunaSettings.getBoolean(MOD_ID, "enableAutomateOption");
        if (enableAutomate == null)
            enableAutomate = Global.getSettings().getBoolean("dmodservicesEnableAutomateOption");

        // Similar deal here
        if (enableAutomate) Global.getSector().getMemoryWithoutUpdate().set(MEM_ENABLE_AUTOMATE, true);
        else Global.getSector().getMemoryWithoutUpdate().unset(MEM_ENABLE_AUTOMATE);
    }
}