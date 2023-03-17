package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings({"unused", "unchecked"})
public class DModServicesShowDModPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        FleetMemberAPI member = (FleetMemberAPI) localMemory.get("$DModServices_pickedShip");
        List<HullModSpecAPI> potentialDMods = (List<HullModSpecAPI>) localMemory.get("$DModServices_eligibleDMods");

        String pickId = potentialDMods.get(new Random().nextInt(potentialDMods.size())).getId();
        if(!localMemory.contains("$DModServices_pickedDMod")) { // Test code
            // Debugging
            dialog.getTextPanel().addParagraph("Ship can receive the following D-Mods:");
            StringBuilder list = new StringBuilder();
            for (HullModSpecAPI mod : potentialDMods) list.append(mod.getDisplayName()).append("\n");
            dialog.getTextPanel().addParagraph(list.toString(), Color.ORANGE);
        }
        localMemory.set("$DModServices_pickedDMod", pickId);

        // TODO: add a custom panel or option selector here

        // Temporary test code (I hope)
        dialog.getTextPanel().addParagraph("Selected D-Mod: " + Global.getSettings().getHullModSpec(pickId).getDisplayName(), Color.YELLOW);
        FireBest.fire(null, dialog, memoryMap, "DModServicesPickedDMod");
        return true;
    }
}
