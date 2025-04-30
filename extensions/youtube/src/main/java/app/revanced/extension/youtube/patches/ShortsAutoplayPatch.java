package app.revanced.extension.youtube.patches;

import android.app.Activity;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ShortsAutoplayPatch {

    private enum ShortsLoopBehavior {
        UNKNOWN,
        /**
         * Repeat the same Short forever!
         */
        REPEAT,
        /**
         * Play once, then advanced to the next Short.
         */
        SINGLE_PLAY,
        /**
         * Pause playback after 1 play.
         */
        END_SCREEN;

        static void setYTEnumValue(Enum<?> ytBehavior) {
            for (ShortsLoopBehavior rvBehavior : values()) {
                if (ytBehavior.name().endsWith(rvBehavior.name())) {
                    rvBehavior.ytEnumValue = ytBehavior;

                    Logger.printDebug(() -> rvBehavior + " set to YT enum: " + ytBehavior.name());
                    return;
                }
            }

            Logger.printException(() -> "Unknown Shorts loop behavior: " + ytBehavior.name());
        }

        /**
         * YouTube enum value of the obfuscated enum type.
         */
        private Enum<?> ytEnumValue;
    }

    private static WeakReference<Activity> mainActivityRef = new WeakReference<>(null);


    public static void setMainActivity(Activity activity) {
        mainActivityRef = new WeakReference<>(activity);
    }

    /**
     * @return If the app is currently in background PiP mode.
     */
    private static boolean isAppInBackgroundPiPMode() {
        Activity activity = mainActivityRef.get();
        return activity != null && activity.isInPictureInPictureMode();
    }

    /**
     * Injection point.
     */
    public static void setYTShortsRepeatEnum(Enum<?> ytEnum) {
        try {
            for (Enum<?> ytBehavior : Objects.requireNonNull(ytEnum.getClass().getEnumConstants())) {
                ShortsLoopBehavior.setYTEnumValue(ytBehavior);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setYTShortsRepeatEnum failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static Enum<?> changeShortsRepeatBehavior(@Nullable Enum<?> original) {
        try {
            final boolean autoplay;

            if (isAppInBackgroundPiPMode()) {
                if (!VersionCheckPatch.IS_19_34_OR_GREATER) {
                    // 19.34+ is required to set background play behavior.
                    Logger.printDebug(() -> "PiP Shorts not supported, using original repeat behavior");

                    return original;
                }

                autoplay = Settings.SHORTS_AUTOPLAY_BACKGROUND.get();
            } else {
                autoplay = Settings.SHORTS_AUTOPLAY.get();
            }

            final ShortsLoopBehavior behavior = autoplay
                    ? ShortsLoopBehavior.SINGLE_PLAY
                    : ShortsLoopBehavior.REPEAT;

            if (behavior.ytEnumValue != null) {
                Logger.printDebug(() -> {
                    String name = (original == null ? "unknown (null)" : original.name());
                    return behavior == original
                            ? "Behavior setting is same as original. Using original: " + name
                            : "Changing Shorts repeat behavior from: " + name + " to: " + behavior.name();
                });

                return behavior.ytEnumValue;
            }

            if (original == null) {
                // Cannot return null, as null is used to indicate Short was auto played.
                // Unpatched app replaces null with unknown enum type (appears to fix for bad api data).
                Enum<?> unknown = ShortsLoopBehavior.UNKNOWN.ytEnumValue;
                Logger.printDebug(() -> "Original is null, returning: " + unknown.name());
                return unknown;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "changeShortsRepeatBehavior failure", ex);
        }

        return original;
    }


    /**
     * Injection point.
     */
    public static boolean isAutoPlay(Enum<?> original) {
        return ShortsLoopBehavior.SINGLE_PLAY.ytEnumValue == original;
    }
}
