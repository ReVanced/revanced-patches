package fi.vanced.libraries.youtube.ui;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.currentVideoId;
import static pl.jakubweg.StringRef.str;

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

public class AdButton extends SlimButton {
    public static final String TAG = "VI - AdButton - Button";

    public AdButton(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID,
                SharedPrefUtils.getBoolean(context, WhitelistType.ADS.getSharedPreferencesName(), WhitelistType.ADS.getPreferenceEnabledName(), false));

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(VancedUtils.getIdentifier("vanced_yt_ad_button", "drawable"));
        this.button_text.setText(str("action_ads"));
        changeEnabled(Whitelist.shouldShowAds());
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
            Whitelist.removeFromWhitelist(WhitelistType.ADS, this.context, VideoInformation.channelName);
            changeEnabled(false);
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
            WhitelistRequester.addChannelToWhitelist(WhitelistType.ADS, view, buttonIcon, this.context);
        }).start();
    }
}
