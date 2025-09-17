package app.revanced.extension.shared.settings.search;

import android.preference.Preference;
import androidx.annotation.ColorInt;

/**
 * Interface for resolving app-specific preference types and extracting type-specific data.
 */
@SuppressWarnings("deprecation")
public interface PreferenceTypeResolver {

    /**
     * Determines the view type for a given preference.
     */
    BaseSearchResultItem.ViewType determineViewType(Preference preference);

    /**
     * Extracts color from color-related preferences.
     *
     * @param preference The preference to extract color from
     * @return The color value, or 0 if not a color preference
     */
    @ColorInt
    int extractColor(Preference preference);
}
