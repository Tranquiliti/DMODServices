package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

                    float credits = 0f;
                    switch (pickOption) {
                        case 1: // Random D-Mod
                            credits = member.getStatus().getHullFraction() > 0.05f ? member.getStatus().getHullFraction() * member.getRepairTracker().getSuppliesFromScuttling() * Global.getSettings().getCommoditySpec(Commodities.SUPPLIES).getBasePrice() : 10f;

                            // Confirmation popup to prevent accidental confirms
                            dialog.getOptionPanel().addOptionConfirmation("dmodservicesRandomConfirm", CONFIRM_DMOD_RANDOM, CONFIRM_DMOD_YES, CONFIRM_DMOD_NO);
                            break;
                        case 2: // Selected D-Mod
                            Float multiplier;
                            if (LUNALIB_ENABLED) {
                                multiplier = LunaSettings.getFloat(MOD_ID, "selectDModCostMult");
                                if (multiplier == null)
                                    multiplier = Global.getSettings().getFloat("dmodservicesSelectDModCostMult");
                            } else multiplier = Global.getSettings().getFloat("dmodservicesSelectDModCostMult");

                            credits = getSelectDModCostMult(DModManager.getNumDMods(member.getVariant())) * getPristineHullSpec(member).getBaseValue() * multiplier;
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
}