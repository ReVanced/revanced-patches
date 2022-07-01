package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;

public class HideSuggestionsPatch {

    public static void hideSuggestions(View view) {
        AdRemoverAPI.hideSuggestions(view);
    }

}
