package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
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
import lunalib.lunaSettings.LunaSettings;

import java.util.*;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unused")
public class DMSShowShipPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        ArrayList<FleetMemberAPI> members = new ArrayList<>(Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy());
        int cols = Math.max(Math.min(members.size(), 7), 4);
        if (members.size() > 30) cols = 14; // More than 30 ships, so just go wide instead
        int rows = (members.size() - 1) / cols + 1;

        dialog.showFleetMemberPickerDialog(PICK_SHIP_TITLE, PICK_SHIP_OK_TEXT, PICK_SHIP_CANCEL_TEXT, rows, cols, 96, true, false, members, new FleetMemberPickerListener() {
            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                if (members.isEmpty()) return;

                FleetMemberAPI member = members.get(0);
                dialog.getVisualPanel().showFleetMemberInfo(member, false);

                MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
                localMemory.set(MEM_PICKED_SHIP, member, 0f);

                int pickOption = params.get(0).getInt(memoryMap);
                localMemory.set(MEM_OPTION_PICKED, pickOption, 0f);

                List<HullModSpecAPI> potentialDMods = getPotentialDMods(member.getVariant(), pickOption == 2, pickOption == 3);

                localMemory.unset(MEM_NOT_ELIGIBLE);
                if (DModManager.getNumDMods(member.getVariant()) >= DModManager.MAX_DMODS_FROM_COMBAT || potentialDMods.isEmpty())
                    localMemory.set(MEM_NOT_ELIGIBLE, "maxDMods", 0f);
                else if (pickOption == 3) { // When automating a ship
                    if (member.getVariant().hasHullMod(HullMods.AUTOMATED))
                        localMemory.set(MEM_NOT_ELIGIBLE, "alreadyAutomated", 0f);
                    else if (member == Global.getSector().getPlayerFleet().getFlagship())
                        localMemory.set(MEM_NOT_ELIGIBLE, "noAutoFlagship", 0f);
                    else if (!member.getCaptain().isDefault()) localMemory.set(MEM_NOT_ELIGIBLE, "officerInShip", 0f);
                    else for (HullModSpecAPI hullMod : DModManager.getModsWithTags(Tags.HULLMOD_NOT_AUTO))
                            if (member.getVariant().hasHullMod(hullMod.getId())) {
                                localMemory.set(MEM_NOT_ELIGIBLE, "incompatibleDMod", 0f);
                                break;
                            }
                }

                if (!localMemory.contains(MEM_NOT_ELIGIBLE)) {
                    localMemory.set(MEM_ELIGIBLE_DMODS, potentialDMods, 0f);

                    float credits = 0;
                    switch (pickOption) {
                        case 1: // Random D-Mod
                            credits = member.getStatus().getHullFraction() > 0.05f ? member.getStatus().getHullFraction() * member.getRepairTracker().getSuppliesFromScuttling() * Global.getSettings().getCommoditySpec(Commodities.SUPPLIES).getBasePrice() : 10f;

                            // Confirmation popup to prevent accidental confirms
                            dialog.getOptionPanel().addOptionConfirmation("dmodservicesRandomConfirm", CONFIRM_DMOD_RANDOM, CONFIRM_DMOD_YES, CONFIRM_DMOD_NO);
                            break;
                        case 2: // Selected D-Mod
                            Float multiplier;
                            if (LUNALIB_ENABLED) {
                                multiplier = LunaSettings.getFloat("dmodservices", "selectDModCostMult");
                                if (multiplier == null)
                                    multiplier = Global.getSettings().getFloat("dmodservicesSelectDModCostMult");
                            } else multiplier = Global.getSettings().getFloat("dmodservicesSelectDModCostMult");

                            float dModMultiplier = Math.min(DModManager.getNumDMods(member.getVariant()) * 0.15f + 0.4f, 1.0f);
                            credits = dModMultiplier * getPristineHullSpec(member).getBaseValue() * multiplier;
                            break;
                        case 3: // Automating ship
                            credits = getPristineHullSpec(member).getBaseValue() * 3.0f;

                            // Confirmation popup to prevent accidental confirms
                            dialog.getOptionPanel().addOptionConfirmation("dmodservicesAutomateConfirm", CONFIRM_AUTOMATE, CONFIRM_DMOD_YES, CONFIRM_DMOD_NO);
                            break;
                    }
                    localMemory.set(MEM_CREDITS, Misc.getDGSCredits(credits), 0f);
                }

                FireBest.fire(null, dialog, memoryMap, "DModServicesPickedShip");
            }

            @Override
            public void cancelledFleetMemberPicking() {
            }
        });

        return true;
    }

    // Gets the base value of the fleet member's pristine hull variant
    public static ShipHullSpecAPI getPristineHullSpec(FleetMemberAPI member) {
        ShipHullSpecAPI hullSpec = member.getHullSpec().getDParentHull(); // Get the (D) variant's pristine hull equivalent
        if (hullSpec == null) hullSpec = member.getHullSpec(); // Get base value for pristine hull
        return hullSpec;
    }

    // Similar implementation to DModManager's addDMods(), but returns a sorted list of eligible D-Mods
    public static List<HullModSpecAPI> getPotentialDMods(ShipVariantAPI variant, boolean canAddDestroyedMods, boolean assumeAllShipsAreAutomated) {
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

        // Destroyed ships always get these D-Mods, so put them in list if allowed
        if (canAddDestroyedMods) potentialMods.addAll(DModManager.getModsWithTags(Tags.HULLMOD_DESTROYED_ALWAYS));

        // No duplicate D-Mods
        DModManager.removeModsAlreadyInVariant(variant, potentialMods);

        Collections.sort(potentialMods, new Comparator<HullModSpecAPI>() {
            @Override
            public int compare(HullModSpecAPI h1, HullModSpecAPI h2) {
                return h1.getDisplayName().compareTo(h2.getDisplayName());
            }
        });

        return potentialMods;
    }
}