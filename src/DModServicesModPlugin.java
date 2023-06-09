import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;

@SuppressWarnings("unused")
public class DModServicesModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
            LunaSettings.addSettingsListener(new DModServicesLunaSettingsListener());
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Boolean enabled = null;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
            enabled = LunaSettings.getBoolean("dmodservices", "enableDMODServices");
        if (enabled == null) enabled = Global.getSettings().getBoolean("dmodservicesEnableDMODServices");

        // Add memory key only if disabling, since enable is default and likely to stay true for most players
        // (This could/should be transient, but it's only one boolean memory key, so it shouldn't be a serious problem either way)
        if (!enabled) Global.getSector().getMemoryWithoutUpdate().set("$DModServices_disabled", true);
        else Global.getSector().getMemoryWithoutUpdate().unset("$DModServices_disabled");

        Boolean enableAutomate = null;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
            enableAutomate = LunaSettings.getBoolean("dmodservices", "enableAutomateOption");
        if (enableAutomate == null)
            enableAutomate = Global.getSettings().getBoolean("dmodservicesEnableAutomateOption");

        // Similar deal here
        if (enableAutomate) Global.getSector().getMemoryWithoutUpdate().set("$DModServices_enableAutomate", true);
        else Global.getSector().getMemoryWithoutUpdate().unset("$DModServices_enableAutomate");
    }
}