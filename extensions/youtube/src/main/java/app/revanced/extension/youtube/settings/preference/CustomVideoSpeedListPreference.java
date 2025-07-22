package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.sf;

import android.content.Context;
import android.util.AttributeSet;

import app.revanced.extension.shared.settings.preference.CustomDialogListPreference;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

/**
 * A custom ListPreference that uses a styled custom dialog with a custom checkmark indicator.
 * Custom video speeds used by {@link CustomPlaybackSpeedPatch}.
 */
@SuppressWarnings({"unused", "deprecation"})
public final class CustomVideoSpeedListPreference extends CustomDialogListPreference {

    {
        // Initialize a settings preference list with the available playback speeds.
        float[] customPlaybackSpeeds = CustomPlaybackSpeedPatch.customPlaybackSpeeds;
        final int numberOfEntries = customPlaybackSpeeds.length + 1;
        String[] preferenceListEntries = new String[numberOfEntries];
        String[] preferenceListEntryValues = new String[numberOfEntries];

        // Auto speed (same behavior as unpatched).
        preferenceListEntries[0] = sf("revanced_custom_playback_speeds_auto").toString();
        preferenceListEntryValues[0] = String.valueOf(Settings.PLAYBACK_SPEED_DEFAULT.defaultValue);

        int i = 1;
        for (float speed : customPlaybackSpeeds) {
            String speedString = String.valueOf(speed);
            preferenceListEntries[i] = speedString + "x";
            preferenceListEntryValues[i] = speedString;
            i++;
        }

        setEntries(preferenceListEntries);
        setEntryValues(preferenceListEntryValues);
    }

    public CustomVideoSpeedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomVideoSpeedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomVideoSpeedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoSpeedListPreference(Context context) {
        super(context);
    }

}
