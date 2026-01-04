package app.revanced.extension.youtube.patches.components;

import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.shared.patches.litho.FilterGroup.*;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

/**
 * Abuse LithoFilter for {@link CustomPlaybackSpeedPatch}.
 */
public final class PlaybackSpeedMenuFilter extends Filter {

    /**
     * Old litho based speed selection menu.
     */
    public static volatile boolean isOldPlaybackSpeedMenuVisible;

    /**
     * 0.05x speed selection menu.
     */
    public static volatile boolean isPlaybackRateSelectorMenuVisible;

    private final StringFilterGroup oldPlaybackMenuGroup;

    public PlaybackSpeedMenuFilter() {
        // 0.05x litho speed menu.
        var playbackRateSelectorGroup = new StringFilterGroup(
                Settings.CUSTOM_SPEED_MENU,
                "playback_rate_selector_menu_sheet.e"
        );

        // Old litho based speed menu.
        oldPlaybackMenuGroup = new StringFilterGroup(
                Settings.CUSTOM_SPEED_MENU,
                "playback_speed_sheet_content.e");

        addPathCallbacks(playbackRateSelectorGroup, oldPlaybackMenuGroup);
    }

    @Override
    public boolean isFiltered(String identifier, String path, byte[] buffer,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == oldPlaybackMenuGroup) {
            isOldPlaybackSpeedMenuVisible = true;
        } else {
            isPlaybackRateSelectorMenuVisible = true;
        }

        return false;
    }
}
