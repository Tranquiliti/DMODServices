import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

public class DModServicesLunaSettingsListener implements LunaSettingsListener {
    @Override
    public void settingsChanged(String modId) {
        if (Global.getCurrentState() != GameState.CAMPAIGN) return;

        if (Boolean.FALSE.equals(LunaSettings.getBoolean(modId, "enableDMODServices")))
            Global.getSector().getMemoryWithoutUpdate().set("$DModServices_disabled", true);
        else Global.getSector().getMemoryWithoutUpdate().unset("$DModServices_disabled");

        if (Boolean.TRUE.equals(LunaSettings.getBoolean(modId, "enableAutomateOption")))
            Global.getSector().getMemoryWithoutUpdate().set("$DModServices_enableAutomate", true);
        else Global.getSector().getMemoryWithoutUpdate().unset("$DModServices_enableAutomate");
    }
}