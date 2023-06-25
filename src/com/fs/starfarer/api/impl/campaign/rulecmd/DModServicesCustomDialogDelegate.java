package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import java.util.Map;

public class DModServicesCustomDialogDelegate extends BaseCustomDialogDelegate {
    protected DModServicesCustomUIPanelPlugin plugin = new DModServicesCustomUIPanelPlugin();
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;

    public DModServicesCustomDialogDelegate(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        plugin.init(panel, dialog, memoryMap);
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
        plugin.confirm();
    }

    @Override
    public void customDialogCancel() {
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return plugin;
    }
}
