package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "unchecked"})
public class DMSAddSelectedDMod extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        FleetMemberAPI member = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get("$DModServices_pickedShip");
        List<HullModSpecAPI> pickedDMods = (List<HullModSpecAPI>) memoryMap.get(MemKeys.LOCAL).get("$DModServices_pickedDMods");
        DModManager.setDHull(member.getVariant());
        for (HullModSpecAPI picked : pickedDMods) {
            member.getVariant().removeSuppressedMod(picked.getId());
            member.getVariant().addPermaMod(picked.getId(), false);
        }

        Boolean makeUnrestorable;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            makeUnrestorable = LunaSettings.getBoolean("dmodservices", "makeUnrestorable");
            if (makeUnrestorable == null)
                makeUnrestorable = Global.getSettings().getBoolean("dmodservicesMakeUnrestorable");
        } else makeUnrestorable = Global.getSettings().getBoolean("dmodservicesMakeUnrestorable");

        if (makeUnrestorable) member.getVariant().addTag(Tags.VARIANT_UNRESTORABLE);

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }
}