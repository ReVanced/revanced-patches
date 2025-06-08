package app.revanced.extension.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

/**
 * Abuse LithoFilter for {@link CustomPlaybackSpeedPatch}.
 */
public final class PlaybackSpeedMenuFilterPatch extends Filter {

    /**
     * 0.05x speed selection menu.
     */
    public static volatile boolean isPlaybackRateSelectorMenuVisible;

    public PlaybackSpeedMenuFilterPatch() {
        // 0.05x litho speed menu.
        var playbackRateSelectorGroup = new StringFilterGroup(
                Settings.CUSTOM_SPEED_MENU,
                "playback_rate_selector_menu_sheet.eml-js"
        );

        addPathCallbacks(playbackRateSelectorGroup);
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        isPlaybackRateSelectorMenuVisible = true;

        return false;
    }
}
