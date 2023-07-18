package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("unused")
public class DModServicesAutomateShip extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        FleetMemberAPI member = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get("$DModServices_pickedShip");
        if (member.getStatus().getRandom() == null) member.getStatus().setRandom(new Random());

        // Adda a random D-Mod to the ship
        String[] potentialDMods = (String[]) memoryMap.get(MemKeys.LOCAL).get("$DModServices_eligibleDMods");
        String pickId = potentialDMods[member.getStatus().getRandom().nextInt(potentialDMods.length)];
        DModManager.setDHull(member.getVariant());
        member.getVariant().removeSuppressedMod(pickId);
        member.getVariant().addPermaMod(pickId, false);

        // Add the appropriate automated hull-mod + tags
        member.getVariant().addPermaMod(HullMods.AUTOMATED);
        member.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);
        member.getVariant().addTag(Tags.VARIANT_UNRESTORABLE);

        member.getRepairTracker().setCR(0f);

        memoryMap.get(MemKeys.LOCAL).set("$DModServices_pickedDModDisplay", Global.getSettings().getHullModSpec(pickId).getDisplayName(), 0f);

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }
}