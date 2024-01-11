package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import org.tranquility.dmodservices.DMSSelectDModDelegate;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DMSShowDModPicker extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        dialog.showCustomDialog(325f, 480f, new DMSSelectDModDelegate(dialog, memoryMap));
        return true;
    }
}