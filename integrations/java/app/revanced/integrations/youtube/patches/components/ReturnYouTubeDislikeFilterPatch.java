package app.revanced.integrations.youtube.patches.components;

import android.os.Build;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import app.revanced.integrations.youtube.patches.ReturnYouTubeDislikePatch;
import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.TrieSearch;

/**
 * Searches for video id's in the proto buffer of Shorts dislike.
 *
 * Because multiple litho dislike spans are created in the background
 * (and also anytime litho refreshes the components, which is somewhat arbitrary),
 * that makes the value of {@link VideoInformation#getVideoId()} and {@link VideoInformation#getPlayerResponseVideoId()}
 * unreliable to determine which video id a Shorts litho span belongs to.
 *
 * But the correct video id does appear in the protobuffer just before a Shorts litho span is created.
 *
 * Once a way to asynchronously update litho text is found, this strategy will no longer be needed.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public final class ReturnYouTubeDislikeFilterPatch extends Filter {

    /**
     * Last unique video id's loaded.  Value is ignored and Map is treated as a Set.
     * Cannot use {@link LinkedHashSet} because it's missing #removeEldestEntry().
     */
    @GuardedBy("itself")
    private static final Map<String, Boolean> lastVideoIds = new LinkedHashMap<>() {
        /**
         * Number of video id's to keep track of for searching thru the buffer.
         * A minimum value of 3 should be sufficient, but check a few more just in case.
         */
        private static final int NUMBER_OF_LAST_VIDEO_IDS_TO_TRACK = 5;

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > NUMBER_OF_LAST_VIDEO_IDS_TO_TRACK;
        }
    };

    /**
     * Injection point.
     */
    public static void newPlayerResponseVideoId(String videoId, boolean isShortAndOpeningOrPlaying) {
        try {
            if (!isShortAndOpeningOrPlaying || !Settings.RYD_SHORTS.get()) {
                return;
            }
            synchronized (lastVideoIds) {
                if (lastVideoIds.put(videoId, Boolean.TRUE) == null) {
                    Logger.printDebug(() -> "New Short video id: " + videoId);
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "newPlayerResponseVideoId failure", ex);
        }
    }

    private final ByteArrayFilterGroupList videoIdFilterGroup = new ByteArrayFilterGroupList();

    public ReturnYouTubeDislikeFilterPatch() {
        addPathCallbacks(
                new StringFilterGroup(Settings.RYD_SHORTS, "|shorts_dislike_button.eml|")
        );
        // After the dislikes icon name is some binary data and then the video id for that specific short.
        videoIdFilterGroup.addAll(
                // Video was previously disliked before video was opened.
                new ByteArrayFilterGroup(null, "ic_right_dislike_on_shadowed"),
                // Video was not already disliked.
                new ByteArrayFilterGroup(null, "ic_right_dislike_off_shadowed")
        );
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        FilterGroup.FilterGroupResult result = videoIdFilterGroup.check(protobufBufferArray);
        if (result.isFiltered()) {
            String matchedVideoId = findVideoId(protobufBufferArray);
            // Matched video will be null if in incognito mode.
            // Must pass a null id to correctly clear out the current video data.
            // Otherwise if a Short is opened in non-incognito, then incognito is enabled and another Short is opened,
            // the new incognito Short will show the old prior data.
            ReturnYouTubeDislikePatch.setLastLithoShortsVideoId(matchedVideoId);
        }

        return false;
    }

    @Nullable
    private String findVideoId(byte[] protobufBufferArray) {
        synchronized (lastVideoIds) {
            for (String videoId : lastVideoIds.keySet()) {
                if (byteArrayContainsString(protobufBufferArray, videoId)) {
                    return videoId;
                }
            }
            return null;
        }
    }

    /**
     * This could use {@link TrieSearch}, but since the patterns are constantly changing
     * the overhead of updating the Trie might negate the search performance gain.
     */
    private static boolean byteArrayContainsString(@NonNull byte[] array, @NonNull String text) {
        for (int i = 0, lastArrayStartIndex = array.length - text.length(); i <= lastArrayStartIndex; i++) {
            boolean found = true;
            for (int j = 0, textLength = text.length(); j < textLength; j++) {
                if (array[i + j] != (byte) text.charAt(j)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }
}