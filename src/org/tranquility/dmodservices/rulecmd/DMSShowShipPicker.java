package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unused")
public class DMSShowShipPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        List<FleetMemberAPI> members = new ArrayList<>(Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy());
        int cols = Math.max(Math.min(members.size(), 7), 4);
        if (members.size() > 30) cols = 12; // More than 30 ships, so just go wide instead
        int rows = (members.size() - 1) / cols + 1;

        dialog.showFleetMemberPickerDialog(PICK_SHIP_TITLE, PICK_SHIP_OK_TEXT, PICK_SHIP_CANCEL_TEXT, rows, cols, 96, true, false, members, new FleetMemberPickerListener() {
            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                if (members.isEmpty()) return;

                FleetMemberAPI member = members.get(0);
                memoryMap.get(MemKeys.LOCAL).set(MEM_PICKED_SHIP, member, 0f);
                memoryMap.get(MemKeys.LOCAL).set(MEM_PICKED_SHIP_NAME, member.getShipName());
                dialog.getVisualPanel().showFleetMemberInfo(member, false);

                validateShip(member, dialog, params, memoryMap);

                FireBest.fire(null, dialog, memoryMap, "DModServicesPickedShip");
            }

            @Override
            public void cancelledFleetMemberPicking() {
            }
        });

        return true;
    }

    private void validateShip(FleetMemberAPI member, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        int pickOption = params.get(0).getInt(memoryMap);
        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        localMemory.set(MEM_OPTION_PICKED, pickOption, 0f); // Store the selected option for later use

        List<HullModSpecAPI> potentialMods = pickOption != 4 ? getPotentialDMods(member.getVariant(), pickOption == 2, pickOption == 3) : getSMods(member);

        // Check for eligibility
        localMemory.unset(MEM_NOT_ELIGIBLE);
        if (pickOption == 4 && potentialMods.isEmpty()) localMemory.set(MEM_NOT_ELIGIBLE, "noSMods", 0f);
        else if (DModManager.getNumDMods(member.getVariant()) >= DModManager.MAX_DMODS_FROM_COMBAT || potentialMods.isEmpty())
            localMemory.set(MEM_NOT_ELIGIBLE, "maxDMods", 0f);
        else if (pickOption == 3) { // When automating a ship
            String autoReason = canBeAutomated(member);
            if (!autoReason.isEmpty()) localMemory.set(MEM_NOT_ELIGIBLE, autoReason, 0f);
        }

        if (!localMemory.contains(MEM_NOT_ELIGIBLE)) {
            potentialMods.sort(Comparator.comparing(HullModSpecAPI::getDisplayName));
            localMemory.set(MEM_ELIGIBLE_HULLMODS, potentialMods, 0f);

            // Set credit price/gain based on picked option
            float credits = switch (pickOption) {
                case 1 -> // Random d-mod
                        Math.max(0.75f * member.getStatus().getHullFraction() * member.getRepairTracker().getSuppliesFromScuttling() * Global.getSettings().getCommoditySpec(Commodities.SUPPLIES).getBasePrice(), 100f);
                case 2 -> // Selected d-mod
                        getSelectDModScalingCostMult(DModManager.getNumDMods(member.getVariant())) * getPristineHullSpec(member).getBaseValue() * getSelectDModCostMultSetting();
                case 3 -> // Automating ship
                        getPristineHullSpec(member).getBaseValue() * getAutomateCostMultSetting();
                case 4 -> // Removing s-mod
                        getPristineHullSpec(member).getBaseValue() * getRemoveSModCostMultSetting();
                default -> 0f;
            };
            localMemory.set(MEM_CREDITS, Misc.getDGSCredits(credits), 0f);
        }
    }

    // Similar implementation to DModManager's addDMods(), but simply returns a list of eligible d-mods
    private List<HullModSpecAPI> getPotentialDMods(ShipVariantAPI variant, boolean canAddDestroyedMods, boolean assumeAllShipsAreAutomated) {
        List<HullModSpecAPI> potentialMods = DModManager.getModsWithTags(Tags.HULLMOD_DAMAGE);
        boolean prevAssume = DModManager.assumeAllShipsAreAutomated;
        DModManager.assumeAllShipsAreAutomated = assumeAllShipsAreAutomated; // Similar hack in PKDefenderPluginImpl.java
        DModManager.removeUnsuitedMods(variant, potentialMods);
        DModManager.assumeAllShipsAreAutomated = prevAssume;

        if (DModManager.getNumDMods(variant, Tags.HULLMOD_DAMAGE_STRUCT) > 0)
            potentialMods = DModManager.getModsWithoutTags(potentialMods, Tags.HULLMOD_DAMAGE_STRUCT);

        if (variant.getHullSpec().getFighterBays() > 0)
            potentialMods.addAll(DModManager.getModsWithTags(Tags.HULLMOD_FIGHTER_BAY_DAMAGE));

        if (variant.getHullSpec().isPhase())
            potentialMods.addAll(DModManager.getModsWithTags(Tags.HULLMOD_DAMAGE_PHASE));

        if (variant.isCarrier()) potentialMods.addAll(DModManager.getModsWithTags(Tags.HULLMOD_CARRIER_ALWAYS));

        // Destroyed ships always get these d-mods, so put them in list if allowed
        if (canAddDestroyedMods) potentialMods.addAll(DModManager.getModsWithTags(Tags.HULLMOD_DESTROYED_ALWAYS));

        // No duplicate d-mods
        DModManager.removeModsAlreadyInVariant(variant, potentialMods);

        return potentialMods;
    }

    private List<HullModSpecAPI> getSMods(FleetMemberAPI member) {
        List<HullModSpecAPI> potentialMods = new ArrayList<>(3);
        for (String id : member.getVariant().getSMods())
            potentialMods.add(Global.getSettings().getHullModSpec(id));

        return potentialMods;
    }

    private String canBeAutomated(FleetMemberAPI member) {
        if (member.getVariant().hasHullMod(HullMods.AUTOMATED)) return "alreadyAutomated";
        if (member == Global.getSector().getPlayerFleet().getFlagship()) return "noAutoFlagship";
        if (!member.getCaptain().isDefault()) return "officerInShip";
        for (String wingId : member.getVariant().getNonBuiltInWings())
            if (!Global.getSettings().getFighterWingSpec(wingId).hasTag(Tags.AUTOMATED_FIGHTER))
                return "fightersInShip";
        for (HullModSpecAPI hullMod : DModManager.getModsWithTags(Tags.HULLMOD_NOT_AUTO))
            if (member.getVariant().hasHullMod(hullMod.getId())) return "incompatibleDMod";

        return "";
    }
}