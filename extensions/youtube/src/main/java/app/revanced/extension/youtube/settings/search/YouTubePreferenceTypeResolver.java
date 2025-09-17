package app.revanced.extension.youtube.settings.search;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import androidx.annotation.ColorInt;

import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.settings.preference.UrlLinkPreference;
import app.revanced.extension.shared.settings.search.PreferenceTypeResolver;
import app.revanced.extension.shared.settings.search.BaseSearchResultItem;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategoryListPreference;

/**
 * YouTube-specific preference type resolver.
 */
@SuppressWarnings("deprecation")
public class YouTubePreferenceTypeResolver implements PreferenceTypeResolver {

    @Override
    public BaseSearchResultItem.ViewType determineViewType(Preference preference) {
        if (preference instanceof SwitchPreference) return BaseSearchResultItem.ViewType.SWITCH;
        if (preference instanceof ListPreference && !(preference instanceof SegmentCategoryListPreference))
            return BaseSearchResultItem.ViewType.LIST;
        if (preference instanceof ColorPickerPreference) return BaseSearchResultItem.ViewType.COLOR_PICKER;
        if (preference instanceof SegmentCategoryListPreference) return BaseSearchResultItem.ViewType.SEGMENT_CATEGORY;
        if (preference instanceof UrlLinkPreference) return BaseSearchResultItem.ViewType.URL_LINK;
        if ("no_results_placeholder".equals(preference.getKey())) return BaseSearchResultItem.ViewType.NO_RESULTS;
        return BaseSearchResultItem.ViewType.REGULAR;
    }

    @Override
    @ColorInt
    public int extractColor(Preference preference) {
        if (preference instanceof SegmentCategoryListPreference segmentPref) {
            return segmentPref.getColorWithOpacity();
        }
        return 0;
    }
}
