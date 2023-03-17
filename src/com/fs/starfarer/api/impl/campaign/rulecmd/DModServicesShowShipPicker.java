package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.*;

@SuppressWarnings("unused")
public class DModServicesShowShipPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        ArrayList<FleetMemberAPI> members = new ArrayList<>(Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy());

        String title = Global.getSettings().getString("dmodservices", "pickShipTitle");
        String okText = Global.getSettings().getString("dmodservices", "pickShipOkText");
        String cancelText = Global.getSettings().getString("dmodservices", "pickShipCancelText");
        int cols = 5;
        int rows = (members.size() - 1) / cols + 1;

        dialog.showFleetMemberPickerDialog(title, okText, cancelText, rows, cols, 96, true, false, members, new FleetMemberPickerListener() {
            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                if (members.isEmpty()) return;

                FleetMemberAPI member = members.get(0);
                dialog.getVisualPanel().showFleetMemberInfo(member, false);

                MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
                localMemory.set("$DModServices_pickedShip", member, 0f);

                boolean isRandom = params.get(0).getBoolean(memoryMap);
                localMemory.set("$DModServices_doRandomDMod", isRandom, 0f);

                List<HullModSpecAPI> potentialDMods = getPotentialDMods(member.getVariant(), !isRandom);

                if (DModManager.getNumDMods(member.getVariant()) >= DModManager.MAX_DMODS_FROM_COMBAT || potentialDMods.isEmpty())
                    localMemory.set("$DModServices_notEligible", true, 0f);
                else {
                    localMemory.unset("$DModServices_notEligible");

                    String[] dModIds = new String[potentialDMods.size()];
                    for (int i = 0; i < dModIds.length; i++) dModIds[i] = potentialDMods.get(i).getId();
                    localMemory.set("$DModServices_eligibleDMods", dModIds, 0f);
                }


                float credits;
                if (isRandom) {
                    // TODO: make this more accurate by calculating hull repair time
                    if (member.getStatus().getHullFraction() > 0.05f)
                        credits = member.getStatus().getHullFraction() * member.getRepairTracker().getSuppliesFromScuttling() * Global.getSettings().getCommoditySpec("supplies").getBasePrice();
                    else credits = 10f;

                    // D-MOD would like to remind you that Phillip Andrada is totally a glorious leader
                    if (DModManager.getNumDMods(member.getVariant()) < 1 && (member.getShipName().startsWith(Global.getSector().getFaction("sindrian_diktat").getShipNamePrefix()) || member.getShipName().startsWith(Global.getSector().getFaction("lions_guard").getShipNamePrefix())) && (Global.getSector().getFaction("sindrian_diktat").knowsShip(member.getHullId()) || Global.getSector().getFaction("lions_guard").knowsShip(member.getHullId())))
                        localMemory.set("$DModServices_creditTip", member.getHullSpec().getBaseValue() * 0.005f, 0f);
                    else localMemory.unset("$DModServices_creditTip");
                } else credits = member.getHullSpec().getBaseValue();
                localMemory.set("$DModServices_credits", Misc.getWithDGS(credits), 0f);

                FireBest.fire(null, dialog, memoryMap, "DModServicesPickedShip");
            }

            @Override
            public void cancelledFleetMemberPicking() {
            }
        });

        return true;
    }

    // Similar implementation to DModManager's addDMods(), but returns a sorted list of eligible D-Mods
    public static List<HullModSpecAPI> getPotentialDMods(ShipVariantAPI variant, boolean canAddDestroyedMods) {
        List<HullModSpecAPI> potentialMods = DModManager.getModsWithTags(Tags.HULLMOD_DAMAGE);
        DModManager.removeUnsuitedMods(variant, potentialMods);

        if (DModManager.getNumDMods(variant, Tags.HULLMOD_DAMAGE_STRUCT) > 0)
            potentialMods = DModManager.getModsWithoutTags(potentialMods, Tags.HULLMOD_DAMAGE_STRUCT);

        if (variant.getHullSpec().getFighterBays() > 0)
            potentialMods.addAll(DModManager.getModsWithTags(Tags.HULLMOD_FIGHTER_BAY_DAMAGE));

        if (variant.getHullSpec().isPhase())
            potentialMods.addAll(DModManager.getModsWithTags(Tags.HULLMOD_DAMAGE_PHASE));

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
