package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DModServicesAddSelectedDMod extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        FleetMemberAPI member = (FleetMemberAPI) localMemory.get("$DModServices_pickedShip");
        String pickId = localMemory.getString("$DModServices_pickedDMod");
        member.getVariant().removeSuppressedMod(pickId);
        member.getVariant().addPermaMod(pickId, false);

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }
}
