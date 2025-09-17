package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings({"deprecation", "unused"})
public class SpoofStreamingDataSideEffectsPreference extends Preference {

    @Nullable
    private ClientType currentClientType;

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        // Because this listener may run before the ReVanced settings fragment updates Settings,
        // this could show the prior config and not the current.
        //
        // Push this call to the end of the main run queue,
        // so all other listeners are done and Settings is up to date.
        Utils.runOnMainThread(this::updateUI);
    };

    public SpoofStreamingDataSideEffectsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SpoofStreamingDataSideEffectsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SpoofStreamingDataSideEffectsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpoofStreamingDataSideEffectsPreference(Context context) {
        super(context);
    }

    private void addChangeListener() {
        Setting.preferences.preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    private void removeChangeListener() {
        Setting.preferences.preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        updateUI();
        addChangeListener();
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        removeChangeListener();
    }

    private void updateUI() {
        ClientType clientType = Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get();
        if (currentClientType == clientType) {
            return;
        }
        currentClientType = clientType;

        Logger.printDebug(() -> "Updating spoof stream side effects preference");
        setEnabled(BaseSettings.SPOOF_VIDEO_STREAMS.get());

        setTitle(str("revanced_spoof_video_streams_about_title"));

        String summary = str(clientType == ClientType.IPADOS
                ? "revanced_spoof_video_streams_about_ipados_summary"
                // Same base side effects for Android VR, Android Studio, and visionOS.
                : "revanced_spoof_video_streams_about_android_summary");

        if (clientType == ClientType.IPADOS) {
            summary += '\n' + str("revanced_spoof_video_streams_about_no_av1");
        } else if (clientType == ClientType.VISIONOS) {
            summary = str("revanced_spoof_video_streams_about_experimental")
                    + '\n' + summary
                    + '\n' + str("revanced_spoof_video_streams_about_no_av1")
                    + '\n' + str("revanced_spoof_video_streams_about_kids_videos");
        } else if (clientType == ClientType.ANDROID_CREATOR) {
            summary += '\n' + str("revanced_spoof_video_streams_about_no_av1")
                    + '\n' + str("revanced_spoof_video_streams_about_kids_videos");
        }

        setSummary(summary);
    }
}
