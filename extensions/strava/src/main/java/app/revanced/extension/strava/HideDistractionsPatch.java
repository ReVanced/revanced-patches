package app.revanced.extension.strava;

import android.annotation.SuppressLint;

import com.strava.modularframework.data.GenericLayoutModule;
import com.strava.modularframework.data.ModularComponent;
import com.strava.modularframework.data.ModularEntry;
import com.strava.modularframework.data.Module;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressLint("NewApi")
public class HideDistractionsPatch {
    public static boolean upselling;
    public static boolean promo;
    public static boolean followSuggestions;
    public static boolean challengeSuggestions;
    public static boolean joinChallenge;
    public static boolean joinClub;
    public static boolean activityLookback;

    public static List<Module> filterModules(ModularEntry modularEntry) {
        if (hideByName(modularEntry.getPage()) || hideByName(modularEntry.getElement())) {
            return Collections.emptyList();
        }
        return modularEntry.getModules$original().stream()
                .filter(module -> !hideByName(module.getPage()))
                .filter(module -> !hideByName(module.getElement()))
                .collect(Collectors.toList());
    }

    public static GenericLayoutModule[] filterSubmodules(GenericLayoutModule genericLayoutModule) {
        if (hideByName(genericLayoutModule.getPage()) || hideByName(genericLayoutModule.getElement())) {
            return new GenericLayoutModule[0];
        }
        return Arrays.stream(genericLayoutModule.getSubmodules$original())
                .filter(submodule -> !hideByName(submodule.getPage()))
                .filter(submodule -> !hideByName(submodule.getElement()))
                .toArray(GenericLayoutModule[]::new);
    }

    public static List<Module> filterSubmodules(ModularComponent modularComponent) {
        if (hideByName(modularComponent.getPage()) || hideByName(modularComponent.getElement())) {
            return Collections.emptyList();
        }
        return modularComponent.getSubmodules$original().stream()
                .filter(submodule -> !hideByName(submodule.getPage()))
                .filter(submodule -> !hideByName(submodule.getElement()))
                .collect(Collectors.toList());
    }

    private static boolean hideByName(String name) {
        return name != null && (
                upselling && name.contains("_upsell") ||
                        promo && (name.equals("promo") || name.equals("top_of_tab_promo")) ||
                        followSuggestions && name.equals("suggested_follows") ||
                        challengeSuggestions && name.equals("suggested_challenges") ||
                        joinChallenge && name.equals("challenge") ||
                        joinClub && name.equals("club") ||
                        activityLookback && name.equals("highlighted_activity_lookback")
        );
    }
}
