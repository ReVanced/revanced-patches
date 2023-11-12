package app.revanced.integrations.patches.spoof;

import static app.revanced.integrations.patches.spoof.requests.StoryboardRendererRequester.getStoryboardRenderer;
import static app.revanced.integrations.utils.ReVancedUtils.containsAny;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/** @noinspection unused*/
public class SpoofSignaturePatch {
    /**
     * Parameter (also used by
     * <a href="https://github.com/yt-dlp/yt-dlp/blob/81ca451480051d7ce1a31c017e005358345a9149/yt_dlp/extractor/youtube.py#L3602">yt-dlp</a>)
     * to fix playback issues.
     */
    private static final String INCOGNITO_PARAMETERS = "CgIQBg==";

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
     * Parameters used in YouTube Shorts.
     */
    private static final String SHORTS_PLAYER_PARAMETERS = "8AEB";

    /**
     * Last video id loaded. Used to prevent reloading the same spec multiple times.
     */
    private static volatile String lastPlayerResponseVideoId;

    private static volatile Future<StoryboardRenderer> rendererFuture;

    private static volatile boolean useOriginalStoryboardRenderer;

    private static volatile boolean isPlayingShorts;

    @Nullable
    private static StoryboardRenderer getRenderer() {
        if (rendererFuture != null) {
            try {
                return rendererFuture.get(2000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ex) {
                LogHelper.printDebug(() -> "Could not get renderer (get timed out)");
            } catch (ExecutionException | InterruptedException ex) {
                // Should never happen.
                LogHelper.printException(() -> "Could not get renderer", ex);
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
    public static String spoofParameter(String parameters) {
        LogHelper.printDebug(() -> "Original protobuf parameter value: " + parameters);

        if (!SettingsEnum.SPOOF_SIGNATURE.getBoolean()) return parameters;

        // Clip's player parameters contain a lot of information (e.g. video start and end time or whether it loops)
        // For this reason, the player parameters of a clip are usually very long (150~300 characters).
        // Clips are 60 seconds or less in length, so no spoofing.
        if (useOriginalStoryboardRenderer = parameters.length() > 150) return parameters;

        // Shorts do not need to be spoofed.
        if (useOriginalStoryboardRenderer = parameters.startsWith(SHORTS_PLAYER_PARAMETERS)) {
            isPlayingShorts = true;
            return parameters;
        }
        isPlayingShorts = false;

        boolean isPlayingFeed = PlayerType.getCurrent() == PlayerType.INLINE_MINIMAL
                && containsAny(parameters, AUTOPLAY_PARAMETERS);
        if (isPlayingFeed) {
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
        return INCOGNITO_PARAMETERS;
    }

    private static void fetchStoryboardRenderer() {
        if (!SettingsEnum.SPOOF_STORYBOARD_RENDERER.getBoolean()) {
            lastPlayerResponseVideoId = null;
            rendererFuture = null;
            return;
        }
        String videoId = VideoInformation.getPlayerResponseVideoId();
        if (!videoId.equals(lastPlayerResponseVideoId)) {
            rendererFuture = ReVancedUtils.submitOnBackgroundThread(() -> getStoryboardRenderer(videoId));
            lastPlayerResponseVideoId = videoId;
        }
        // Block until the fetch is completed.  Without this, occasionally when a new video is opened
        // the video will be frozen a few seconds while the audio plays.
        // This is because the main thread is calling to get the storyboard but the fetch is not completed.
        // To prevent this, call get() here and block until the fetch is completed.
        // So later when the main thread calls to get the renderer it will never block as the future is done.
        getRenderer();
    }

    private static String getStoryboardRendererSpec(String originalStoryboardRendererSpec,
                                                    boolean returnNullIfLiveStream) {
        if (SettingsEnum.SPOOF_SIGNATURE.getBoolean() && !useOriginalStoryboardRenderer) {
            StoryboardRenderer renderer = getRenderer();
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
            StoryboardRenderer renderer = getRenderer();
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
        return SettingsEnum.SPOOF_SIGNATURE.getBoolean();
    }

    /**
     * Injection point.
     *
     * @param view seekbar thumbnail view.  Includes both shorts and regular videos.
     */
    public static void seekbarImageViewCreated(ImageView view) {
        if (!SettingsEnum.SPOOF_SIGNATURE.getBoolean()
                || SettingsEnum.SPOOF_STORYBOARD_RENDERER.getBoolean()) {
            return;
        }
        if (isPlayingShorts) return;

        view.setVisibility(View.GONE);
        // Also hide the border around the thumbnail (otherwise a 1 pixel wide bordered frame is visible).
        ViewGroup parentLayout = (ViewGroup) view.getParent();
        parentLayout.setPadding(0, 0, 0, 0);
    }
}
