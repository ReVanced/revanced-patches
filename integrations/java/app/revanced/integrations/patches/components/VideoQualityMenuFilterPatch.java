package app.revanced.integrations.patches.components;

import app.revanced.integrations.settings.SettingsEnum;

// Abuse LithoFilter for OldVideoQualityMenuPatch.
public final class VideoQualityMenuFilterPatch extends Filter {
    // Must be volatile or synchronized, as litho filtering runs off main thread and this field is then access from the main thread.
    public static volatile boolean isVideoQualityMenuVisible;

    public VideoQualityMenuFilterPatch() {
        pathFilterGroups.addAll(new StringFilterGroup(
                SettingsEnum.SHOW_OLD_VIDEO_QUALITY_MENU,
                "quick_quality_sheet_content.eml-js"
        ));
    }

    @Override
    boolean isFiltered(final String path, final String identifier, final byte[] protobufBufferArray) {
        isVideoQualityMenuVisible = super.isFiltered(path, identifier, protobufBufferArray);

        return false;
    }
}
