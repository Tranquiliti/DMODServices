package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddCredits;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddText;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings({"unused", "unchecked"})
public class DMSAddRandomDMod extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        FleetMemberAPI member = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get(MEM_PICKED_SHIP);
        if (member.getStatus().getRandom() == null) member.getStatus().setRandom(new Random());

        boolean isAutomated = params.get(0).getBoolean(memoryMap);
        if (!isAutomated) {
            member.getStatus().disable();
            member.getStatus().setHullFraction(0.01f); // Needed as entering combat with 0% hull can cause bugs

            if (isHiddenEligible(member, memoryMap.get(MemKeys.FACTION).getString("$id"))) {
                new AddCredits().execute(null, dialog, Misc.tokenize(Float.toString(member.getHullSpec().getBaseValue() * 0.005f)), memoryMap);
                new AddText().execute(null, dialog, Misc.tokenize(CONFIRM_RANDOM_DMOD_HIDDEN), memoryMap);
            }
        } else {
            member.getVariant().addPermaMod(HullMods.AUTOMATED);
            member.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);
            member.getVariant().addTag(Tags.VARIANT_UNRESTORABLE);
        }
        member.getRepairTracker().setCR(0f);

        List<HullModSpecAPI> potentialDMods = (List<HullModSpecAPI>) memoryMap.get(MemKeys.LOCAL).get(MEM_ELIGIBLE_DMODS);
        HullModSpecAPI pickedDMod = potentialDMods.get(member.getStatus().getRandom().nextInt(potentialDMods.size()));
        memoryMap.get(MemKeys.LOCAL).set(MEM_PICKED_DMOD_DISPLAY, pickedDMod.getDisplayName(), 0f);
        DModManager.setDHull(member.getVariant());
        member.getVariant().removeSuppressedMod(pickedDMod.getId());
        member.getVariant().addPermaMod(pickedDMod.getId(), false);

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }

    private boolean isHiddenEligible(FleetMemberAPI member, String factionId) {
        if (factionId == null || factionId.equals(Factions.DIKTAT)) return false;
        return DModManager.getNumDMods(member.getVariant()) < 1 && (member.getShipName().startsWith(Global.getSector().getFaction(Factions.DIKTAT).getShipNamePrefix()) || member.getShipName().startsWith(Global.getSector().getFaction(Factions.LIONS_GUARD).getShipNamePrefix())) && (Global.getSector().getFaction(Factions.DIKTAT).knowsShip(member.getHullId()) || Global.getSector().getFaction(Factions.LIONS_GUARD).knowsShip(member.getHullId()));
    }
}