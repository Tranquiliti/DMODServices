package org.tranquility.dmodservices.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;

import java.util.List;
import java.util.Map;

import static org.tranquility.dmodservices.DMSUtil.*;

@SuppressWarnings("unused")
public class DMSAddSelectedDMod extends BaseCommandPlugin {
    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        FleetMemberAPI member = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get(MEM_PICKED_SHIP);
        List<HullModSpecAPI> pickedDMods = (List<HullModSpecAPI>) memoryMap.get(MemKeys.LOCAL).get(MEM_PICKED_HULLMODS);
        DModManager.setDHull(member.getVariant());
        for (HullModSpecAPI picked : pickedDMods)
            addPermaMod(member.getVariant(), picked.getId());

        Boolean addUnrestorable;
        if (LUNALIB_ENABLED) {
            addUnrestorable = LunaSettings.getBoolean(MOD_ID, SETTING_SELECT_DMOD_ADD_UNRESTORABLE);
            if (addUnrestorable == null) addUnrestorable = getSettingBoolean(SETTING_SELECT_DMOD_ADD_UNRESTORABLE);
        } else addUnrestorable = getSettingBoolean(SETTING_SELECT_DMOD_ADD_UNRESTORABLE);

        if (addUnrestorable) member.getVariant().addTag(Tags.VARIANT_UNRESTORABLE);

        Global.getSoundPlayer().playUISound("ui_raid_finished", 0.5f, 2f);
        return true;
    }
}