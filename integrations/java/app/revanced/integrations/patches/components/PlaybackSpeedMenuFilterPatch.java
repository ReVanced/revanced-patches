package app.revanced.integrations.patches.components;

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
    boolean isFiltered(final String path, final String identifier, final byte[] protobufBufferArray) {
        isPlaybackSpeedMenuVisible = super.isFiltered(path, identifier, protobufBufferArray);

        return false;
    }
}
