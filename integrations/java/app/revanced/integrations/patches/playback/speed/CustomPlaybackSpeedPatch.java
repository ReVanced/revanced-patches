package app.revanced.integrations.patches.playback.speed;

import android.preference.ListPreference;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.Arrays;

import app.revanced.integrations.patches.components.PlaybackSpeedMenuFilterPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class CustomPlaybackSpeedPatch {
    /**
     * Maximum playback speed, exclusive value.  Custom speeds must be less than this value.
     * Limit is required otherwise double digit speeds show up out of order in the UI selector.
     */
    public static final float MAXIMUM_PLAYBACK_SPEED = 10;

    /**
     * Custom playback speeds.
     */
    public static float[] customPlaybackSpeeds;

    /**
     * PreferenceList entries and values, of all available playback speeds.
     */
    private static String[] preferenceListEntries, preferenceListEntryValues;

    static {
        loadCustomSpeeds();
    }

    private static void resetCustomSpeeds(@NonNull String toastMessage) {
        ReVancedUtils.showToastLong(toastMessage);
        SettingsEnum.CUSTOM_PLAYBACK_SPEEDS.saveValue(SettingsEnum.CUSTOM_PLAYBACK_SPEEDS.defaultValue);
    }

    private static void loadCustomSpeeds() {
        try {
            String[] speedStrings = SettingsEnum.CUSTOM_PLAYBACK_SPEEDS.getString().split("\\s+");
            Arrays.sort(speedStrings);
            if (speedStrings.length == 0) {
                throw new IllegalArgumentException();
            }
            customPlaybackSpeeds = new float[speedStrings.length];
            for (int i = 0, length = speedStrings.length; i < length; i++) {
                final float speed = Float.parseFloat(speedStrings[i]);
                if (speed <= 0 || arrayContains(customPlaybackSpeeds, speed)) {
                    throw new IllegalArgumentException();
                }
                if (speed >= MAXIMUM_PLAYBACK_SPEED) {
                    resetCustomSpeeds("Custom speeds must be less than " + MAXIMUM_PLAYBACK_SPEED
                            + ".  Using default values.");
                    loadCustomSpeeds();
                    return;
                }
                customPlaybackSpeeds[i] = speed;
            }
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "parse error", ex);
            resetCustomSpeeds("Invalid custom playback speeds. Using default values.");
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
    public static void initializeListPreference(ListPreference preference) {
        if (preferenceListEntries == null) {
            preferenceListEntries = new String[customPlaybackSpeeds.length];
            preferenceListEntryValues = new String[customPlaybackSpeeds.length];
            int i = 0;
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
                // For some reason, the custom playback speed flyout panel is activated when the user opens the share panel. (A/B tests)
                // Check the child count of playback speed flyout panel to prevent this issue.
                // Child count of playback speed flyout panel is always 8.
                if (PlaybackSpeedMenuFilterPatch.isPlaybackSpeedMenuVisible
                        && ((ViewGroup) recyclerView.getChildAt(0)).getChildCount() == 8) {
                    PlaybackSpeedMenuFilterPatch.isPlaybackSpeedMenuVisible = false;
                    ViewGroup parentView3rd = (ViewGroup) recyclerView.getParent().getParent().getParent();
                    ViewGroup parentView4th = (ViewGroup) parentView3rd.getParent();

                    // Dismiss View [R.id.touch_outside] is the 1st ChildView of the 4th ParentView.
                    // This only shows in phone layout.
                    parentView4th.getChildAt(0).performClick();

                    // In tablet layout there is no Dismiss View, instead we just hide all two parent views.
                    parentView3rd.setVisibility(View.GONE);
                    parentView4th.setVisibility(View.GONE);

                    // This works without issues for both tablet and phone layouts,
                    // So no code is needed to check whether the current device is a tablet or phone.

                    // Close the new Playback speed menu and show the old one.
                    showOldPlaybackSpeedMenu();
                }
            } catch (Exception ex) {
                LogHelper.printException(() -> "onFlyoutMenuCreate failure", ex);
            }
        });
    }

    public static void showOldPlaybackSpeedMenu() {
        LogHelper.printDebug(() -> "Old video quality menu shown");

        // Rest of the implementation added by patch.
    }
}
