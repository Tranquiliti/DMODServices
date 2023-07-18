package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DModServicesAddSelectedDMod extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        FleetMemberAPI member = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get("$DModServices_pickedShip");
        String pickId = memoryMap.get(MemKeys.LOCAL).getString("$DModServices_pickedDMod");
        DModManager.setDHull(member.getVariant());
        member.getVariant().removeSuppressedMod(pickId);
        member.getVariant().addPermaMod(pickId, false);

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