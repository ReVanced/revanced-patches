package app.revanced.integrations.patches;

import app.revanced.integrations.adremover.AdRemoverAPI;

public class HideInfoCardSuggestionsPatch {

    //TODO: Create Patch
    //Not used yet
    public static void HideInfoCardSuggestions(Object InfoCardOverlayPresenter) {
        AdRemoverAPI.removeInfoCardSuggestions(InfoCardOverlayPresenter);
    }
}
