package app.revanced.integrations.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.patches.playback.quality.RestoreOldVideoQualityMenuPatch;
import app.revanced.integrations.settings.SettingsEnum;

/**
 * Abuse LithoFilter for {@link RestoreOldVideoQualityMenuPatch}.
 */
public final class VideoQualityMenuFilterPatch extends Filter {
    // Must be volatile or synchronized, as litho filtering runs off main thread and this field is then access from the main thread.
    public static volatile boolean isVideoQualityMenuVisible;

    public VideoQualityMenuFilterPatch() {
        pathFilterGroupList.addAll(new StringFilterGroup(
                SettingsEnum.RESTORE_OLD_VIDEO_QUALITY_MENU,
                "quick_quality_sheet_content.eml-js"
        ));
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        isVideoQualityMenuVisible = true;

        return false;
    }
}
