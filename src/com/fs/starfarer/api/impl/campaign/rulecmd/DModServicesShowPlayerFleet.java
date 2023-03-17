package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DModServicesShowPlayerFleet extends BaseCommandPlugin {
    // A Rule Command to do just one thing XD
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        dialog.getVisualPanel().showFleetInfo(null, Global.getSector().getPlayerFleet(), null, null);
        return true;
    }
}
