package app.revanced.extension.youtube.patches.playback.speed;

import static app.revanced.extension.shared.StringRef.sf;
import static app.revanced.extension.shared.StringRef.str;

import android.preference.ListPreference;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;

import java.util.Arrays;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.components.PlaybackSpeedMenuFilterPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class CustomPlaybackSpeedPatch {

    private static final float PLAYBACK_SPEED_AUTO = Settings.PLAYBACK_SPEED_DEFAULT.defaultValue;

    /**
     * Maximum playback speed, exclusive value.  Custom speeds must be less than this value.
     * <p>
     * Going over 8x does not increase the actual playback speed any higher,
     * and the UI selector starts flickering and acting weird.
     * Over 10x and the speeds show up out of order in the UI selector.
     */
    public static final float PLAYBACK_SPEED_MAXIMUM = 8;

    /**
     * Custom playback speeds.
     */
    public static float[] customPlaybackSpeeds;

    /**
     * The last time the old playback menu was forcefully called.
     */
    private static long lastTimeOldPlaybackMenuInvoked;

    /**
     * PreferenceList entries and values, of all available playback speeds.
     */
    private static String[] preferenceListEntries, preferenceListEntryValues;

    static {
        loadCustomSpeeds();
    }

    private static void resetCustomSpeeds(@NonNull String toastMessage) {
        Utils.showToastLong(toastMessage);
        Settings.CUSTOM_PLAYBACK_SPEEDS.resetToDefault();
    }

    private static void loadCustomSpeeds() {
        try {
            String[] speedStrings = Settings.CUSTOM_PLAYBACK_SPEEDS.get().split("\\s+");
            Arrays.sort(speedStrings);
            if (speedStrings.length == 0) {
                throw new IllegalArgumentException();
            }

            customPlaybackSpeeds = new float[speedStrings.length];

            int i = 0;
            for (String speedString : speedStrings) {
                final float speedFloat = Float.parseFloat(speedString);
                if (speedFloat <= 0 || arrayContains(customPlaybackSpeeds, speedFloat)) {
                    throw new IllegalArgumentException();
                }

                if (speedFloat >= PLAYBACK_SPEED_MAXIMUM) {
                    resetCustomSpeeds(str("revanced_custom_playback_speeds_invalid", PLAYBACK_SPEED_MAXIMUM));
                    loadCustomSpeeds();
                    return;
                }

                customPlaybackSpeeds[i] = speedFloat;
                i++;
            }
        } catch (Exception ex) {
            Logger.printInfo(() -> "parse error", ex);
            resetCustomSpeeds(str("revanced_custom_playback_speeds_parse_exception"));
            loadCustomSpeeds();
        }
    }

    private static boolean arrayContains(float[] array, float value) {
        for (float arrayValue : array) {
            if (arrayValue == value) return true;
        }
        return false;
    }

    /**
     * Initialize a settings preference list with the available playback speeds.
     */
    @SuppressWarnings("deprecation")
    public static void initializeListPreference(ListPreference preference) {
        if (preferenceListEntries == null) {
            final int numberOfEntries = customPlaybackSpeeds.length + 1;
            preferenceListEntries = new String[numberOfEntries];
            preferenceListEntryValues = new String[numberOfEntries];

            // Auto speed (same behavior as unpatched).
            preferenceListEntries[0] = sf("revanced_custom_playback_speeds_auto").toString();
            preferenceListEntryValues[0] = String.valueOf(PLAYBACK_SPEED_AUTO);

            int i = 1;
            for (float speed : customPlaybackSpeeds) {
                String speedString = String.valueOf(speed);
                preferenceListEntries[i] = speedString + "x";
                preferenceListEntryValues[i] = speedString;
                i++;
            }
        }

        preference.setEntries(preferenceListEntries);
        preference.setEntryValues(preferenceListEntryValues);
    }

    /**
     * Injection point.
     */
    public static void onFlyoutMenuCreate(RecyclerView recyclerView) {
        recyclerView.getViewTreeObserver().addOnDrawListener(() -> {
            try {
                if (PlaybackSpeedMenuFilterPatch.isPlaybackRateSelectorMenuVisible) {
                    if (hideLithoMenuAndShowOldSpeedMenu(recyclerView, 5)) {
                        PlaybackSpeedMenuFilterPatch.isPlaybackRateSelectorMenuVisible = false;
                    }
                    return;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "isPlaybackRateSelectorMenuVisible failure", ex);
            }

            try {
                if (PlaybackSpeedMenuFilterPatch.isOldPlaybackSpeedMenuVisible) {
                    if (hideLithoMenuAndShowOldSpeedMenu(recyclerView, 8)) {
                        PlaybackSpeedMenuFilterPatch.isOldPlaybackSpeedMenuVisible = false;
                    }
                }
            } catch (Exception ex) {
                Logger.printException(() -> "isOldPlaybackSpeedMenuVisible failure", ex);
            }
        });
    }

    private static boolean hideLithoMenuAndShowOldSpeedMenu(RecyclerView recyclerView, int expectedChildCount) {
        if (recyclerView.getChildCount() == 0) {
            return false;
        }

        View firstChild = recyclerView.getChildAt(0);
        if (!(firstChild instanceof ViewGroup PlaybackSpeedParentView)) {
            return false;
        }

        if (PlaybackSpeedParentView.getChildCount() != expectedChildCount) {
            return false;
        }

        ViewParent parentView3rd = Utils.getParentView(recyclerView, 3);
        if (!(parentView3rd instanceof ViewGroup)) {
            return true;
        }

        ViewParent parentView4th = parentView3rd.getParent();
        if (!(parentView4th instanceof ViewGroup)) {
            return true;
        }

        // Dismiss View [R.id.touch_outside] is the 1st ChildView of the 4th ParentView.
        // This only shows in phone layout.
        final var touchInsidedView = ((ViewGroup) parentView4th).getChildAt(0);
        touchInsidedView.setSoundEffectsEnabled(false);
        touchInsidedView.performClick();

        // In tablet layout there is no Dismiss View, instead we just hide all two parent views.
        ((ViewGroup) parentView3rd).setVisibility(View.GONE);
        ((ViewGroup) parentView4th).setVisibility(View.GONE);

        // Close the litho speed menu and show the old one.
        showOldPlaybackSpeedMenu();

        return true;
    }

    public static void showOldPlaybackSpeedMenu() {
        // This method is sometimes used multiple times.
        // To prevent this, ignore method reuse within 1 second.
        final long now = System.currentTimeMillis();
        if (now - lastTimeOldPlaybackMenuInvoked < 1000) {
            Logger.printDebug(() -> "Ignoring call to showOldPlaybackSpeedMenu");
            return;
        }
        lastTimeOldPlaybackMenuInvoked = now;
        Logger.printDebug(() -> "Old video quality menu shown");

        // Rest of the implementation added by patch.
    }
}
