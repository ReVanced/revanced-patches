package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch;

@SuppressWarnings({"deprecation", "unused"})
public class ForceOriginalAudioSwitchPreference extends SwitchPreference {

    // Spoof stream patch is not included, or is not currently spoofing to Android Studio.
    private static final boolean available = !SpoofVideoStreamsPatch.isPatchIncluded()
            || !(BaseSettings.SPOOF_VIDEO_STREAMS.get()
            && SpoofVideoStreamsPatch.getPreferredClient() == ClientType.ANDROID_CREATOR);

    {
        if (!available) {
            // Show why force audio is not available.
            String summary = str("revanced_force_original_audio_not_available");
            super.setSummary(summary);
            super.setSummaryOn(summary);
            super.setSummaryOff(summary);
            super.setEnabled(false);
        }
    }

    public ForceOriginalAudioSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ForceOriginalAudioSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ForceOriginalAudioSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ForceOriginalAudioSwitchPreference(Context context) {
        super(context);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!available) {
            return;
        }

        super.setEnabled(enabled);
    }

    @Override
    public void setSummary(CharSequence summary) {
        if (!available) {
            return;
        }

        super.setSummary(summary);
    }
}

