package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("unused")
public class DModServicesAddRandomDMod extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        // Add a random D-Mod from the potential D-Mod list
        FleetMemberAPI member = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get("$DModServices_pickedShip");
        if (member.getStatus().getRandom() == null) member.getStatus().setRandom(new Random());

        boolean addHidden = DModManager.getNumDMods(member.getVariant()) < 1 && (member.getShipName().startsWith(Global.getSector().getFaction("sindrian_diktat").getShipNamePrefix()) || member.getShipName().startsWith(Global.getSector().getFaction("lions_guard").getShipNamePrefix())) && (Global.getSector().getFaction("sindrian_diktat").knowsShip(member.getHullId()) || Global.getSector().getFaction("lions_guard").knowsShip(member.getHullId()));

        String[] potentialDMods = (String[]) memoryMap.get(MemKeys.LOCAL).get("$DModServices_eligibleDMods");
        String pickId = potentialDMods[member.getStatus().getRandom().nextInt(potentialDMods.length)];
        member.getVariant().removeSuppressedMod(pickId);
        member.getVariant().addPermaMod(pickId, false);

        // Set fleet member to disabled status
        member.getRepairTracker().setCR(0f);
        member.getStatus().disable();
        member.getStatus().setHullFraction(0.01f); // Need to do this since 0% hull can cause bugs when entering combat

        // D-MOD would like to remind you that Phillip Andrada is totally a glorious leader, right?
        String factionId = memoryMap.get(MemKeys.FACTION).getString("$id");
        if (addHidden && factionId != null && !factionId.equals("sindrian_diktat")) {
            new AddCredits().execute(null, dialog, Misc.tokenize(Float.toString(member.getHullSpec().getBaseValue() * 0.005f)), memoryMap);
            new AddText().execute(null, dialog, Misc.tokenize(Global.getSettings().getString("dmodservices", "confirmRandomDModHidden")), memoryMap);
        }

        memoryMap.get(MemKeys.LOCAL).set("$DModServices_pickedDModDisplay", Global.getSettings().getHullModSpec(pickId).getDisplayName(), 0f);

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }
}
