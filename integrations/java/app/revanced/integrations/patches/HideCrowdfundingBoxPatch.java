package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public class HideCrowdfundingBoxPatch {
    //Used by app.revanced.patches.youtube.layout.hidecrowdfundingbox.patch.HideCrowdfundingBoxPatch
    public static void hideCrowdfundingBox(View view) {
        if (!SettingsEnum.HIDE_CROWDFUNDING_BOX.getBoolean()) return;
        ReVancedUtils.HideViewByLayoutParams(view);
    }
}
