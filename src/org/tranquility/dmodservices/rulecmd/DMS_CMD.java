package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.ValueDisplayMode;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;
import org.tranquility.dmodservices.ui.DMSHullmodDelegate;
import org.tranquility.dmodservices.ui.DMSSelectDModPanel;
import org.tranquility.dmodservices.ui.DMSSelectHullmodPanel;
import org.tranquility.dmodservices.ui.DMSSelectSModPanel;

import java.util.*;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unused")
public class DMS_CMD extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        String action = params.get(0).getString(memoryMap);

        return switch (action) {
            case "addDModSlider" ->
                    addDModSlider(dialog, memoryMap.get(MemKeys.LOCAL), params.get(1).getString(memoryMap), params.get(2).getString(memoryMap));
            case "addOptionConfirmation" ->
                    addOptionConfirmation(dialog, params.get(1).getString(memoryMap), params.get(2).getStringWithTokenReplacement(ruleId, dialog, memoryMap));
            case "addRandomDMod" -> addRandomDMod(dialog, memoryMap.get(MemKeys.LOCAL));
            case "addSelectedDMod" -> addSelectedDMod(memoryMap.get(MemKeys.LOCAL));
            case "automate" -> automate(memoryMap.get(MemKeys.LOCAL));
            case "hasShipsProduction" -> hasShipsProduction(memoryMap.get(MemKeys.MARKET));
            case "removeSMod" -> removeSMod(memoryMap.get(MemKeys.LOCAL));
            case "showHullmodPicker" -> showHullmodPicker(dialog, memoryMap, params.get(1).getString(memoryMap));
            case "showPlayerFleet" -> showPlayerFleet(dialog);
            case "showShipPicker" -> showShipPicker(dialog, memoryMap, params.get(1).getString(memoryMap));
            default -> false;
        };
    }

    @SuppressWarnings("unchecked")
    private boolean addDModSlider(InteractionDialogAPI dialog, MemoryAPI memory, String text, String tooltip) {
        OptionPanelAPI panel = dialog.getOptionPanel();
        if (panel.hasSelector(OPT_NUM_DMOD_SELECTOR)) panel.removeOption(OPT_NUM_DMOD_SELECTOR);

        int maxAddDMods = Math.min(DModManager.MAX_DMODS_FROM_COMBAT - DModManager.getNumDMods(((FleetMemberAPI) memory.get(MEM_PICKED_SHIP)).getVariant()), ((List<HullModSpecAPI>) memory.get(MEM_ELIGIBLE_HULLMODS)).size());
        panel.addSelector(text, OPT_NUM_DMOD_SELECTOR, Misc.getNegativeHighlightColor(), 256, 48, 1, maxAddDMods, ValueDisplayMode.VALUE, tooltip);

        if (!memory.contains(MEM_SET_NUM_OF_DMODS)) memory.set(MEM_SET_NUM_OF_DMODS, 1, 0f);

        panel.setSelectorValue(OPT_NUM_DMOD_SELECTOR, Math.min(memory.getInt(MEM_SET_NUM_OF_DMODS), maxAddDMods));

        return true;
    }

    private boolean addOptionConfirmation(InteractionDialogAPI dialog, String optionId, String text) {
        dialog.getOptionPanel().addOptionConfirmation(optionId, text, CONFIRM_DMOD_YES, CONFIRM_DMOD_NO);
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean addRandomDMod(InteractionDialogAPI dialog, MemoryAPI memory) {
        FleetMemberAPI member = (FleetMemberAPI) memory.get(MEM_PICKED_SHIP);
        if (member.getStatus().getRandom() == null) member.getStatus().setRandom(new Random());

        List<HullModSpecAPI> potentialDMods = (List<HullModSpecAPI>) memory.get(MEM_ELIGIBLE_HULLMODS);
        member.getStatus().disable();
        member.getStatus().setHullFraction(0.01f); // Needed as entering combat with 0% hull can cause bugs

        StringBuilder display = new StringBuilder();
        int selectorValue = (int) dialog.getOptionPanel().getSelectorValue(OPT_NUM_DMOD_SELECTOR);
        memory.set(MEM_SET_NUM_OF_DMODS, selectorValue, 0f); // Makes the selected slider number persist
        String separator = ", ";
        for (int i = selectorValue; i > 0; i--) {
            HullModSpecAPI pickedDMod = potentialDMods.remove(member.getStatus().getRandom().nextInt(potentialDMods.size()));

            if (pickedDMod.hasTag(Tags.HULLMOD_DAMAGE_STRUCT))
                potentialDMods.removeAll(DModManager.getModsWithTags(Tags.HULLMOD_DAMAGE_STRUCT));

            addPermaMod(member.getVariant(), pickedDMod.getId());
            display.append(pickedDMod.getDisplayName()).append(separator);
        }
        display.delete(display.length() - separator.length(), display.length());
        memory.set(MEM_PICKED_HULLMODS_DISPLAY, display.toString(), 0f);

        member.getRepairTracker().setCR(0f);
        DModManager.setDHull(member.getVariant());

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean addSelectedDMod(MemoryAPI memory) {
        FleetMemberAPI member = (FleetMemberAPI) memory.get(MEM_PICKED_SHIP);
        List<HullModSpecAPI> pickedDMods = (List<HullModSpecAPI>) memory.get(MEM_PICKED_HULLMODS);
        DModManager.setDHull(member.getVariant());
        for (HullModSpecAPI picked : pickedDMods)
            addPermaMod(member.getVariant(), picked.getId());

        Boolean addUnrestorable;
        if (LUNALIB_ENABLED) {
            addUnrestorable = LunaSettings.getBoolean(MOD_ID, SETTING_SELECT_DMOD_ADD_UNRESTORABLE);
            if (addUnrestorable == null) addUnrestorable = getSettingBoolean(SETTING_SELECT_DMOD_ADD_UNRESTORABLE);
        } else addUnrestorable = getSettingBoolean(SETTING_SELECT_DMOD_ADD_UNRESTORABLE);

        if (addUnrestorable) member.getVariant().addTag(Tags.VARIANT_UNRESTORABLE);

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean automate(MemoryAPI memory) {
        FleetMemberAPI member = (FleetMemberAPI) memory.get(MEM_PICKED_SHIP);
        if (member.getStatus().getRandom() == null) member.getStatus().setRandom(new Random());

        List<HullModSpecAPI> potentialDMods = (List<HullModSpecAPI>) memory.get(MEM_ELIGIBLE_HULLMODS);
        HullModSpecAPI pickedDMod = potentialDMods.get(member.getStatus().getRandom().nextInt(potentialDMods.size()));
        addPermaMod(member.getVariant(), pickedDMod.getId());
        memory.set(MEM_PICKED_HULLMODS_DISPLAY, pickedDMod.getDisplayName(), 0f);

        member.getVariant().addPermaMod(HullMods.AUTOMATED);
        Boolean addNoAutoPenalty;
        if (LUNALIB_ENABLED) {
            addNoAutoPenalty = LunaSettings.getBoolean(MOD_ID, SETTING_AUTOMATE_ADD_NO_AUTO_PENALTY);
            if (addNoAutoPenalty == null) addNoAutoPenalty = getSettingBoolean(SETTING_AUTOMATE_ADD_NO_AUTO_PENALTY);
        } else addNoAutoPenalty = getSettingBoolean(SETTING_AUTOMATE_ADD_NO_AUTO_PENALTY);
        if (addNoAutoPenalty) member.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);

        Boolean addUnrestorable;
        if (LUNALIB_ENABLED) {
            addUnrestorable = LunaSettings.getBoolean(MOD_ID, SETTING_AUTOMATE_ADD_UNRESTORABLE);
            if (addUnrestorable == null) addUnrestorable = getSettingBoolean(SETTING_AUTOMATE_ADD_UNRESTORABLE);
        } else addUnrestorable = getSettingBoolean(SETTING_AUTOMATE_ADD_UNRESTORABLE);
        if (addUnrestorable) member.getVariant().addTag(Tags.VARIANT_UNRESTORABLE);

        member.getRepairTracker().setCR(0f);
        DModManager.setDHull(member.getVariant());

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }

    private boolean hasShipsProduction(MemoryAPI memory) {
        return memory.contains("$ind:" + Industries.ORBITALWORKS) || memory.contains("$ind:" + Industries.HEAVYINDUSTRY);
    }

    @SuppressWarnings("unchecked")
    private boolean removeSMod(MemoryAPI memory) {
        FleetMemberAPI member = (FleetMemberAPI) memory.get(MEM_PICKED_SHIP);
        List<HullModSpecAPI> pickedSMods = (List<HullModSpecAPI>) memory.get(MEM_PICKED_HULLMODS);
        for (HullModSpecAPI picked : pickedSMods)
            member.getVariant().removePermaMod(picked.getId());

        Boolean removeUnrestorable;
        if (LUNALIB_ENABLED) {
            removeUnrestorable = LunaSettings.getBoolean(MOD_ID, SETTING_REMOVE_SMOD_REMOVE_UNRESTORABLE);
            if (removeUnrestorable == null)
                removeUnrestorable = getSettingBoolean(SETTING_REMOVE_SMOD_REMOVE_UNRESTORABLE);
        } else removeUnrestorable = getSettingBoolean(SETTING_REMOVE_SMOD_REMOVE_UNRESTORABLE);
        if (removeUnrestorable && pickedSMods.size() >= Misc.MAX_PERMA_MODS)
            member.getVariant().removeTag(Tags.VARIANT_UNRESTORABLE);

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }

    private boolean showHullmodPicker(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, String option) {
        DMSSelectHullmodPanel panel = option.equals(Tags.HULLMOD_DMOD) ? new DMSSelectDModPanel() : new DMSSelectSModPanel();
        dialog.showCustomDialog(325f, 480f, new DMSHullmodDelegate(panel, dialog, memoryMap));
        return true;
    }

    private boolean showPlayerFleet(InteractionDialogAPI dialog) {
        dialog.getVisualPanel().showFleetInfo(null, Global.getSector().getPlayerFleet(), null, null);
        return true;
    }

    private boolean showShipPicker(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, String option) {
        List<FleetMemberAPI> members = new ArrayList<>(Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy());
        int cols = Math.max(Math.min(members.size(), 7), 4);
        if (members.size() > 35) cols = 12; // More than 35 ships, so just go wide instead
        int rows = (members.size() - 1) / cols + 1;

        dialog.showFleetMemberPickerDialog(PICK_SHIP_TITLE, PICK_SHIP_OK_TEXT, PICK_SHIP_CANCEL_TEXT, rows, cols, 96, true, false, members, new FleetMemberPickerListener() {
            @Override
            public void pickedFleetMembers(List<FleetMemberAPI> members) {
                if (members.isEmpty()) return;

                FleetMemberAPI member = members.get(0);
                dialog.getVisualPanel().showFleetMemberInfo(member, false);

                validateShip(member, option, memoryMap.get(MemKeys.LOCAL));

                FireBest.fire(null, dialog, memoryMap, "DMODServicesPickedShip");
            }

            @Override
            public void cancelledFleetMemberPicking() {
            }
        });

        return true;
    }

    private void validateShip(FleetMemberAPI member, String pickOption, MemoryAPI memory) {
        memory.set(MEM_PICKED_SHIP, member, 0f);
        memory.set(MEM_PICKED_SHIP_NAME, member.getShipName());
        memory.set(MEM_OPTION_PICKED, pickOption, 0f); // Store the selected option for later use

        List<HullModSpecAPI> potentialMods = !pickOption.equals("smods") ? getPotentialDMods(member.getVariant(), pickOption.equals("selection"), pickOption.equals("automate")) : getSMods(member);

        // Check for eligibility
        memory.unset(MEM_NOT_ELIGIBLE);
        if (pickOption.equals("smods")) {
            if (potentialMods.isEmpty()) memory.set(MEM_NOT_ELIGIBLE, "noSMods", 0f);
        } else if (pickOption.equals("automate")) {
            String autoReason = getAutomatedReason(member);
            if (!autoReason.isEmpty()) memory.set(MEM_NOT_ELIGIBLE, autoReason, 0f);
        } else if (DModManager.getNumDMods(member.getVariant()) >= DModManager.MAX_DMODS_FROM_COMBAT || potentialMods.isEmpty())
            memory.set(MEM_NOT_ELIGIBLE, "maxDMods", 0f);

        if (!memory.contains(MEM_NOT_ELIGIBLE)) {
            potentialMods.sort(Comparator.comparing(HullModSpecAPI::getDisplayName));
            memory.set(MEM_ELIGIBLE_HULLMODS, potentialMods, 0f);

            // Set credit price/gain based on picked option
            float credits = switch (pickOption) {
                case "random" ->
                        Math.max(0.75f * member.getStatus().getHullFraction() * member.getRepairTracker().getSuppliesFromScuttling() * Global.getSettings().getCommoditySpec(Commodities.SUPPLIES).getBasePrice(), 100f);
                case "selection" ->
                        getSelectDModScalingCostMult(DModManager.getNumDMods(member.getVariant())) * getPristineHullSpec(member).getBaseValue() * getSelectDModCostMultSetting();
                case "smods" -> getPristineHullSpec(member).getBaseValue() * getRemoveSModCostMultSetting();
                case "automate" -> getPristineHullSpec(member).getBaseValue() * getAutomateCostMultSetting();
                default -> 0f;
            };
            memory.set(MEM_CREDITS, Misc.getDGSCredits(credits), 0f);
        }
    }
}
