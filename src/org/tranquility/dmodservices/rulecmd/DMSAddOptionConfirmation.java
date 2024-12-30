package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.CONFIRM_DMOD_NO;
import static org.tranquility.dmodservices.DMSUtil.CONFIRM_DMOD_YES;

@SuppressWarnings("unused")
public class DMSAddOptionConfirmation extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        dialog.getOptionPanel().addOptionConfirmation(params.get(0).getString(memoryMap), params.get(1).getStringWithTokenReplacement(ruleId, dialog, memoryMap), CONFIRM_DMOD_YES, CONFIRM_DMOD_NO);
        return true;
    }
}