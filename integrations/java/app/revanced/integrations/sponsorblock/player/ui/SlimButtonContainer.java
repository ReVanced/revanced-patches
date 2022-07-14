package app.revanced.integrations.sponsorblock.player.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import android.view.ViewGroup;

import com.google.android.apps.youtube.app.ui.SlimMetadataScrollableButtonContainerLayout;

import app.revanced.integrations.whitelist.Whitelist;
import app.revanced.integrations.whitelist.WhitelistType;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SlimButtonContainer extends SlimMetadataScrollableButtonContainerLayout {

    private ViewGroup container;
    private CopyButton copyButton;
    private CopyWithTimestamp copyWithTimestampButton;
    public static AdButton adBlockButton;
    public static SBWhitelistButton sbWhitelistButton;
    private SBBrowserButton sbBrowserButton;
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
            container = this.findViewById(ReVancedUtils.getIdentifier("button_container_vanced", "id"));
            if (container == null)
                throw new Exception("Unable to initialize the button container because the button_container_vanced couldn't be found");

            copyButton = new CopyButton(context, this);
            copyWithTimestampButton = new CopyWithTimestamp(context, this);
            adBlockButton = new AdButton(context, this);
            sbWhitelistButton = new SBWhitelistButton(context, this);
            sbBrowserButton = new SBBrowserButton(context, this);
            new SponsorBlockVoting(context, this);

            addSharedPrefsChangeListener();
        } catch (Exception ex) {
            LogHelper.printException(SlimButtonContainer.class, "Unable to initialize the button container", ex);
        }
    }

    private void addSharedPrefsChangeListener() {
        listener = (sharedPreferences, key) -> {
            try {
                LogHelper.debug(SlimButtonContainer.class, String.format("SharedPreference changed with key %s", key));
                if ("pref_copy_video_url_button_list".equals(key) && copyButton != null) {
                    copyButton.setVisible(ButtonVisibility.isVisibleInContainer(context, "pref_copy_video_url_button_list"));
                    return;
                }
                if ("pref_copy_video_url_timestamp_button_list".equals(key) && copyWithTimestampButton != null) {
                    copyWithTimestampButton.setVisible(ButtonVisibility.isVisibleInContainer(context, "pref_copy_video_url_timestamp_button_list"));
                    return;
                }
                if (SettingsEnum.SB_ENABLED.getPath().equals(key)) {
                    if (sbWhitelistButton != null) {
                        if (SettingsEnum.SB_ENABLED.getBoolean()) {
                            toggleWhitelistButton();
                        } else {
                            Whitelist.setEnabled(WhitelistType.SPONSORBLOCK, false);
                            sbWhitelistButton.setVisible(false);
                        }
                    }
                    if (sbBrowserButton != null) {
                        if (SettingsEnum.SB_ENABLED.getBoolean()) {
                            toggleBrowserButton();
                        } else {
                            sbBrowserButton.setVisible(false);
                        }
                    }
                }
                if (SettingsEnum.SB_SHOW_BROWSER_BUTTON.getPath().equals(key) && sbBrowserButton != null) {
                    toggleBrowserButton();
                    return;
                }
                WhitelistType whitelistAds = WhitelistType.ADS;
                String adsEnabledPreferenceName = whitelistAds.getPreferenceEnabledName();
                if (adsEnabledPreferenceName.equals(key) && adBlockButton != null) {
                    boolean enabled = SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, adsEnabledPreferenceName, false);
                    Whitelist.setEnabled(whitelistAds, enabled);
                    adBlockButton.setVisible(enabled);
                    return;
                }
                if (WhitelistType.SPONSORBLOCK.getPreferenceEnabledName().equals(key) && sbWhitelistButton != null) {
                    toggleWhitelistButton();
                    return;
                }
            } catch (Exception ex) {
                LogHelper.printException(SlimButtonContainer.class, "Error handling shared preference change", ex);
            }
        };

        context.getSharedPreferences(WhitelistType.ADS.getSharedPreferencesName().getName(), Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(listener);
        context.getSharedPreferences(WhitelistType.SPONSORBLOCK.getSharedPreferencesName().getName(), Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(listener);
    }

    private void toggleWhitelistButton() {
        WhitelistType whitelistSB = WhitelistType.SPONSORBLOCK;
        String sbEnabledPreferenceName = whitelistSB.getPreferenceEnabledName();
        boolean enabled = SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, sbEnabledPreferenceName, false);
        Whitelist.setEnabled(whitelistSB, enabled);
        sbWhitelistButton.setVisible(enabled);
    }

    private void toggleBrowserButton() {
        sbBrowserButton.setVisible(SettingsEnum.SB_SHOW_BROWSER_BUTTON.getBoolean());
    }
}
