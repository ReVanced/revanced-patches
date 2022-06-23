package app.revanced.integrations.patches;


import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;

public class HideShortsButtonPatch {

    //Todo: Switch BooleanPreferences to Settings class
    //Used by app.revanced.patches.youtube.layout.shorts.button.patch.ShortsButtonRemoverPatch
    public static void hideShortsButton(View view) {
        AdRemoverAPI.hideShortsButton(view);
    }

    //Needed for the ShortsButtonRemoverPatch
    public static Enum lastPivotTab;
}
