package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings({"unused", "unchecked"})
public class DModServicesAddRandomDMod extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);

        // Add a random D-Mod from the potential D-Mod list
        // TODO: set this to a defined Random to mitigate save-scumming
        List<HullModSpecAPI> potentialDMods = (List<HullModSpecAPI>) localMemory.get("$DModServices_eligibleDMods");
        String pickId = potentialDMods.get(new Random().nextInt(potentialDMods.size())).getId();

        FleetMemberAPI member = (FleetMemberAPI) localMemory.get("$DModServices_pickedShip");
        member.getVariant().removeSuppressedMod(pickId);
        member.getVariant().addPermaMod(pickId, false);

        // Set fleet member to disabled status
        member.getRepairTracker().setCR(0f);
        member.getStatus().disable();
        member.getStatus().setHullFraction(0.01f); // Need to do this since 0% hull can cause bugs when entering combat

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }
}
