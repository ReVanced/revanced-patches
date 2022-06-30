package app.revanced.integrations.sponsorblock.player.ui;

import static app.revanced.integrations.sponsorblock.player.VideoInformation.currentVideoId;
import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.sponsorblock.player.VideoInformation;
import app.revanced.integrations.adremover.whitelist.Whitelist;
import app.revanced.integrations.adremover.whitelist.WhitelistType;
import app.revanced.integrations.adremover.whitelist.requests.WhitelistRequester;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;

public class SBWhitelistButton extends SlimButton {
    public SBWhitelistButton(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID,
                SponsorBlockUtils.isSBButtonEnabled(context, WhitelistType.SPONSORBLOCK.getPreferenceEnabledName()));

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(ReVancedUtils.getIdentifier("revanced_yt_sb_button", "drawable"));
        this.button_text.setText(str("action_segments"));
        changeEnabled(Whitelist.isChannelSBWhitelisted());
    }

    public void changeEnabled(boolean enabled) {
        LogHelper.debug("SBWhiteListButton", "changeEnabled " + enabled);
        this.button_icon.setEnabled(!enabled); // enabled == true -> strikethrough (no segments), enabled == false -> clear (segments)
    }

    @Override
    public void onClick(View view) {
        this.view.setEnabled(false);
        if (Whitelist.isChannelSBWhitelisted()) {
            removeFromWhitelist();
            return;
        }
        //this.button_icon.setEnabled(!this.button_icon.isEnabled());

        addToWhiteList(this.view, this.button_icon);
    }

    private void removeFromWhitelist() {
        try {
            Whitelist.removeFromWhitelist(WhitelistType.SPONSORBLOCK, this.context, VideoInformation.channelName);
            changeEnabled(false);
        } catch (Exception ex) {
            LogHelper.printException("SBWhiteListButton", "Failed to remove from whitelist", ex);
            return;
        }

        this.view.setEnabled(true);
    }

    private void addToWhiteList(View view, ImageView buttonIcon) {
        new Thread(() -> {
            LogHelper.debug("SBWhiteListButton", "Fetching channelId for " + currentVideoId);
            WhitelistRequester.addChannelToWhitelist(WhitelistType.SPONSORBLOCK, view, buttonIcon, this.context);
        }).start();
    }
}
