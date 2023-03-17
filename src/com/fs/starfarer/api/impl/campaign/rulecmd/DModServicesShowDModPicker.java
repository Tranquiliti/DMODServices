package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DModServicesShowDModPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        String[] potentialDMods = (String[]) localMemory.get("$DModServices_eligibleDMods");

        // TODO: replaces all of this with a custom panel or option selector
        // Interim solution for selecting D-Mods (hopefully until I set up a proper selection dialog or something) <- LOL
        if (!localMemory.contains("$DModServices_pickedDMod")) {
            localMemory.set("$DModServices_selectIndex", 0, 0f);
            dialog.getTextPanel().addParagraph("Selected ship can receive the following D-Mods:");
            StringBuilder list = new StringBuilder();
            for (String dModId : potentialDMods)
                list.append(Global.getSettings().getHullModSpec(dModId).getDisplayName()).append("\n");
            dialog.getTextPanel().addParagraph(list.toString(), Color.ORANGE);
        }

        int index = localMemory.getInt("$DModServices_selectIndex");
        String pickId = potentialDMods[index];
        index++;
        if (index == potentialDMods.length) index = 0;
        localMemory.set("$DModServices_selectIndex", index, 0f);
        localMemory.set("$DModServices_pickedDMod", pickId, 0f);

        dialog.getTextPanel().addParagraph("Selected D-Mod: " + Global.getSettings().getHullModSpec(pickId).getDisplayName(), Color.YELLOW);
        FireBest.fire(null, dialog, memoryMap, "DModServicesPickedDMod");
        return true;
    }
}
