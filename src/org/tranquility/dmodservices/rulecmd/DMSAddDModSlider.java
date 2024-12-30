package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.ValueDisplayMode;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unused")
public class DMSAddDModSlider extends BaseCommandPlugin {
    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        OptionPanelAPI opt = dialog.getOptionPanel();
        if (opt.hasSelector(OPT_NUM_DMOD_SELECTOR)) opt.removeOption(OPT_NUM_DMOD_SELECTOR);

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        int maxAddDMods = Math.min(DModManager.MAX_DMODS_FROM_COMBAT - DModManager.getNumDMods(((FleetMemberAPI) localMemory.get(MEM_PICKED_SHIP)).getVariant()), ((List<HullModSpecAPI>) localMemory.get(MEM_ELIGIBLE_HULLMODS)).size());
        opt.addSelector(params.get(0).getString(memoryMap), OPT_NUM_DMOD_SELECTOR, Misc.getNegativeHighlightColor(), 256, 48, 1, maxAddDMods, ValueDisplayMode.VALUE, params.get(1).getString(memoryMap));

        if (!localMemory.contains(MEM_SET_NUM_OF_DMODS)) localMemory.set(MEM_SET_NUM_OF_DMODS, 1, 0f);

        opt.setSelectorValue(OPT_NUM_DMOD_SELECTOR, Math.min(localMemory.getInt(MEM_SET_NUM_OF_DMODS), maxAddDMods));

        return true;
    }
}