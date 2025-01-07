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
        ClientType clientType = BaseSettings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get();
        if (currentClientType == clientType) {
            return;
        }

        Logger.printDebug(() -> "Updating spoof stream side effects preference");
        setEnabled(BaseSettings.SPOOF_VIDEO_STREAMS.get());

        String key = "revanced_spoof_video_streams_about_" +
                (clientType == ClientType.IOS_UNPLUGGED
                        ? "ios_tv"
                        : "android");
        String title = str(key + "_title");
        String summary = str(key + "_summary");

        // Android VR supports AV1 but all other clients do not.
        if (clientType != ClientType.ANDROID_VR && clientType != ClientType.ANDROID_VR_NO_AUTH) {
            summary += '\n' + str("revanced_spoof_video_streams_about_no_av1");

            // Android Creator does not support HDR.
            if (clientType == ClientType.ANDROID_CREATOR) {
                summary += '\n' + str("revanced_spoof_video_streams_about_no_hdr");
            }
        }

        setTitle(title);
        setSummary(summary);
    }
}
