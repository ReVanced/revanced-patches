package fi.vanced.libraries.youtube.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.ads.VideoAds.getShouldShowAds;
import static fi.vanced.libraries.youtube.player.VideoInformation.currentVideoId;
import static fi.vanced.utils.VancedUtils.parseJson;
import static pl.jakubweg.StringRef.str;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import fi.vanced.libraries.youtube.ads.VideoAds;
import fi.vanced.libraries.youtube.player.ChannelModel;
import fi.vanced.libraries.youtube.player.VideoInformation;
import fi.vanced.utils.SharedPrefUtils;
import fi.vanced.utils.VancedUtils;

public class AdBlock extends SlimButton {
    private static final String TAG = "VI - AdBlock - Button";
    private static final String YT_API_URL = "https://www.youtube.com/youtubei/v1";
    private static final String YT_API_KEY = "replaceMeWithTheYouTubeAPIKey";

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

        addToWhiteList(this.view, this.button_icon);
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

    private void addToWhiteList(View view, ImageView buttonIcon) {
        new Thread(() -> {
            try {
                if (debug) {
                    Log.d(TAG, "Fetching channelId for " + currentVideoId);
                }
                HttpURLConnection connection = (HttpURLConnection) new URL(YT_API_URL + "/player?key=" + YT_API_KEY).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(2 * 1000);

                // TODO: Actually fetch the version
                String jsonInputString = "{\"context\": {\"client\": { \"clientName\": \"Android\", \"clientVersion\": \"16.49.37\" } }, \"videoId\": \"" + currentVideoId + "\"}";
                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                if (connection.getResponseCode() == 200) {
                    JSONObject json = new JSONObject(parseJson(connection));
                    JSONObject videoInfo = json.getJSONObject("videoDetails");
                    ChannelModel channelModel = new ChannelModel(videoInfo.getString("author"), videoInfo.getString("channelId"));
                    if (debug) {
                        Log.d(TAG, "channelId " + channelModel.getChannelId() + " fetched for author " + channelModel.getAuthor());
                    }

                    boolean success = VideoAds.addToWhitelist(this.context, channelModel.getAuthor(), channelModel.getChannelId());
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (success) {
                            buttonIcon.setEnabled(true);
                            Toast.makeText(context, "Channel " + channelModel.getAuthor() + " whitelisted", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            buttonIcon.setEnabled(false);
                            Toast.makeText(context, "Channel " + channelModel.getAuthor() + " failed to whitelist", Toast.LENGTH_SHORT).show();
                        }

                        view.setEnabled(true);
                    });
                }
                else {
                    if (debug) {
                        Log.d(TAG, "player fetch response was " + connection.getResponseCode());
                    }

                    buttonIcon.setEnabled(false);
                    this.view.setEnabled(true);
                }
            }
            catch (Exception ex) {
                Log.e(TAG, "Failed to fetch channelId", ex);
                this.view.setEnabled(true);
                return;
            }
        }).start();
    }
}
