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
                String ytName = ytBehavior.name();
                if (ytName.endsWith(rvBehavior.name())) {
                    if (rvBehavior.ytEnumValue != null) {
                        Logger.printException(() -> "Conflicting behavior names: " + rvBehavior
                                + " ytBehavior: " + ytName);
                    } else {
                        rvBehavior.ytEnumValue = ytBehavior;
                        Logger.printDebug(() -> rvBehavior + " set to YT enum: " + ytName);
                    }
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

            Enum<?> overrideBehavior = (autoplay
                    ? ShortsLoopBehavior.SINGLE_PLAY
                    : ShortsLoopBehavior.REPEAT).ytEnumValue;

            if (overrideBehavior != null) {
                Logger.printDebug(() -> {
                    String name = original == null ? "unknown (null)" : original.name();
                    return overrideBehavior == original
                            ? "Behavior setting is same as original. Using original: " + name
                            : "Changing Shorts repeat behavior from: " + name + " to: " + overrideBehavior.name();
                });

                return overrideBehavior;
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
