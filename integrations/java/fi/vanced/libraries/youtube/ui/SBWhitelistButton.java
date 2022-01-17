package fi.vanced.libraries.youtube.ui;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.currentVideoId;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import fi.vanced.libraries.youtube.player.VideoInformation;
import fi.vanced.libraries.youtube.whitelisting.Whitelist;
import fi.vanced.libraries.youtube.whitelisting.WhitelistType;
import fi.vanced.libraries.youtube.whitelisting.requests.WhitelistRequester;
import fi.vanced.utils.SharedPrefUtils;
import fi.vanced.utils.VancedUtils;
import pl.jakubweg.SponsorBlockSettings;

public class SBWhitelistButton extends SlimButton {
    public static final String TAG = "VI - SBWhitelistButton";

    public SBWhitelistButton(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID,
                SharedPrefUtils.getBoolean(context, SponsorBlockSettings.PREFERENCES_NAME, WhitelistType.SPONSORBLOCK.getPreferenceEnabledName(), false));

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(VancedUtils.getIdentifier("vanced_sb_logo", "drawable"));
        this.button_text.setText("SB");
        changeEnabled(Whitelist.shouldShowSegments());
    }

    public void changeEnabled(boolean enabled) {
        if (debug) {
            Log.d(TAG, "changeEnabled " + enabled);
        }
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
            Whitelist.removeFromWhitelist(WhitelistType.SPONSORBLOCK, this.context, VideoInformation.channelName);
            this.button_icon.setEnabled(false);
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to remove from whitelist", ex);
            return;
        }

        this.view.setEnabled(true);
    }

    private void addToWhiteList(View view, ImageView buttonIcon) {
        new Thread(() -> {
            if (debug) {
                Log.d(TAG, "Fetching channelId for " + currentVideoId);
            }
            WhitelistRequester.addChannelToWhitelist(WhitelistType.SPONSORBLOCK, view, buttonIcon, this.context);
        }).start();
    }
}
