package app.revanced.extension.strava;

import com.strava.modularframework.data.ModularEntry;
import com.strava.modularframework.data.Module;

import java.util.Collections;
import java.util.List;

public class HideDistractionsPatch {
    public static boolean upselling;
    public static boolean promo;
    public static boolean followSuggestions;
    public static boolean challengeSuggestions;
    public static boolean joinChallenge;
    public static boolean joinClub;
    public static boolean activityLookback;

    public static List<Module> filterModules(ModularEntry modularEntry) {
        String page = modularEntry.getPage();
        if (upselling && page.endsWith("_upsell") ||
                promo && (page.equals("promo") || page.equals("top_of_tab_promo")) ||
                followSuggestions && page.equals("suggested_follows") ||
                challengeSuggestions && page.equals("suggested_challenges") ||
                joinChallenge && page.equals("challenge") ||
                joinClub && page.equals("club") ||
                activityLookback && page.equals("highlighted_activity_lookback")) {
            return Collections.emptyList();
        }
        return modularEntry.getModules$original();
    }
}
