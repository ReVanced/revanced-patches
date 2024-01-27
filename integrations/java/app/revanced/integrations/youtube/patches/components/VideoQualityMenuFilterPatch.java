package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.patches.playback.quality.RestoreOldVideoQualityMenuPatch;
import app.revanced.integrations.youtube.settings.Settings;

/**
 * Abuse LithoFilter for {@link RestoreOldVideoQualityMenuPatch}.
 */
public final class VideoQualityMenuFilterPatch extends Filter {
    // Must be volatile or synchronized, as litho filtering runs off main thread
    // and this field is then access from the main thread.
    public static volatile boolean isVideoQualityMenuVisible;

    public VideoQualityMenuFilterPatch() {
        addPathCallbacks(new StringFilterGroup(
                Settings.RESTORE_OLD_VIDEO_QUALITY_MENU,
                "quick_quality_sheet_content.eml-js"
        ));
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        isVideoQualityMenuVisible = true;

        return false;
    }
}
