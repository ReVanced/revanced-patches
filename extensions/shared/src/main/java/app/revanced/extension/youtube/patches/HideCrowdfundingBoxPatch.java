package app.revanced.extension.youtube.patches;

import android.view.View;

import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public class HideCrowdfundingBoxPatch {
    //Used by app.revanced.patches.youtube.layout.hidecrowdfundingbox.patch.HideCrowdfundingBoxPatch
    public static void hideCrowdfundingBox(View view) {
        if (!Settings.HIDE_CROWDFUNDING_BOX.get()) return;
        Utils.hideViewByLayoutParams(view);
    }
}
