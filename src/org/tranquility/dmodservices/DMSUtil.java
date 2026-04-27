package org.tranquility.dmodservices;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public final class DMSUtil {
    // strings.json Strings
    private static final String STRINGS_CATEGORY = "dmodservices";
    public static final String PICK_SHIP_TITLE = Global.getSettings().getString(STRINGS_CATEGORY, "pickShipTitle");
    public static final String PICK_SHIP_OK_TEXT = Global.getSettings().getString(STRINGS_CATEGORY, "pickShipOkText");
    public static final String PICK_SHIP_CANCEL_TEXT = Global.getSettings().getString(STRINGS_CATEGORY, "pickShipCancelText");
    public static final String CONFIRM_DMOD_YES = Global.getSettings().getString(STRINGS_CATEGORY, "confirmDModYes");
    public static final String CONFIRM_DMOD_NO = Global.getSettings().getString(STRINGS_CATEGORY, "confirmDModNo");

    public static final boolean LUNALIB_ENABLED = Global.getSettings().getModManager().isModEnabled("lunalib");
    public static final String MOD_ID = "dmodservices";

    // Settings
    public static final String SETTING_ENABLE_DMOD_SERVICES = "enableDMODServices";
    public static final String SETTING_SELECT_DMOD_COST_MULT = "selectDModCostMult";
    public static final String SETTING_SELECT_DMOD_ADD_UNRESTORABLE = "selectDModAddUnrestorable";
    public static final String SETTING_REMOVE_SMOD_COST_MULT = "removeSModCostMult";
    public static final String SETTING_REMOVE_SMOD_REMOVE_UNRESTORABLE = "removeSModRemoveUnrestorable";
    public static final String SETTING_ENABLE_AUTOMATE_OPTION = "enableAutomateOption";
    public static final String SETTING_AUTOMATE_COST_MULT = "automateCostMult";
    public static final String SETTING_AUTOMATE_ADD_UNRESTORABLE = "automateAddUnrestorable";
    public static final String SETTING_AUTOMATE_ADD_NO_AUTO_PENALTY = "automateAddNoAutoPenalty";

    // Memory keys
    public static final String MEM_PICKED_SHIP = "$dmodservices_pickedShip";
    public static final String MEM_PICKED_SHIP_NAME = "$dmodservices_pickedShipName";
    public static final String MEM_ELIGIBLE_HULLMODS = "$dmodservices_eligibleHullmods";
    public static final String MEM_SET_NUM_OF_DMODS = "$dmodservices_setNumOfDMods";
    public static final String MEM_PICKED_HULLMODS_DISPLAY = "$dmodservices_pickedHullmodsDisplay";
    public static final String MEM_PICKED_HULLMODS = "$dmodservices_pickedHullmods";
    public static final String MEM_OPTION_PICKED = "$dmodservices_optionPicked";
    public static final String MEM_NOT_ELIGIBLE = "$dmodservices_notEligible";
    public static final String MEM_CREDITS = "$dmodservices_credits";
    public static final String MEM_DISABLED = "$dmodservices_disabled";
    public static final String MEM_ENABLE_AUTOMATE = "$dmodservices_enableAutomate";
    public static final String MEM_NEW_CREDITS = "$dmodservices_newCredits";

    public static final String OPT_NUM_DMOD_SELECTOR = "dmodservicesSelector";

    public static boolean getSettingBoolean(String settingId) {
        try {
            return Global.getSettings().getJSONObject(MOD_ID).getBoolean(settingId);
        } catch (JSONException e) {
            throw new RuntimeException();
        }
    }

    public static float getSettingFloat(String settingId) {
        try {
            return (float) Global.getSettings().getJSONObject(MOD_ID).getDouble(settingId);
        } catch (JSONException e) {
            throw new RuntimeException();
        }
    }

    public static ShipHullSpecAPI getPristineHullSpec(FleetMemberAPI member) {
        ShipHullSpecAPI hullSpec = member.getHullSpec().getDParentHull();
        return hullSpec == null ? member.getHullSpec() : hullSpec;
    }

    public static float getSelectDModScalingCostMult(int numOfDMods) {
        return Math.min(numOfDMods * 0.15f + 0.4f, 1.0f);
    }

    public static Float getSelectDModCostMultSetting() {
        if (LUNALIB_ENABLED) {
            Float multi = LunaSettings.getFloat(MOD_ID, SETTING_SELECT_DMOD_COST_MULT);
            if (multi != null) return multi;
        }
        return getSettingFloat(SETTING_SELECT_DMOD_COST_MULT);
    }

    public static Float getAutomateCostMultSetting() {
        if (LUNALIB_ENABLED) {
            Float multi = LunaSettings.getFloat(MOD_ID, SETTING_AUTOMATE_COST_MULT);
            if (multi != null) return multi;
        }
        return getSettingFloat(SETTING_AUTOMATE_COST_MULT);
    }

    public static Float getRemoveSModCostMultSetting() {
        if (LUNALIB_ENABLED) {
            Float multi = LunaSettings.getFloat(MOD_ID, SETTING_REMOVE_SMOD_COST_MULT);
            if (multi != null) return multi;
        }
        return getSettingFloat(SETTING_REMOVE_SMOD_COST_MULT);
    }

    public static void addPermaMod(ShipVariantAPI variant, String hullModId) {
        variant.removeSuppressedMod(hullModId);
        variant.addPermaMod(hullModId, false);
    }

    // Similar implementation to DModManager's addDMods(), but simply returns a list of eligible d-mods
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

        // Destroyed ships always get these d-mods, so put them in list if allowed
        if (canAddDestroyedMods) potentialMods.addAll(DModManager.getModsWithTags(Tags.HULLMOD_DESTROYED_ALWAYS));

        // No duplicate d-mods
        DModManager.removeModsAlreadyInVariant(variant, potentialMods);

        return potentialMods;
    }

    public static List<HullModSpecAPI> getSMods(FleetMemberAPI member) {
        List<HullModSpecAPI> potentialMods = new ArrayList<>(3);
        for (String id : member.getVariant().getSMods())
            potentialMods.add(Global.getSettings().getHullModSpec(id));

        return potentialMods;
    }

    public static String getAutomatedReason(FleetMemberAPI member) {
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