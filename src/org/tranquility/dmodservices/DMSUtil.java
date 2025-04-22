package org.tranquility.dmodservices;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lunalib.lunaSettings.LunaSettings;

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

    // Memory keys
    public static final String MEM_PICKED_SHIP = "$DModServices_pickedShip";
    public static final String MEM_PICKED_SHIP_NAME = "$DModServices_pickedShipName";
    public static final String MEM_ELIGIBLE_HULLMODS = "$DModServices_eligibleHullmods";
    public static final String MEM_SET_NUM_OF_DMODS = "$DModServices_setNumOfDMods";
    public static final String MEM_PICKED_HULLMODS_DISPLAY = "$DModServices_pickedHullmodsDisplay";
    public static final String MEM_PICKED_HULLMODS = "$DModServices_pickedHullmods";
    public static final String MEM_OPTION_PICKED = "$DModServices_optionPicked";
    public static final String MEM_NOT_ELIGIBLE = "$DModServices_notEligible";
    public static final String MEM_CREDITS = "$DModServices_credits";
    public static final String MEM_DISABLED = "$DModServices_disabled";
    public static final String MEM_ENABLE_AUTOMATE = "$DModServices_enableAutomate";
    public static final String MEM_NEW_CREDITS = "$DModServices_newCredits";

    public static final String OPT_NUM_DMOD_SELECTOR = "dmodservicesSelector";

    public static ShipHullSpecAPI getPristineHullSpec(FleetMemberAPI member) {
        ShipHullSpecAPI hullSpec = member.getHullSpec().getDParentHull();
        return hullSpec == null ? member.getHullSpec() : hullSpec;
    }

    public static float getSelectDModScalingCostMult(int numOfDMods) {
        return Math.min(numOfDMods * 0.15f + 0.4f, 1.0f);
    }

    public static Float getSelectDModCostMultSetting() {
        if (LUNALIB_ENABLED) {
            Float multi = LunaSettings.getFloat(MOD_ID, "selectDModCostMult");
            if (multi != null) return multi;
        }
        return Global.getSettings().getFloat("dmodservicesSelectDModCostMult");
    }

    public static Float getAutomateCostMultSetting() {
        if (LUNALIB_ENABLED) {
            Float multi = LunaSettings.getFloat(MOD_ID, "automateCostMult");
            if (multi != null) return multi;
        }
        return Global.getSettings().getFloat("dmodservicesAutomateCostMult");
    }

    public static Float getRemoveSModCostMultSetting() {
        if (LUNALIB_ENABLED) {
            Float multi = LunaSettings.getFloat(MOD_ID, "removeSModCostMult");
            if (multi != null) return multi;
        }
        return Global.getSettings().getFloat("dmodservicesRemoveSModCostMult");
    }

    public static void addPermaMod(ShipVariantAPI variant, String hullModId) {
        variant.removeSuppressedMod(hullModId);
        variant.addPermaMod(hullModId, false);
    }
}