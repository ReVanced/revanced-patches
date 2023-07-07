package app.revanced.integrations.patches.components;

// Abuse LithoFilter for CustomVideoSpeedPatch.
public final class VideoSpeedMenuFilterPatch extends Filter {
    // Must be volatile or synchronized, as litho filtering runs off main thread and this field is then access from the main thread.
    public static volatile boolean isVideoSpeedMenuVisible;

    public VideoSpeedMenuFilterPatch() {
        pathFilterGroups.addAll(new StringFilterGroup(
                null,
                "playback_speed_sheet_content.eml-js"
        ));
    }

    @Override
    boolean isFiltered(final String path, final String identifier, final byte[] protobufBufferArray) {
        isVideoSpeedMenuVisible = super.isFiltered(path, identifier, protobufBufferArray);

        return false;
    }
}
