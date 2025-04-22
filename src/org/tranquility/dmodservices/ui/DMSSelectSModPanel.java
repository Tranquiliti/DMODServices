package org.tranquility.dmodservices.ui;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.*;

public class DMSSelectSModPanel extends DMSSelectHullmodPanel {
    @Override
    public void buttonPressed(Object buttonId) {
        buttonsChecked += buttons.get(((HullModSpecAPI) buttonId).getId()).isChecked() ? 1 : -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(CustomPanelAPI panel, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        List<HullModSpecAPI> sMods = (List<HullModSpecAPI>) memoryMap.get(MemKeys.LOCAL).get(MEM_ELIGIBLE_HULLMODS);
        buttons = new LinkedHashMap<>(sMods.size());
        TooltipMakerAPI tooltip = panel.createUIElement(panel.getPosition().getWidth(), panel.getPosition().getHeight(), true);
        for (HullModSpecAPI thisHullMod : sMods) {
            float imageSize = 25f;
            ButtonAPI button = tooltip.addAreaCheckbox(thisHullMod.getDisplayName(), thisHullMod, Misc.getButtonTextColor(), Misc.getDarkPlayerColor(), Misc.getPositiveHighlightColor(), panel.getPosition().getWidth() - 25f, imageSize, 5f);
            tooltip.addImage(thisHullMod.getSpriteName(), imageSize, imageSize, -25f); // Hacky way of "attaching" hullmod sprite to area checkbox
            buttons.put(thisHullMod.getId(), button);
        }
        panel.addUIElement(tooltip);
    }

    @Override
    public void confirm() {
        if (buttonsChecked <= 0) return;

        List<HullModSpecAPI> checked = new ArrayList<>(buttonsChecked);
        for (String sModId : buttons.keySet())
            if (buttons.get(sModId).isChecked()) checked.add((HullModSpecAPI) buttons.get(sModId).getCustomData());

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        float newCredits = Float.parseFloat(((String) localMemory.get(MEM_CREDITS)).replaceAll("[^0-9]", ""));

        // Increase the overall price with each additional s-mod
        newCredits *= checked.size();

        StringBuilder display = new StringBuilder();
        String separator = ", ";
        for (HullModSpecAPI hullMod : checked)
            display.append(hullMod.getDisplayName()).append(separator);
        display.delete(display.length() - separator.length(), display.length());

        localMemory.set(MEM_PICKED_HULLMODS, checked, 0f);
        localMemory.set(MEM_PICKED_HULLMODS_DISPLAY, display.toString(), 0f);
        localMemory.set(MEM_NEW_CREDITS, Misc.getDGSCredits(newCredits), 0f);

        FireBest.fire(null, dialog, memoryMap, "DModServicesPickedSMod");
    }
}