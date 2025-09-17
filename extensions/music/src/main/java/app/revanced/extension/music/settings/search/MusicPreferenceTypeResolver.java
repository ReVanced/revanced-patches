package app.revanced.extension.music.settings.search;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import androidx.annotation.ColorInt;

import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.settings.search.PreferenceTypeResolver;
import app.revanced.extension.shared.settings.search.BaseSearchResultItem;

/**
 * Music-specific preference type resolver.
 */
@SuppressWarnings("deprecation")
public class MusicPreferenceTypeResolver implements PreferenceTypeResolver {

    @Override
    public BaseSearchResultItem.ViewType determineViewType(Preference preference) {
        if (preference instanceof SwitchPreference) return BaseSearchResultItem.ViewType.SWITCH;
        if (preference instanceof ListPreference) return BaseSearchResultItem.ViewType.LIST;
        if (preference instanceof ColorPickerPreference) return BaseSearchResultItem.ViewType.COLOR_PICKER;
        if ("no_results_placeholder".equals(preference.getKey())) return BaseSearchResultItem.ViewType.NO_RESULTS;
        return BaseSearchResultItem.ViewType.REGULAR;
    }

    @Override
    @ColorInt
    public int extractColor(Preference preference) {
        // Music doesn't have special color preferences beyond ColorPickerPreference.
        return 0;
    }
}
