package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;

/**
 * Abuse LithoFilter for {@link CustomPlaybackSpeedPatch}.
 */
public final class PlaybackSpeedMenuFilterPatch extends Filter {
    // Must be volatile or synchronized, as litho filtering runs off main thread and this field is then access from the main thread.
    public static volatile boolean isPlaybackSpeedMenuVisible;

    public PlaybackSpeedMenuFilterPatch() {
        addPathCallbacks(new StringFilterGroup(
                null,
                "playback_speed_sheet_content.eml-js"
        ));
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        isPlaybackSpeedMenuVisible = true;

        return false;
    }
}
