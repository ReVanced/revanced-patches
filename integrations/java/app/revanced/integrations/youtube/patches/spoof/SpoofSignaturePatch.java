package app.revanced.integrations.youtube.patches.spoof;

import static app.revanced.integrations.youtube.patches.spoof.requests.StoryboardRendererRequester.getStoryboardRenderer;
import static app.revanced.integrations.shared.Utils.containsAny;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.PlayerType;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

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
    private static volatile Future<StoryboardRenderer> rendererFuture;

    private static volatile boolean useOriginalStoryboardRenderer;

    private static volatile boolean isPlayingShorts;

    @Nullable
    private static StoryboardRenderer getRenderer(boolean waitForCompletion) {
        Future<StoryboardRenderer> future = rendererFuture;
        if (future != null) {
            try {
                if (waitForCompletion || future.isDone()) {
                    return future.get(20000, TimeUnit.MILLISECONDS); // Any arbitrarily large timeout.
                } // else, return null.
            } catch (TimeoutException ex) {
                Logger.printDebug(() -> "Could not get renderer (get timed out)");
            } catch (ExecutionException | InterruptedException ex) {
                // Should never happen.
                Logger.printException(() -> "Could not get renderer", ex);
            }
        }
        return null;
    }

    /**
     * Injection point.
     *
     * Called off the main thread, and called multiple times for each video.
     *
     * @param parameters Original protobuf parameter value.
     */
    public static String spoofParameter(String parameters, boolean isShortAndOpeningOrPlaying) {
        try {
            Logger.printDebug(() -> "Original protobuf parameter value: " + parameters);

            if (!Settings.SPOOF_SIGNATURE.get()) {
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
                if (useOriginalStoryboardRenderer = !Settings.SPOOF_SIGNATURE_IN_FEED.get()) {
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
            Logger.printException(() -> "spoofParameter failure", ex);
        }
        return INCOGNITO_PARAMETERS;
    }

    private static void fetchStoryboardRenderer() {
        if (!Settings.SPOOF_STORYBOARD_RENDERER.get()) {
            lastPlayerResponseVideoId = null;
            rendererFuture = null;
            return;
        }
        String videoId = VideoInformation.getPlayerResponseVideoId();
        if (!videoId.equals(lastPlayerResponseVideoId)) {
            rendererFuture = Utils.submitOnBackgroundThread(() -> getStoryboardRenderer(videoId));
            lastPlayerResponseVideoId = videoId;
        }
        // Block until the renderer fetch completes.
        // This is desired because if this returns without finishing the fetch
        // then video will start playback but the storyboard is not ready yet.
        getRenderer(true);
    }

    private static String getStoryboardRendererSpec(String originalStoryboardRendererSpec,
                                                    boolean returnNullIfLiveStream) {
        if (Settings.SPOOF_SIGNATURE.get() && !useOriginalStoryboardRenderer) {
            StoryboardRenderer renderer = getRenderer(false);
            if (renderer != null) {
                if (returnNullIfLiveStream && renderer.isLiveStream()) {
                    return null;
                }
                String spec = renderer.getSpec();
                if (spec != null) {
                    return spec;
                }
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
        if (Settings.SPOOF_SIGNATURE.get() && !useOriginalStoryboardRenderer) {
            StoryboardRenderer renderer = getRenderer(false);
            if (renderer != null) {
                Integer recommendedLevel = renderer.getRecommendedLevel();
                if (recommendedLevel != null) return recommendedLevel;
            }
        }

        return originalLevel;
    }

    /**
     * Injection point.  Forces seekbar to be shown for paid videos or
     * if {@link Settings#SPOOF_STORYBOARD_RENDERER} is not enabled.
     */
    public static boolean getSeekbarThumbnailOverrideValue() {
        if (!Settings.SPOOF_SIGNATURE.get()) {
            return false;
        }
        StoryboardRenderer renderer = getRenderer(false);
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
            if (!Settings.SPOOF_SIGNATURE.get()
                    || Settings.SPOOF_STORYBOARD_RENDERER.get()) {
                return;
            }
            if (isPlayingShorts) return;

            view.setVisibility(View.GONE);
            // Also hide the border around the thumbnail (otherwise a 1 pixel wide bordered frame is visible).
            ViewGroup parentLayout = (ViewGroup) view.getParent();
            parentLayout.setPadding(0, 0, 0, 0);
        } catch (Exception ex) {
            Logger.printException(() -> "seekbarImageViewCreated failure", ex);
        }
    }
}
