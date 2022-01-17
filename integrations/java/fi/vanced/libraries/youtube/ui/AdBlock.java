package fi.vanced.libraries.youtube.ui;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.ads.VideoAds.getShouldShowAds;
import static fi.vanced.libraries.youtube.player.VideoInformation.currentVideoId;
import static pl.jakubweg.StringRef.str;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import fi.vanced.libraries.youtube.ads.AdsRequester;
import fi.vanced.libraries.youtube.ads.VideoAds;
import fi.vanced.libraries.youtube.player.VideoInformation;
import fi.vanced.utils.SharedPrefUtils;
import fi.vanced.utils.VancedUtils;

public class AdBlock extends SlimButton {
    public static final String TAG = "VI - AdBlock - Button";

    public AdBlock(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID, SharedPrefUtils.getBoolean(context, "youtube", "vanced_videoadwhitelisting_enabled", false));

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(VancedUtils.getIdentifier("vanced_yt_ad_button", "drawable"));
        this.button_text.setText(str("action_ads"));
        changeEnabled(getShouldShowAds());
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

        addToWhiteList();
    }

    private void removeFromWhitelist() {
        try {
            VideoAds.removeFromWhitelist(this.context, VideoInformation.channelName);
            this.button_icon.setEnabled(false);
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to remove from whitelist", ex);
            return;
        }

        this.view.setEnabled(true);
    }

    private void addToWhiteList() {
        new Thread(() -> {
            if (debug) {
                Log.d(TAG, "Fetching channelId for " + currentVideoId);
            }
            AdsRequester.retrieveChannelDetails(this.view, this.button_icon, this.context);
        }).start();
    }
}
