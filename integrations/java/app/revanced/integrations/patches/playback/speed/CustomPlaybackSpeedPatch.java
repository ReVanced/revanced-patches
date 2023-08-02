package app.revanced.integrations.patches.playback.speed;

import android.preference.ListPreference;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import app.revanced.integrations.patches.components.PlaybackSpeedMenuFilterPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import com.facebook.litho.ComponentHost;

import java.util.Arrays;

import static app.revanced.integrations.patches.playback.quality.OldVideoQualityMenuPatch.addRecyclerListener;

public class CustomPlaybackSpeedPatch {
    /**
     * Maximum playback speed, exclusive value.  Custom speeds must be less than this value.
     */
    public static final float MAXIMUM_PLAYBACK_SPEED = 10;

    /**
     * Custom playback speeds.
     */
    public static float[] customPlaybackSpeeds;

    /**
     * Minimum value of {@link #customPlaybackSpeeds}
     */
    public static float minPlaybackSpeed;

    /**
     * Maxium value of {@link #customPlaybackSpeeds}
     */
    public static float maxPlaybackSpeed;

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
                minPlaybackSpeed = Math.min(minPlaybackSpeed, speed);
                maxPlaybackSpeed = Math.max(maxPlaybackSpeed, speed);
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

    /*
     * To reduce copy and paste between two similar code paths.
     */
    public static void onFlyoutMenuCreate(final LinearLayout linearLayout) {
        // The playback rate menu is a RecyclerView with 2 children. The third child is the "Advanced" quality menu.
        addRecyclerListener(linearLayout, 2, 1, recyclerView -> {
            if (PlaybackSpeedMenuFilterPatch.isPlaybackSpeedMenuVisible) {
                PlaybackSpeedMenuFilterPatch.isPlaybackSpeedMenuVisible = false;

                if (recyclerView.getChildCount() == 1 && recyclerView.getChildAt(0) instanceof ComponentHost) {
                    linearLayout.setVisibility(View.GONE);

                    // Close the new Playback speed menu and instead show the old one.
                    showOldPlaybackSpeedMenu();

                    // DismissView [R.id.touch_outside] is the 1st ChildView of the 3rd ParentView.
                    ((ViewGroup) linearLayout.getParent().getParent().getParent())
                            .getChildAt(0).performClick();
                }
            }
        });
    }

    public static void showOldPlaybackSpeedMenu() {
        LogHelper.printDebug(() -> "Old video quality menu shown");

        // Rest of the implementation added by patch.
    }
}
