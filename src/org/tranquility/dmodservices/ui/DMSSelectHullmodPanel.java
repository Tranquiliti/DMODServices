package org.tranquility.dmodservices.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import java.util.Map;

public abstract class DMSSelectHullmodPanel extends BaseCustomUIPanelPlugin {
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected Map<String, ButtonAPI> buttons;
    protected int buttonsChecked;

    public abstract void init(CustomPanelAPI panel, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);

    public abstract void confirm();
}