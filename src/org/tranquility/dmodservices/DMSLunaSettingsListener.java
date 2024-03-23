package org.tranquility.dmodservices;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

import static org.tranquility.dmodservices.DMSUtil.MEM_DISABLED;
import static org.tranquility.dmodservices.DMSUtil.MEM_ENABLE_AUTOMATE;

public class DMSLunaSettingsListener implements LunaSettingsListener {
    @Override
    public void settingsChanged(String modId) {
        if (Global.getCurrentState() != GameState.CAMPAIGN) return;

        if (Boolean.FALSE.equals(LunaSettings.getBoolean(modId, "enableDMODServices")))
            Global.getSector().getMemoryWithoutUpdate().set(MEM_DISABLED, true);
        else Global.getSector().getMemoryWithoutUpdate().unset(MEM_DISABLED);

        if (Boolean.TRUE.equals(LunaSettings.getBoolean(modId, "enableAutomateOption")))
            Global.getSector().getMemoryWithoutUpdate().set(MEM_ENABLE_AUTOMATE, true);
        else Global.getSector().getMemoryWithoutUpdate().unset(MEM_ENABLE_AUTOMATE);
    }
}