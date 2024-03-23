package org.tranquility.dmodservices;

import com.fs.starfarer.api.Global;

public final class DMSUtil {
    // strings.json Strings
    private static final String STRINGS_CATEGORY = "dmodservices";
    public static final String PICK_SHIP_TITLE = Global.getSettings().getString(STRINGS_CATEGORY, "pickShipTitle");
    public static final String PICK_SHIP_OK_TEXT = Global.getSettings().getString(STRINGS_CATEGORY, "pickShipOkText");
    public static final String PICK_SHIP_CANCEL_TEXT = Global.getSettings().getString(STRINGS_CATEGORY, "pickShipCancelText");
    public static final String CONFIRM_RANDOM_DMOD_HIDDEN = Global.getSettings().getString(STRINGS_CATEGORY, "confirmRandomDModHidden");
    public static final String CONFIRM_DMOD_RANDOM = Global.getSettings().getString(STRINGS_CATEGORY, "confirmDModRandom");
    public static final String CONFIRM_DMOD_PRECISE = Global.getSettings().getString(STRINGS_CATEGORY, "confirmDModPrecise");
    public static final String CONFIRM_AUTOMATE = Global.getSettings().getString(STRINGS_CATEGORY, "confirmAutomate");
    public static final String CONFIRM_DMOD_YES = Global.getSettings().getString(STRINGS_CATEGORY, "confirmDModYes");
    public static final String CONFIRM_DMOD_NO = Global.getSettings().getString(STRINGS_CATEGORY, "confirmDModNo");

    public static final boolean LUNALIB_ENABLED = Global.getSettings().getModManager().isModEnabled("lunalib");

    public static final String MEM_PICKED_SHIP = "$DModServices_pickedShip";
    public static final String MEM_ELIGIBLE_DMODS = "$DModServices_eligibleDMods";
    public static final String MEM_PICKED_DMOD_DISPLAY = "$DModServices_pickedDModDisplay";
    public static final String MEM_PICKED_DMODS = "$DModServices_pickedDMods";
    public static final String MEM_OPTION_PICKED = "$DModServices_optionPicked";
    public static final String MEM_NOT_ELIGIBLE = "$DModServices_notEligible";
    public static final String MEM_CREDITS = "$DModServices_credits";
    public static final String MEM_DISABLED = "$DModServices_disabled";
    public static final String MEM_ENABLE_AUTOMATE = "$DModServices_enableAutomate";
    public static final String MEM_NEW_CREDITS = "$DModServices_newCredits";
}
