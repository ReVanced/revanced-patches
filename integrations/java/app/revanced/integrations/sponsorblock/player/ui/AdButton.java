package app.revanced.integrations.sponsorblock.player.ui;

import static app.revanced.integrations.videoplayer.VideoInformation.currentVideoId;
import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.videoplayer.VideoInformation;
import app.revanced.integrations.whitelist.Whitelist;
import app.revanced.integrations.whitelist.WhitelistType;
import app.revanced.integrations.whitelist.requests.WhitelistRequester;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class AdButton extends SlimButton {
    public AdButton(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID,
                SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, WhitelistType.ADS.getPreferenceEnabledName(), false));

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(ReVancedUtils.getIdentifier("revanced_yt_ad_button", "drawable"));
        this.button_text.setText(str("action_ads"));
        changeEnabled(Whitelist.shouldShowAds());
    }

    public void changeEnabled(boolean enabled) {
        LogHelper.debug(AdButton.class, "changeEnabled " + enabled);
        this.button_icon.setEnabled(enabled);
    }

    @Override
    public void onClick(View view) {
        this.view.setEnabled(false);
        if (this.button_icon.isEnabled()) {
            removeFromWhitelist();
            return;
        }
        //this.button_icon.setEnabled(!this.button_icon.isEnabled());

        addToWhiteList(this.view, this.button_icon);
    }

    private void removeFromWhitelist() {
        try {
            Whitelist.removeFromWhitelist(WhitelistType.ADS, this.context, VideoInformation.channelName);
            changeEnabled(false);
        } catch (Exception ex) {
            LogHelper.printException(AdButton.class, "Failed to remove from whitelist", ex);
            return;
        }

        this.view.setEnabled(true);
    }

    private void addToWhiteList(View view, ImageView buttonIcon) {
        new Thread(() -> {
            LogHelper.debug(AdButton.class, "Fetching channelId for " + currentVideoId);
            WhitelistRequester.addChannelToWhitelist(WhitelistType.ADS, view, buttonIcon, this.context);
        }).start();
    }
}
