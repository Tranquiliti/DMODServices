package org.tranquility.dmodservices.ui;

import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.PICK_SHIP_CANCEL_TEXT;
import static org.tranquility.dmodservices.DMSUtil.PICK_SHIP_OK_TEXT;

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
    public String getConfirmText() {
        return PICK_SHIP_OK_TEXT;
    }

    @Override
    public String getCancelText() {
        return PICK_SHIP_CANCEL_TEXT;
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