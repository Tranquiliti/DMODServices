package org.tranquility.dmodservices.ui;

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
import org.tranquility.dmodservices.DMSUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.*;

public class DMSSelectDModPanel extends DMSSelectHullmodPanel {
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

    @Override
    @SuppressWarnings("unchecked")
    public void init(CustomPanelAPI panel, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        numExistingDMods = DModManager.getNumDMods(((FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get(MEM_PICKED_SHIP)).getVariant());
        allowDamageStruct = true;

        List<HullModSpecAPI> potentialDMods = (List<HullModSpecAPI>) memoryMap.get(MemKeys.LOCAL).get(MEM_ELIGIBLE_HULLMODS);
        buttons = new LinkedHashMap<>(potentialDMods.size());
        TooltipMakerAPI tooltip = panel.createUIElement(panel.getPosition().getWidth(), panel.getPosition().getHeight(), true);
        for (HullModSpecAPI thisHullMod : potentialDMods) {
            float imageSize = 25f;
            ButtonAPI button = tooltip.addAreaCheckbox(thisHullMod.getDisplayName(), thisHullMod, Misc.getButtonTextColor(), Misc.getDarkPlayerColor(), Misc.getNegativeHighlightColor(), panel.getPosition().getWidth() - 25f, imageSize, 5f);
            tooltip.addImage(thisHullMod.getSpriteName(), imageSize, imageSize, -25f); // Hacky way of "attaching" d-mod sprite to area checkbox
            buttons.put(thisHullMod.getId(), button);
        }
        panel.addUIElement(tooltip);
    }

    @Override
    public void confirm() {
        if (buttonsChecked <= 0) return;

        List<HullModSpecAPI> checked = new ArrayList<>(buttonsChecked);
        for (String dModId : buttons.keySet())
            if (buttons.get(dModId).isChecked()) checked.add((HullModSpecAPI) buttons.get(dModId).getCustomData());

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        float newCredits = Float.parseFloat(((String) localMemory.get(MEM_CREDITS)).replaceAll("[^0-9]", ""));

        // Increase the overall price with each additional d-mod
        if (checked.size() > 1) {
            float baseValue = DMSUtil.getPristineHullSpec((FleetMemberAPI) localMemory.get(MEM_PICKED_SHIP)).getBaseValue();
            float multi = DMSUtil.getSelectDModCostMultSetting();
            for (int i = 1; i < checked.size(); i++)
                newCredits += getSelectDModScalingCostMult(numExistingDMods + i) * baseValue * multi;
        }

        StringBuilder display = new StringBuilder();
        String separator = ", ";
        for (HullModSpecAPI hullMod : checked)
            display.append(hullMod.getDisplayName()).append(separator);
        display.delete(display.length() - separator.length(), display.length());

        localMemory.set(MEM_PICKED_HULLMODS, checked, 0f);
        localMemory.set(MEM_PICKED_HULLMODS_DISPLAY, display.toString(), 0f);
        localMemory.set(MEM_NEW_CREDITS, Misc.getDGSCredits(newCredits), 0f);

        FireBest.fire(null, dialog, memoryMap, "DModServicesPickedDMod");
    }
}