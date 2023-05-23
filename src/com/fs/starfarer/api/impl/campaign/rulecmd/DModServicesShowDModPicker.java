package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
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

@SuppressWarnings("unused")
public class DModServicesShowDModPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        final MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        final String[] potentialDMods = (String[]) localMemory.get("$DModServices_eligibleDMods");

        // TODO: better UI and button selection
        // Hopefully will implement buttons with action listeners or similar
        final float panelWidth = 350f;
        final float panelHeight = 300f;
        dialog.showCustomDialog(panelWidth, panelHeight, new CustomDialogDelegate() {
            private List<ButtonAPI> buttons;

            @Override
            public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
                TooltipMakerAPI tooltip = panel.createUIElement(panelWidth, panelHeight, true);

                buttons = new ArrayList<>(potentialDMods.length);
                for (int i = 0; i < potentialDMods.length; i++)
                    buttons.add(tooltip.addAreaCheckbox(Global.getSettings().getHullModSpec(potentialDMods[i]).getDisplayName(), i, Misc.getButtonTextColor(), Misc.getGrayColor(), Misc.getHighlightedOptionColor(), panelWidth - 25f, 25f, 10f));

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
                for (ButtonAPI button : buttons) {
                    if (button.isChecked()) { // Only first checked button goes through
                        int i = (int) button.getCustomData();
                        String pickId = potentialDMods[i];
                        localMemory.set("$DModServices_selectIndex", i, 0f);
                        localMemory.set("$DModServices_pickedDMod", pickId, 0f);
                        localMemory.set("$DModServices_pickedDModDisplay", Global.getSettings().getHullModSpec(pickId).getDisplayName(), 0f);

                        FireBest.fire(null, dialog, memoryMap, "DModServicesPickedDMod");

                        // Confirmation popup to prevent accidental confirms
                        dialog.getOptionPanel().addOptionConfirmation("dmodservicesPreciseConfirm", Global.getSettings().getString("dmodservices", "confirmDModPrecise") + Global.getSettings().getHullModSpec(pickId).getDisplayName() + ".", Global.getSettings().getString("dmodservices", "confirmDModYes"), Global.getSettings().getString("dmodservices", "confirmDModNo"));

                        break;
                    }
                }
            }

            @Override
            public void customDialogCancel() {
            }

            @Override
            public CustomUIPanelPlugin getCustomPanelPlugin() {
                return null;
            }
        });

        return true;
    }
}
