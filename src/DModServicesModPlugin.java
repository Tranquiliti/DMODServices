import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;

@SuppressWarnings("unused")
public class DModServicesModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        Boolean enabled = null;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            enabled = LunaSettings.getBoolean("dmodservices", "enableDMODServices");
            new DModServicesLunaSettingsListener();
        }

        if (enabled == null) enabled = Global.getSettings().getBoolean("dmodservicesEnableDMODServices");

        // Add memory key only if disabling, since enable is default and likely to stay true for most players
        // (This could/should be transient, but it's only one boolean memory key, so it shouldn't be a serious problem either way)
        if (!enabled) Global.getSector().getMemoryWithoutUpdate().set("$DModServices_disabled", true);
        else Global.getSector().getMemoryWithoutUpdate().unset("$DModServices_disabled");
    }
}