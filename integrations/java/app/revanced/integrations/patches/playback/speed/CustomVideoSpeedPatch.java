package app.revanced.integrations.patches.playback.speed;

public class CustomVideoSpeedPatch {
    /**
     * Default playback speeds offered by YouTube.
     * Values are also used by {@link RememberPlaybackSpeedPatch}.
     *
     * If custom video speed is applied,
     * then this array is overwritten by the patch with custom speeds
     */
    public static final float[] videoSpeeds = {0.25f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};
}
