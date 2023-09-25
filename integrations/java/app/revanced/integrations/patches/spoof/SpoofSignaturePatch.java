package app.revanced.integrations.patches.spoof;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;

import static app.revanced.integrations.patches.spoof.requests.StoryBoardRendererRequester.fetchStoryboardsRenderer;
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

    private static boolean isPlayingShorts;

    private static String storyboardRendererSpec = "";
    private static int recommendedLevel;

    /**
     * Injection point.
     *
     * @param parameters Original protobuf parameter value.
     */
    public static String spoofParameter(String parameters) {
        LogHelper.printDebug(() -> "Original protobuf parameter value: " + parameters);

        if (!SettingsEnum.SPOOF_SIGNATURE.getBoolean()) return parameters;

        // Clip's player parameters contain a lot of information (e.g. video start and end time or whether it loops)
        // For this reason, the player parameters of a clip are usually very long (150~300 characters).
        // Clips are 60 seconds or less in length, so no spoofing.
        var isClip = parameters.length() > 150;
        if (isClip) return parameters;


        // Shorts do not need to be spoofed.
        //noinspection AssignmentUsedAsCondition
        if (isPlayingShorts = parameters.startsWith(SHORTS_PLAYER_PARAMETERS)) return parameters;

        boolean isPlayingFeed = PlayerType.getCurrent() == PlayerType.INLINE_MINIMAL && containsAny(parameters, AUTOPLAY_PARAMETERS);
        if (isPlayingFeed) return SettingsEnum.SPOOF_SIGNATURE_IN_FEED.getBoolean() ?
                // Prepend the scrim parameter to mute videos in feed.
                SCRIM_PARAMETER + INCOGNITO_PARAMETERS :
                // In order to prevent videos that are auto-played in feed to be added to history,
                // only spoof the parameter if the video is not playing in the feed.
                // This will cause playback issues in the feed, but it's better than manipulating the history.
                parameters;

        fetchStoryboardsRenderer(VideoInformation.getVideoId());
        return INCOGNITO_PARAMETERS;
    }

    /**
     * Injection point.
     */
    public static boolean getSeekbarThumbnailOverrideValue() {
        return SettingsEnum.SPOOF_SIGNATURE.getBoolean();
    }


    /**
     * Injection point.
     */
    public static String getStoryboardRendererSpec(String originalStoryboardRendererSpec) {
        if (!SettingsEnum.SPOOF_SIGNATURE.getBoolean()) return originalStoryboardRendererSpec;

        return storyboardRendererSpec;
    }

    /**
     * Injection point.
     */
    public static int getRecommendedLevel(int originalLevel) {
        if (!SettingsEnum.SPOOF_SIGNATURE.getBoolean()) return originalLevel;

        return recommendedLevel;
    }

    public static void setStoryboardRendererSpec(String newlyLoadedStoryboardRendererSpec) {
        storyboardRendererSpec = newlyLoadedStoryboardRendererSpec;
    }

    public static void setRecommendedLevel(int level) {
        recommendedLevel = level;
    }
}
