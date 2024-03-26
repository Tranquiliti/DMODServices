package org.tranquility.dmodservices;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    public static final String MOD_ID = "dmodservices";

    // Memory keys
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

    public static ShipHullSpecAPI getPristineHullSpec(FleetMemberAPI member) {
        ShipHullSpecAPI hullSpec = member.getHullSpec().getDParentHull();
        return hullSpec == null ? member.getHullSpec() : hullSpec;
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

    public static float getSelectDModCostMult(int numOfDMods) {
        return Math.min(numOfDMods * 0.15f + 0.4f, 1.0f);
    }
}
