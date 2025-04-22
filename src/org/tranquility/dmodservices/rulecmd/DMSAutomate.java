package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unused")
public class DMSAutomate extends BaseCommandPlugin {
    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        FleetMemberAPI member = (FleetMemberAPI) localMemory.get(MEM_PICKED_SHIP);
        if (member.getStatus().getRandom() == null) member.getStatus().setRandom(new Random());

        List<HullModSpecAPI> potentialDMods = (List<HullModSpecAPI>) localMemory.get(MEM_ELIGIBLE_HULLMODS);
        HullModSpecAPI pickedDMod = potentialDMods.get(member.getStatus().getRandom().nextInt(potentialDMods.size()));
        addPermaMod(member.getVariant(), pickedDMod.getId());
        localMemory.set(MEM_PICKED_HULLMODS_DISPLAY, pickedDMod.getDisplayName(), 0f);

        member.getVariant().addPermaMod(HullMods.AUTOMATED);
        member.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);
        member.getVariant().addTag(Tags.VARIANT_UNRESTORABLE);
        member.getRepairTracker().setCR(0f);
        DModManager.setDHull(member.getVariant());

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }
}