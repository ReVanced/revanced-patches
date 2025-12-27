package app.revanced.extension.youtube.patches.components;

import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.shared.patches.litho.FilterGroup.StringFilterGroup;
import app.revanced.extension.youtube.patches.playback.quality.AdvancedVideoQualityMenuPatch;
import app.revanced.extension.youtube.settings.Settings;

/**
 * LithoFilter for {@link AdvancedVideoQualityMenuPatch}.
 */
public final class AdvancedVideoQualityMenuFilter extends Filter {
    // Must be volatile or synchronized, as litho filtering runs off main thread
    // and this field is then access from the main thread.
    public static volatile boolean isVideoQualityMenuVisible;

    public AdvancedVideoQualityMenuFilter() {
        addPathCallbacks(new StringFilterGroup(
                Settings.ADVANCED_VIDEO_QUALITY_MENU,
                "quick_quality_sheet_content.e"
        ));
    }

    @Override
    public boolean isFiltered(String identifier, String path, byte[] buffer,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        isVideoQualityMenuVisible = true;

        return false;
    }
}
