package app.revanced.integrations.patches;

import app.revanced.integrations.adremover.AdRemoverAPI;

public class HideSuggestionsPatch {

    //TODO: Create Patch
    //Not used yet
    public static void HideSuggestions(boolean showSuggestions) {
        AdRemoverAPI.removeSuggestions(showSuggestions);
    }

}
