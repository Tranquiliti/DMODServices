package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unused")
public class DMSAddOptionConfirmation extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        String optionId = params.get(0).getString(memoryMap);
        String text = "";
        switch (optionId) {
            case "dmodservicesRandomConfirm":
                text = CONFIRM_DMOD_RANDOM;
                break;
            case "dmodservicesSelectionConfirm":
                text = String.format(CONFIRM_DMOD_SELECTION, memoryMap.get(MemKeys.LOCAL).getString(MEM_PICKED_DMOD_DISPLAY));
                break;
            case "dmodservicesAutomateConfirm":
                text = CONFIRM_AUTOMATE;
                break;
        }

        dialog.getOptionPanel().addOptionConfirmation(optionId, text, CONFIRM_DMOD_YES, CONFIRM_DMOD_NO);

        return true;
    }
}