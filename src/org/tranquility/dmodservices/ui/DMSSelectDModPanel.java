package org.tranquility.dmodservices.ui;

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
import org.tranquility.dmodservices.DMSUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unchecked")
public class DMSSelectDModPanel extends BaseCustomUIPanelPlugin {
    private InteractionDialogAPI dialog;
    private Map<String, MemoryAPI> memoryMap;
    private Map<String, ButtonAPI> buttons;
    private int buttonsChecked;
    private int numExistingDMods;
    private boolean allowDamageStruct;

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

    public void init(CustomPanelAPI panel, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        buttonsChecked = 0;
        numExistingDMods = DModManager.getNumDMods(((FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get(MEM_PICKED_SHIP)).getVariant());
        allowDamageStruct = true;

        List<HullModSpecAPI> potentialDMods = (List<HullModSpecAPI>) memoryMap.get(MemKeys.LOCAL).get(MEM_ELIGIBLE_DMODS);
        buttons = new LinkedHashMap<>(potentialDMods.size());
        TooltipMakerAPI tooltip = panel.createUIElement(panel.getPosition().getWidth(), panel.getPosition().getHeight(), true);
        for (HullModSpecAPI thisHullMod : potentialDMods) {
            float imageSize = 25f;
            ButtonAPI button = tooltip.addAreaCheckbox(thisHullMod.getDisplayName(), thisHullMod, Misc.getButtonTextColor(), Misc.getDarkPlayerColor(), Misc.getNegativeHighlightColor(), panel.getPosition().getWidth() - 25f, imageSize, 5f);
            tooltip.addImage(thisHullMod.getSpriteName(), imageSize, imageSize, -25f); // Hacky way of "attaching" D-Mod sprite to area checkbox
            buttons.put(thisHullMod.getId(), button);
        }
        panel.addUIElement(tooltip);
    }

    public void confirm() {
        if (buttonsChecked <= 0) return;

        List<HullModSpecAPI> checked = new ArrayList<>(buttonsChecked);
        for (String dModId : buttons.keySet())
            if (buttons.get(dModId).isChecked()) checked.add((HullModSpecAPI) buttons.get(dModId).getCustomData());

        Float multiplier;
        if (LUNALIB_ENABLED) {
            multiplier = LunaSettings.getFloat(MOD_ID, "selectDModCostMult");
            if (multiplier == null) multiplier = Global.getSettings().getFloat("dmodservicesSelectDModCostMult");
        } else multiplier = Global.getSettings().getFloat("dmodservicesSelectDModCostMult");

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        float newCredits = Float.parseFloat(((String) localMemory.get(MEM_CREDITS)).replaceAll("[^0-9]", ""));

        // Increase the overall price with each additional D-Mod
        if (checked.size() > 1) {
            float baseValue = DMSUtil.getPristineHullSpec((FleetMemberAPI) localMemory.get(MEM_PICKED_SHIP)).getBaseValue();
            for (int i = 1; i < checked.size(); i++)
                newCredits += getSelectDModCostMult(numExistingDMods + i) * baseValue * multiplier;
        }

        StringBuilder display = new StringBuilder();
        for (HullModSpecAPI hullMod : checked)
            display.append(hullMod.getDisplayName()).append(", ");
        display.delete(display.length() - 2, display.length());

        localMemory.set(MEM_PICKED_DMODS, checked, 0f);
        localMemory.set(MEM_PICKED_DMOD_DISPLAY, display.toString(), 0f);
        localMemory.set(MEM_NEW_CREDITS, Misc.getDGSCredits(newCredits), 0f);

        FireBest.fire(null, dialog, memoryMap, "DModServicesPickedDMod");

        // Confirmation popup to prevent accidental confirms
        dialog.getOptionPanel().addOptionConfirmation("dmodservicesPreciseConfirm", CONFIRM_DMOD_PRECISE + display, CONFIRM_DMOD_YES, CONFIRM_DMOD_NO);
    }
}