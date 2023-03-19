import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

public class DModServicesLunaSettingsListener implements LunaSettingsListener {
    public DModServicesLunaSettingsListener() {
        Global.getSector().getListenerManager().addListener(this, true);
    }

    @Override
    public void settingsChanged(String modId) {
        if (Boolean.FALSE.equals(LunaSettings.getBoolean(modId, "dmodservices_EnableDMODServices")))
            Global.getSector().getMemoryWithoutUpdate().set("$DModServices_disabled", true);
        else Global.getSector().getMemoryWithoutUpdate().unset("$DModServices_disabled");
    }
}
