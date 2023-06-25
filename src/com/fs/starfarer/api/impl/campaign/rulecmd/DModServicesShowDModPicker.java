package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DModServicesShowDModPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        dialog.showCustomDialog(325f, 480f, new DModServicesCustomDialogDelegate(dialog, memoryMap));

        return true;
    }
}