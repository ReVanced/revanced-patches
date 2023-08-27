package app.revanced.integrations.patches.components;

import androidx.annotation.Nullable;

// Abuse LithoFilter for CustomPlaybackSpeedPatch.
public final class PlaybackSpeedMenuFilterPatch extends Filter {
    // Must be volatile or synchronized, as litho filtering runs off main thread and this field is then access from the main thread.
    public static volatile boolean isPlaybackSpeedMenuVisible;

    public PlaybackSpeedMenuFilterPatch() {
        pathFilterGroups.addAll(new StringFilterGroup(
                null,
                "playback_speed_sheet_content.eml-js"
        ));
    }

    @Override
    boolean isFiltered(String path, @Nullable String identifier, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        isPlaybackSpeedMenuVisible = true;

        return false;
    }
}
