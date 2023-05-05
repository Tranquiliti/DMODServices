package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DModServicesShowDModPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        final String[] potentialDMods = (String[]) localMemory.get("$DModServices_eligibleDMods");

        // TODO: Work on this after the 0.96 update
        /*
        final float panelWidth = 350f;
        final float panelHeight = 300f;
        dialog.showCustomDialog(panelWidth, panelHeight, new CustomDialogDelegate() {

            TooltipMakerAPI tooltip;


            @Override
            public void createCustomDialog(CustomPanelAPI panel) {
                tooltip = panel.createUIElement(panelWidth, panelHeight, true);
                for (String potentialDMod : potentialDMods) {
                    String displayText = Global.getSettings().getHullModSpec(potentialDMod).getDisplayName();
                    tooltip.addAreaCheckbox(displayText, potentialDMod, Misc.getButtonTextColor(), Misc.getGrayColor(), Misc.getHighlightedOptionColor(), panelWidth - 25f, 25f, 10f);
                    tooltip.getPrev();
                }

                panel.addUIElement(tooltip);
            }

            @Override
            public boolean hasCancelButton() {
                return true;
            }

            @Override
            public String getConfirmText() {
                return Global.getSettings().getString("dmodservices", "pickShipOkText");
            }

            @Override
            public String getCancelText() {
                return Global.getSettings().getString("dmodservices", "pickShipCancelText");
            }

            @Override
            public void customDialogConfirm() {
            }

            @Override
            public void customDialogCancel() {
            }

            @Override
            public CustomUIPanelPlugin getCustomPanelPlugin() {
                return null;
            }
        }); */

        // Interim solution for selecting D-Mods (hopefully until I set up a proper selection dialog or something) <- LOL
        if (!localMemory.contains("$DModServices_pickedDMod")) {
            localMemory.set("$DModServices_selectIndex", 0, 0f);
            dialog.getTextPanel().addParagraph(Global.getSettings().getString("dmodservices", "pickDModDesc"));
            StringBuilder list = new StringBuilder();
            for (String dModId : potentialDMods)
                list.append(Global.getSettings().getHullModSpec(dModId).getDisplayName()).append("\n");
            dialog.getTextPanel().addParagraph(list.toString(), Color.ORANGE);
        }

        int index = localMemory.getInt("$DModServices_selectIndex");
        String pickId = potentialDMods[index];
        index++;
        if (index == potentialDMods.length) index = 0;
        localMemory.set("$DModServices_selectIndex", index, 0f);
        localMemory.set("$DModServices_pickedDMod", pickId, 0f);
        localMemory.set("$DModServices_pickedDModDisplay", Global.getSettings().getHullModSpec(pickId).getDisplayName(), 0f);

        FireBest.fire(null, dialog, memoryMap, "DModServicesPickedDMod");

        // Confirmation popup to prevent accidental confirms
        dialog.getOptionPanel().addOptionConfirmation("dmodservicesPreciseConfirm", Global.getSettings().getString("dmodservices", "confirmDModPrecise") + Global.getSettings().getHullModSpec(pickId).getDisplayName() + ".", Global.getSettings().getString("dmodservices", "confirmDModYes"), Global.getSettings().getString("dmodservices", "confirmDModNo"));
        return true;
    }
}
