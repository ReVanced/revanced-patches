package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.content.Context;
import android.util.AttributeSet;

import app.revanced.extension.shared.settings.preference.SortedListPreference;
import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings({"deprecation", "unused"})
public class SpoofAudioSelectorListPreference extends SortedListPreference {

    private final boolean available;

    {
        final boolean isAndroidStudio = Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get() == ClientType.ANDROID_CREATOR;

        if (isAndroidStudio || SpoofVideoStreamsPatch.getLanguageOverride() != null) {
            available = false;
            super.setEnabled(false);
            super.setSummary(str(isAndroidStudio
                    ? "revanced_spoof_video_streams_language_android_studio"
                    : "revanced_spoof_video_streams_language_not_available"));
        } else {
            available = true;
        }
    }

    public SpoofAudioSelectorListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public SpoofAudioSelectorListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public SpoofAudioSelectorListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SpoofAudioSelectorListPreference(Context context) {
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

