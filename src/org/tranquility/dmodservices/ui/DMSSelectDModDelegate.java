package org.tranquility.dmodservices.ui;

import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import java.util.Map;

public class DMSSelectDModDelegate extends BaseCustomDialogDelegate {
    private final DMSSelectDModPanel plugin = new DMSSelectDModPanel();
    private final InteractionDialogAPI dialog;
    private final Map<String, MemoryAPI> memoryMap;

    public DMSSelectDModDelegate(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
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
    public void customDialogConfirm() {
        plugin.confirm();
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return plugin;
    }
}