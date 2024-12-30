package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.MEM_PICKED_HULLMODS;
import static org.tranquility.dmodservices.DMSUtil.MEM_PICKED_SHIP;

@SuppressWarnings("unused")
public class DMSRemoveSMod extends BaseCommandPlugin {
    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        FleetMemberAPI member = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get(MEM_PICKED_SHIP);
        List<HullModSpecAPI> pickedSMods = (List<HullModSpecAPI>) memoryMap.get(MemKeys.LOCAL).get(MEM_PICKED_HULLMODS);
        for (HullModSpecAPI picked : pickedSMods)
            member.getVariant().removePermaMod(picked.getId());

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }
}