package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.spoof.DeviceHardwareSupport.DEVICE_HAS_HARDWARE_DECODING_VP9;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

@SuppressWarnings({"unused", "deprecation"})
public class ForceAVCSpoofingPreference extends SwitchPreference {
    {
        if (!DEVICE_HAS_HARDWARE_DECODING_VP9) {
            setSummaryOn(str("revanced_spoof_video_streams_ios_force_avc_no_hardware_vp9_summary_on"));
        }
    }

    public ForceAVCSpoofingPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ForceAVCSpoofingPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ForceAVCSpoofingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ForceAVCSpoofingPreference(Context context) {
        super(context);
    }

    private void updateUI() {
        if (DEVICE_HAS_HARDWARE_DECODING_VP9) {
            return;
        }

        // Temporarily remove the preference key to allow changing this preference without
        // causing the settings UI listeners from showing reboot dialogs by the changes made here.
        String key = getKey();
        setKey(null);

        // This setting cannot be changed by the user.
        super.setEnabled(false);
        super.setChecked(true);

        setKey(key);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        updateUI();
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        updateUI();
    }
}
