package fi.vanced.libraries.youtube.ui;

import static fi.razerman.youtube.XGlobals.debug;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import com.google.android.apps.youtube.app.ui.SlimMetadataScrollableButtonContainerLayout;

import fi.vanced.libraries.youtube.whitelisting.Whitelist;
import fi.vanced.libraries.youtube.whitelisting.WhitelistType;
import fi.vanced.utils.SharedPrefUtils;
import fi.vanced.utils.VancedUtils;

public class SlimButtonContainer extends SlimMetadataScrollableButtonContainerLayout {
    private static final String TAG = "VI - Slim - Container";
    private ViewGroup container;
    private CopyButton copyButton;
    private CopyWithTimestamp copyWithTimestampButton;
    public static AdButton adBlockButton;
    public static SBWhitelistButton sbWhitelistButton;
    private final Context context;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    public SlimButtonContainer(Context context) {
        super(context);
        this.context = context;
        this.initialize(context);
    }

    public SlimButtonContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.initialize(context);
    }

    public SlimButtonContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.initialize(context);
    }

    public void initialize(Context context) {
        try {
            container = this.findViewById(VancedUtils.getIdentifier("button_container_vanced", "id"));
            if (container == null) throw new Exception("Unable to initialize the button container because the button_container_vanced couldn't be found");

            copyButton = new CopyButton(context, this);
            copyWithTimestampButton = new CopyWithTimestamp(context, this);
            adBlockButton = new AdButton(context, this);
            sbWhitelistButton = new SBWhitelistButton(context, this);
            new SponsorBlockVoting(context, this);

            addSharedPrefsChangeListener();
        }
        catch (Exception ex) {
            Log.e(TAG, "Unable to initialize the button container", ex);
        }
    }

    private void addSharedPrefsChangeListener() {
        listener = (sharedPreferences, key) -> {
            try {
                if (debug) {
                    Log.d(TAG, String.format("SharedPreference changed with key %s", key));
                }
                if ("pref_copy_video_url_button_list".equals(key) && copyButton != null) {
                    copyButton.setVisible(ButtonVisibility.isVisibleInContainer(context, "pref_copy_video_url_button_list"));
                    return;
                }
                if ("pref_copy_video_url_timestamp_button_list".equals(key) && copyWithTimestampButton != null) {
                    copyWithTimestampButton.setVisible(ButtonVisibility.isVisibleInContainer(context, "pref_copy_video_url_timestamp_button_list"));
                    return;
                }
                WhitelistType whitelistAds = WhitelistType.ADS;
                String adsEnabledPreferenceName = whitelistAds.getPreferenceEnabledName();
                if (adsEnabledPreferenceName.equals(key) && adBlockButton != null) {
                    boolean enabled = SharedPrefUtils.getBoolean(context, "youtube", adsEnabledPreferenceName, false);
                    Whitelist.setEnabled(whitelistAds, enabled);
                    adBlockButton.setVisible(enabled);
                    return;
                }
                WhitelistType whitelistSB = WhitelistType.SPONSORBLOCK;
                String sbEnabledPreferenceName = whitelistSB.getPreferenceEnabledName();
                if (sbEnabledPreferenceName.equals(key) && sbWhitelistButton != null) {
                    boolean enabled = SharedPrefUtils.getBoolean(context, "youtube", sbEnabledPreferenceName, false);
                    Whitelist.setEnabled(whitelistSB, enabled);
                    sbWhitelistButton.setVisible(enabled);
                    return;
                }
            }
            catch (Exception ex) {
                Log.e(TAG, "Error handling shared preference change", ex);
            }
        };

        context.getSharedPreferences("youtube", Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(listener);
    }
}
