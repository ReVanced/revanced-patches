package app.revanced.integrations.patches.components;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;

import app.revanced.integrations.patches.ReturnYouTubeDislikePatch;
import app.revanced.integrations.settings.SettingsEnum;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class ReturnYouTubeDislikeFilterPatch extends Filter {

    private final ByteArrayFilterGroupList videoIdFilterGroup = new ByteArrayFilterGroupList();

    public ReturnYouTubeDislikeFilterPatch() {
        pathFilterGroupList.addAll(
                new StringFilterGroup(SettingsEnum.RYD_SHORTS, "|shorts_dislike_button.eml|")
        );
        // After the dislikes icon name is some binary data and then the video id for that specific short.
        videoIdFilterGroup.addAll(
                // Video was previously disliked before video was opened.
                new ByteArrayAsStringFilterGroup(null, "ic_right_dislike_on_shadowed"),
                // Video was not already disliked.
                new ByteArrayAsStringFilterGroup(null, "ic_right_dislike_off_shadowed")
        );
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        FilterGroup.FilterGroupResult result = videoIdFilterGroup.check(protobufBufferArray);
        if (result.isFiltered()) {
            // The video length must be hard coded to 11, as there is additional ASCII text that
            // appears immediately after the id if the dislike button is already selected.
            final int videoIdLength = 11;
            final int subStringSearchStartIndex = result.getMatchedIndex() + result.getMatchedLength();
            String videoId = findSubString(protobufBufferArray, subStringSearchStartIndex, videoIdLength);
            if (videoId != null) {
                ReturnYouTubeDislikePatch.newVideoLoaded(videoId, true);
            }
        }

        return false;
    }

    /**
     * Find an exact length ASCII substring starting from a given index.
     *
     * Similar to the String finding code in {@link LithoFilterPatch},
     * but refactoring it to also handle this use case became messy and overly complicated.
     */
    @Nullable
    private static String findSubString(byte[] buffer, int bufferStartIndex, int subStringLength) {
        // Valid ASCII values (ignore control characters).
        final int minimumAscii = 32;  // 32 = space character
        final int maximumAscii = 126; // 127 = delete character

        final int bufferLength = buffer.length;
        int start = bufferStartIndex;
        int end = bufferStartIndex;
        do {
            final int value = buffer[end];
            if (value < minimumAscii || value > maximumAscii) {
                start = end + 1;
            } else if (end - start == subStringLength) {
                return new String(buffer, start, subStringLength, StandardCharsets.US_ASCII);
            }
            end++;
        } while (end < bufferLength);

        return null;
    }
}
