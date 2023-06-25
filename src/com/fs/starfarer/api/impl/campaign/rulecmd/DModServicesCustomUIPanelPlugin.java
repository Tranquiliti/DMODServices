package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DModServicesCustomUIPanelPlugin extends BaseCustomUIPanelPlugin {
    protected transient InteractionDialogAPI dialog;
    protected transient Map<String, MemoryAPI> memoryMap;
    protected transient List<ButtonAPI> buttons = new ArrayList<>();

    public void init(CustomPanelAPI panel, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        TooltipMakerAPI tooltip = panel.createUIElement(panel.getPosition().getWidth(), panel.getPosition().getHeight(), true);
        String[] potentialDMods = (String[]) memoryMap.get(MemKeys.LOCAL).get("$DModServices_eligibleDMods");
        buttons = new ArrayList<>(potentialDMods.length);
        for (String dModId : potentialDMods) {
            ButtonAPI newButton = tooltip.addAreaCheckbox(Global.getSettings().getHullModSpec(dModId).getDisplayName(), dModId, Misc.getButtonTextColor(), Misc.getGrayColor(), Misc.getHighlightedOptionColor(), panel.getPosition().getWidth() - 25f, 25f, 5f);
            buttons.add(newButton);
        }

        panel.addUIElement(tooltip);
    }

    public void confirm() {
        for (ButtonAPI button : buttons)
            if (button.isChecked()) {
                String dModId = (String) button.getCustomData();
                memoryMap.get(MemKeys.LOCAL).set("$DModServices_pickedDMod", dModId, 0f);
                memoryMap.get(MemKeys.LOCAL).set("$DModServices_pickedDModDisplay", Global.getSettings().getHullModSpec(dModId).getDisplayName(), 0f);

                FireBest.fire(null, dialog, memoryMap, "DModServicesPickedDMod");

                // Confirmation popup to prevent accidental confirms
                dialog.getOptionPanel().addOptionConfirmation("dmodservicesPreciseConfirm", Global.getSettings().getString("dmodservices", "confirmDModPrecise") + Global.getSettings().getHullModSpec(dModId).getDisplayName() + ".", Global.getSettings().getString("dmodservices", "confirmDModYes"), Global.getSettings().getString("dmodservices", "confirmDModNo"));

                break;
            }
    }

    // Triggers on every areaCheckbox press
    @Override
    public void buttonPressed(Object buttonId) {
        for (ButtonAPI button : buttons)
            if (button.getCustomData() != buttonId) button.setChecked(false);
    }
}
