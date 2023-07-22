package org.tranquility.dmodservices;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;
import org.tranquility.dmodservices.rulecmd.DMSShowShipPicker;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class DMSSelectDModPanel extends BaseCustomUIPanelPlugin {
    protected transient InteractionDialogAPI dialog;
    protected transient Map<String, MemoryAPI> memoryMap;
    protected transient TreeMap<String, ButtonAPI> buttons;
    protected transient int buttonsChecked;
    protected transient int numExistingDMods;
    protected transient boolean allowDamageStruct;

    public void init(CustomPanelAPI panel, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        buttons = new TreeMap<>();
        buttonsChecked = 0;
        numExistingDMods = DModManager.getNumDMods(((FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get("$DModServices_pickedShip")).getVariant());
        allowDamageStruct = true;

        TooltipMakerAPI tooltip = panel.createUIElement(panel.getPosition().getWidth(), panel.getPosition().getHeight(), true);
        String[] potentialDMods = (String[]) memoryMap.get(MemKeys.LOCAL).get("$DModServices_eligibleDMods");
        for (String dModId : potentialDMods) {
            HullModSpecAPI thisHullMod = Global.getSettings().getHullModSpec(dModId);
            float imageSize = 25f;
            ButtonAPI button = tooltip.addAreaCheckbox(thisHullMod.getDisplayName(), thisHullMod, Misc.getButtonTextColor(), Misc.getDarkPlayerColor(), Misc.getNegativeHighlightColor(), panel.getPosition().getWidth() - 25f, imageSize, 5f);
            tooltip.addImage(thisHullMod.getSpriteName(), imageSize, imageSize, -25f); // Hacky way of "attaching" D-Mod sprite to area checkbox
            buttons.put(dModId, button);
        }

        panel.addUIElement(tooltip);
    }

    public void confirm() {
        if (buttonsChecked <= 0) return;

        ArrayList<HullModSpecAPI> checked = new ArrayList<>(buttonsChecked);
        for (String dModId : buttons.keySet())
            if (buttons.get(dModId).isChecked()) checked.add((HullModSpecAPI) buttons.get(dModId).getCustomData());

        Float multiplier;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            multiplier = LunaSettings.getFloat("dmodservices", "selectDModCostMult");
            if (multiplier == null) {
                multiplier = Global.getSettings().getFloat("dmodservicesSelectDModCostMult");
            }
        } else multiplier = Global.getSettings().getFloat("dmodservicesSelectDModCostMult");

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        float newCredits = Float.parseFloat(((String) localMemory.get("$DModServices_credits")).replace(",", ""));

        // Increase the overall price with each additional D-Mod
        if (checked.size() > 1) {
            float baseValue = DMSShowShipPicker.getPristineHullSpec((FleetMemberAPI) localMemory.get("$DModServices_pickedShip")).getBaseValue();
            for (int i = 1; i < checked.size(); i++) {
                float dModMultiplier = Math.min((numExistingDMods + i) * 0.15f + 0.4f, 1.0f);
                newCredits += dModMultiplier * baseValue * multiplier;
            }
        }

        StringBuilder display = new StringBuilder();
        for (HullModSpecAPI hullMod : checked)
            display.append(hullMod.getDisplayName()).append(", ");
        display.delete(display.length() - 2, display.length());

        localMemory.set("$DModServices_pickedDMod", checked, 0f);
        localMemory.set("$DModServices_pickedDModDisplay", display.toString(), 0f);
        localMemory.set("$DModServices_newCredits", Misc.getWithDGS(newCredits), 0f);

        FireBest.fire(null, dialog, memoryMap, "DModServicesPickedDMod");

        // Confirmation popup to prevent accidental confirms
        dialog.getOptionPanel().addOptionConfirmation("dmodservicesPreciseConfirm", Global.getSettings().getString("dmodservices", "confirmDModPrecise") + display, Global.getSettings().getString("dmodservices", "confirmDModYes"), Global.getSettings().getString("dmodservices", "confirmDModNo"));
    }

    // Triggers on every areaCheckbox press
    // buttonId is the data of the clicked button
    @Override
    public void buttonPressed(Object buttonId) {
        HullModSpecAPI pressedHullMod = (HullModSpecAPI) buttonId;
        boolean isPressed = buttons.get(pressedHullMod.getId()).isChecked();
        buttonsChecked += isPressed ? 1 : -1;
        if (!pressedHullMod.hasTag(Tags.HULLMOD_DESTROYED_ALWAYS) && pressedHullMod.hasTag(Tags.HULLMOD_DAMAGE_STRUCT))
            allowDamageStruct = !isPressed;

        for (String dModId : buttons.keySet()) {
            ButtonAPI thisButton = buttons.get(dModId);
            HullModSpecAPI thisHullMod = (HullModSpecAPI) thisButton.getCustomData();
            thisButton.setEnabled(thisButton.isChecked() || (buttonsChecked + numExistingDMods < DModManager.MAX_DMODS_FROM_COMBAT && (allowDamageStruct || thisHullMod.hasTag(Tags.HULLMOD_DESTROYED_ALWAYS) || !thisHullMod.hasTag(Tags.HULLMOD_DAMAGE_STRUCT))));
        }
    }
}