package fi.vanced.libraries.youtube.whitelisting.requests;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.currentVideoId;
import static fi.vanced.libraries.youtube.ui.AdButton.TAG;
import static fi.vanced.utils.VancedUtils.runOnMainThread;
import static pl.jakubweg.StringRef.str;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import fi.vanced.libraries.youtube.player.ChannelModel;
import fi.vanced.libraries.youtube.whitelisting.Whitelist;
import fi.vanced.libraries.youtube.whitelisting.WhitelistType;
import fi.vanced.utils.VancedUtils;
import fi.vanced.utils.requests.Requester;
import fi.vanced.utils.requests.Route;
import vanced.integrations.BuildConfig;

public class WhitelistRequester {
    private static final String YT_API_URL = "https://www.youtube.com/youtubei/v1/";

    private WhitelistRequester() {}

    public static void addChannelToWhitelist(WhitelistType whitelistType, View view, ImageView buttonIcon, Context context) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(WhitelistRoutes.GET_CHANNEL_DETAILS, BuildConfig.YT_API_KEY);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(2 * 1000);

            String versionName = VancedUtils.getVersionName(context);
            String jsonInputString = "{\"context\": {\"client\": { \"clientName\": \"Android\", \"clientVersion\": \"" + versionName + "\" } }, \"videoId\": \"" + currentVideoId + "\"}";
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                JSONObject json = getJSONObject(connection);
                JSONObject videoInfo = json.getJSONObject("videoDetails");
                ChannelModel channelModel = new ChannelModel(videoInfo.getString("author"), videoInfo.getString("channelId"));
                String author = channelModel.getAuthor();
                if (debug) {
                    Log.d(TAG, "channelId " + channelModel.getChannelId() + " fetched for author " + author);
                }

                boolean success = Whitelist.addToWhitelist(whitelistType, context, channelModel);
                String whitelistTypeName = whitelistType.getFriendlyName();
                runOnMainThread(() -> {
                    if (success) {
                        buttonIcon.setEnabled(whitelistType != WhitelistType.SPONSORBLOCK);
                        Toast.makeText(context, str("vanced_whitelisting_added", author, whitelistTypeName), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        buttonIcon.setEnabled(whitelistType == WhitelistType.SPONSORBLOCK);
                        Toast.makeText(context, str("vanced_whitelisting_add_failed", author, whitelistTypeName), Toast.LENGTH_SHORT).show();
                    }
                    view.setEnabled(true);
                });
            }
            else {
                if (debug) {
                    Log.d(TAG, "player fetch response was " + responseCode);
                }
                runOnMainThread(() -> {
                    Toast.makeText(context, str("vanced_whitelisting_fetch_failed", responseCode), Toast.LENGTH_SHORT).show();
                    buttonIcon.setEnabled(true);
                    view.setEnabled(true);
                });
            }
            connection.disconnect();
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to fetch channelId", ex);
            runOnMainThread(() -> view.setEnabled(true));
        }
    }

    // helpers

    private static HttpURLConnection getConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(YT_API_URL, route, params);
    }

    private static JSONObject getJSONObject(HttpURLConnection connection) throws Exception {
        return Requester.getJSONObject(connection);
    }
}