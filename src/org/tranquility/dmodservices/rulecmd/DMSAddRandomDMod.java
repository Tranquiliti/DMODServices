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

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unused")
public class DMSAddRandomDMod extends BaseCommandPlugin {
    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        FleetMemberAPI member = (FleetMemberAPI) localMemory.get(MEM_PICKED_SHIP);
        if (member.getStatus().getRandom() == null) member.getStatus().setRandom(new Random());

        List<HullModSpecAPI> potentialDMods = (List<HullModSpecAPI>) localMemory.get(MEM_ELIGIBLE_HULLMODS);
        member.getStatus().disable();
        member.getStatus().setHullFraction(0.01f); // Needed as entering combat with 0% hull can cause bugs

        StringBuilder display = new StringBuilder();
        int selectorValue = (int) dialog.getOptionPanel().getSelectorValue(OPT_NUM_DMOD_SELECTOR);
        localMemory.set(MEM_SET_NUM_OF_DMODS, selectorValue, 0f); // Makes the selected slider number persist
        String separator = ", ";
        for (int i = selectorValue; i > 0; i--) {
            HullModSpecAPI pickedDMod = potentialDMods.remove(member.getStatus().getRandom().nextInt(potentialDMods.size()));

            if (pickedDMod.hasTag(Tags.HULLMOD_DAMAGE_STRUCT))
                potentialDMods.removeAll(DModManager.getModsWithTags(Tags.HULLMOD_DAMAGE_STRUCT));

            addPermaMod(member.getVariant(), pickedDMod.getId());
            display.append(pickedDMod.getDisplayName()).append(separator);
        }
        display.delete(display.length() - separator.length(), display.length());
        localMemory.set(MEM_PICKED_HULLMODS_DISPLAY, display.toString(), 0f);

        member.getRepairTracker().setCR(0f);
        DModManager.setDHull(member.getVariant());

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }
}