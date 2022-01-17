package fi.vanced.libraries.youtube.whitelisting.requests;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.currentVideoId;
import static fi.vanced.libraries.youtube.ui.AdBlock.TAG;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import fi.vanced.utils.requests.Requester;
import fi.vanced.utils.requests.Route;

public class WhitelistRequester {
    private static final String YT_API_URL = "https://www.youtube.com/youtubei/v1/";
    private static final String YT_API_KEY = "replaceMeWithTheYouTubeAPIKey";

    public static void addChannelToWhitelist(WhitelistType whitelistType, View view, ImageView buttonIcon, Context context) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(WhitelistRoutes.GET_CHANNEL_DETAILS, YT_API_KEY);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(2 * 1000);

            // TODO: Actually fetch the version
            String jsonInputString = "{\"context\": {\"client\": { \"clientName\": \"Android\", \"clientVersion\": \"16.49.37\" } }, \"videoId\": \"" + currentVideoId + "\"}";
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (connection.getResponseCode() == 200) {
                JSONObject json = getJSONObject(connection);
                JSONObject videoInfo = json.getJSONObject("videoDetails");
                ChannelModel channelModel = new ChannelModel(videoInfo.getString("author"), videoInfo.getString("channelId"));
                if (debug) {
                    Log.d(TAG, "channelId " + channelModel.getChannelId() + " fetched for author " + channelModel.getAuthor());
                }

                boolean success = Whitelist.addToWhitelist(whitelistType, context, channelModel);
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
                view.setEnabled(true);
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to fetch channelId", ex);
            view.setEnabled(true);
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