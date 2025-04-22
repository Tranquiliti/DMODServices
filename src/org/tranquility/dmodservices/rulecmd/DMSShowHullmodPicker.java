package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import org.tranquility.dmodservices.ui.DMSHullmodDelegate;
import org.tranquility.dmodservices.ui.DMSSelectDModPanel;
import org.tranquility.dmodservices.ui.DMSSelectHullmodPanel;
import org.tranquility.dmodservices.ui.DMSSelectSModPanel;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DMSShowHullmodPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        DMSSelectHullmodPanel panel = params.get(0).getString(memoryMap).equals(Tags.HULLMOD_DMOD) ? new DMSSelectDModPanel() : new DMSSelectSModPanel();
        dialog.showCustomDialog(325f, 480f, new DMSHullmodDelegate(panel, dialog, memoryMap));
        return true;
    }
}