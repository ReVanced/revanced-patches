package app.revanced.integrations.patches.spoof;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.patches.spoof.requests.StoryboardRendererRequester;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;

import static app.revanced.integrations.utils.ReVancedUtils.containsAny;

/** @noinspection unused*/
public class SpoofSignaturePatch {
    /**
     * Parameter (also used by
     * <a href="https://github.com/yt-dlp/yt-dlp/blob/81ca451480051d7ce1a31c017e005358345a9149/yt_dlp/extractor/youtube.py#L3602">yt-dlp</a>)
     * to fix playback issues.
     */
    private static final String INCOGNITO_PARAMETERS = "CgIQBg==";

    /**
     * Parameters used when playing clips.
     */
    private static final String CLIPS_PARAMETERS = "kAIB";

    /**
     * Parameters causing playback issues.
     */
    private static final String[] AUTOPLAY_PARAMETERS = {
            "YAHI", // Autoplay in feed.
            "SAFg"  // Autoplay in scrim.
    };

    /**
     * Parameter used for autoplay in scrim.
     * Prepend this parameter to mute video playback (for autoplay in feed).
     */
    private static final String SCRIM_PARAMETER = "SAFgAXgB";

    /**
     * Last video id loaded. Used to prevent reloading the same spec multiple times.
     */
    @Nullable
    private static volatile String lastPlayerResponseVideoId;

    @Nullable
    private static volatile StoryboardRenderer videoRenderer;

    private static volatile boolean useOriginalStoryboardRenderer;

    private static volatile boolean isPlayingShorts;

    /**
     * Injection point.
     *
     * Called off the main thread, and called multiple times for each video.
     *
     * @param parameters Original protobuf parameter value.
     */
    public static String spoofParameter(String parameters, boolean isShortAndOpeningOrPlaying) {
        try {
            LogHelper.printDebug(() -> "Original protobuf parameter value: " + parameters);

            if (!SettingsEnum.SPOOF_SIGNATURE.getBoolean()) {
                return parameters;
            }

            // Clip's player parameters contain a lot of information (e.g. video start and end time or whether it loops)
            // For this reason, the player parameters of a clip are usually very long (150~300 characters).
            // Clips are 60 seconds or less in length, so no spoofing.
            //noinspection AssignmentUsedAsCondition
            if (useOriginalStoryboardRenderer = parameters.length() > 150 || containsAny(parameters, CLIPS_PARAMETERS)) {
                return parameters;
            }

            // Shorts do not need to be spoofed.
            //noinspection AssignmentUsedAsCondition
            if (useOriginalStoryboardRenderer = VideoInformation.playerParametersAreShort(parameters)) {
                isPlayingShorts = true;
                return parameters;
            }
            isPlayingShorts = false;

            boolean isPlayingFeed = PlayerType.getCurrent() == PlayerType.INLINE_MINIMAL
                    && containsAny(parameters, AUTOPLAY_PARAMETERS);
            if (isPlayingFeed) {
                //noinspection AssignmentUsedAsCondition
                if (useOriginalStoryboardRenderer = !SettingsEnum.SPOOF_SIGNATURE_IN_FEED.getBoolean()) {
                    // Don't spoof the feed video playback. This will cause video playback issues,
                    // but only if user continues watching for more than 1 minute.
                    return parameters;
                }
                // Spoof the feed video.  Video will show up in watch history and video subtitles are missing.
                fetchStoryboardRenderer();
                return SCRIM_PARAMETER + INCOGNITO_PARAMETERS;
            }

            fetchStoryboardRenderer();
        } catch (Exception ex) {
            LogHelper.printException(() -> "spoofParameter failure", ex);
        }
        return INCOGNITO_PARAMETERS;
    }

    private static void fetchStoryboardRenderer() {
        if (!SettingsEnum.SPOOF_STORYBOARD_RENDERER.getBoolean()) {
            lastPlayerResponseVideoId = null;
            videoRenderer = null;
            return;
        }
        String videoId = VideoInformation.getPlayerResponseVideoId();
        if (!videoId.equals(lastPlayerResponseVideoId)) {
            lastPlayerResponseVideoId = videoId;
            // This will block starting video playback until the fetch completes.
            // This is desired because if this returns without finishing the fetch,
            // then video will start playback but the image will be frozen
            // while the main thread call for the renderer waits for the fetch to complete.
            videoRenderer = StoryboardRendererRequester.getStoryboardRenderer(videoId);
        }
    }

    private static String getStoryboardRendererSpec(String originalStoryboardRendererSpec,
                                                    boolean returnNullIfLiveStream) {
        if (SettingsEnum.SPOOF_SIGNATURE.getBoolean() && !useOriginalStoryboardRenderer) {
            StoryboardRenderer renderer = videoRenderer;
            if (renderer != null) {
                if (returnNullIfLiveStream && renderer.isLiveStream()) return null;
                return renderer.getSpec();
            }
        }

        return originalStoryboardRendererSpec;
    }

    /**
     * Injection point.
     * Called from background threads and from the main thread.
     */
    @Nullable
    public static String getStoryboardRendererSpec(String originalStoryboardRendererSpec) {
        return getStoryboardRendererSpec(originalStoryboardRendererSpec, false);
    }

    /**
     * Injection point.
     * Uses additional check to handle live streams.
     * Called from background threads and from the main thread.
     */
    @Nullable
    public static String getStoryboardDecoderRendererSpec(String originalStoryboardRendererSpec) {
        return getStoryboardRendererSpec(originalStoryboardRendererSpec, true);
    }

    /**
     * Injection point.
     */
    public static int getRecommendedLevel(int originalLevel) {
        if (SettingsEnum.SPOOF_SIGNATURE.getBoolean() && !useOriginalStoryboardRenderer) {
            StoryboardRenderer renderer = videoRenderer;
            if (renderer != null) {
                Integer recommendedLevel = renderer.getRecommendedLevel();
                if (recommendedLevel != null) return recommendedLevel;
            }
        }

        return originalLevel;
    }

    /**
     * Injection point.  Forces seekbar to be shown for paid videos or
     * if {@link SettingsEnum#SPOOF_STORYBOARD_RENDERER} is not enabled.
     */
    public static boolean getSeekbarThumbnailOverrideValue() {
        if (!SettingsEnum.SPOOF_SIGNATURE.getBoolean()) {
            return false;
        }
        StoryboardRenderer renderer = videoRenderer;
        if (renderer == null) {
            // Spoof storyboard renderer is turned off,
            // video is paid, or the storyboard fetch timed out.
            // Show empty thumbnails so the seek time and chapters still show up.
            return true;
        }
        return renderer.getSpec() != null;
    }

    /**
     * Injection point.
     *
     * @param view seekbar thumbnail view.  Includes both shorts and regular videos.
     */
    public static void seekbarImageViewCreated(ImageView view) {
        try {
            if (!SettingsEnum.SPOOF_SIGNATURE.getBoolean()
                    || SettingsEnum.SPOOF_STORYBOARD_RENDERER.getBoolean()) {
                return;
            }
            if (isPlayingShorts) return;

            view.setVisibility(View.GONE);
            // Also hide the border around the thumbnail (otherwise a 1 pixel wide bordered frame is visible).
            ViewGroup parentLayout = (ViewGroup) view.getParent();
            parentLayout.setPadding(0, 0, 0, 0);
        } catch (Exception ex) {
            LogHelper.printException(() -> "seekbarImageViewCreated failure", ex);
        }
    }
}
