package app.revanced.integrations.whitelist.requests;

import static app.revanced.integrations.videoplayer.VideoInformation.currentVideoId;
import static app.revanced.integrations.utils.ReVancedUtils.runOnMainThread;
import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.content.Context;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.sponsorblock.player.ChannelModel;
import app.revanced.integrations.whitelist.Whitelist;
import app.revanced.integrations.whitelist.WhitelistType;
import app.revanced.integrations.BuildConfig;

public class WhitelistRequester {
    private static final String YT_API_URL = "https://www.youtube.com/youtubei/v1/";

    private WhitelistRequester() {
    }

    public static void addChannelToWhitelist(WhitelistType whitelistType, View view, ImageView buttonIcon, Context context) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(WhitelistRoutes.GET_CHANNEL_DETAILS, BuildConfig.YT_API_KEY);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(2 * 1000);

            String versionName = getVersionName(context);
            String jsonInputString = "{\"context\": {\"client\": { \"clientName\": \"Android\", \"clientVersion\": \"" + versionName + "\" } }, \"videoId\": \"" + currentVideoId + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                JSONObject json = getJSONObject(connection);
                JSONObject videoInfo = json.getJSONObject("videoDetails");
                ChannelModel channelModel = new ChannelModel(videoInfo.getString("author"), videoInfo.getString("channelId"));
                String author = channelModel.getAuthor();
                LogHelper.debug(WhitelistRequester.class, "channelId " + channelModel.getChannelId() + " fetched for author " + author);

                boolean success = Whitelist.addToWhitelist(whitelistType, context, channelModel);
                String whitelistTypeName = whitelistType.getFriendlyName();
                runOnMainThread(() -> {
                    if (success) {
                        buttonIcon.setEnabled(whitelistType != WhitelistType.SPONSORBLOCK);
                        Toast.makeText(context, str("revanced_whitelisting_added", author, whitelistTypeName), Toast.LENGTH_SHORT).show();
                    } else {
                        buttonIcon.setEnabled(whitelistType == WhitelistType.SPONSORBLOCK);
                        Toast.makeText(context, str("revanced_whitelisting_add_failed", author, whitelistTypeName), Toast.LENGTH_SHORT).show();
                    }
                    view.setEnabled(true);
                });
            } else {
                LogHelper.debug(WhitelistRequester.class, "player fetch response was " + responseCode);
                runOnMainThread(() -> {
                    Toast.makeText(context, str("revanced_whitelisting_fetch_failed", responseCode), Toast.LENGTH_SHORT).show();
                    buttonIcon.setEnabled(true);
                    view.setEnabled(true);
                });
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(WhitelistRequester.class, "Failed to fetch channelId", ex);
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

    private static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            return (version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return ("17.24.34");
    }
}