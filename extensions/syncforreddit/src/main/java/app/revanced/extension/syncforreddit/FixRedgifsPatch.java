package app.revanced.extension.syncforreddit;


import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.requests.Requester;
import app.revanced.extension.shared.requests.Route;
import com.android.volley.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Pattern;

import static app.revanced.extension.shared.requests.Route.Method.GET;

/**
 * @noinspection unused
 */
public class FixRedgifsPatch {
    public static void fetchVideoUrl(String link, boolean hd, Response.Listener<String> result) {
        Utils.runOnBackgroundThread(() -> {
            String url = getVideoUrl(link, hd);
            if (url == null) return;
            Utils.runOnMainThread(() -> result.onResponse(url));
        });
    }

    private static final Route OEMBED_ROUTE = new Route(GET, "oembed?url={link}");

    private static String getVideoUrl(String link, boolean hd) {
        Logger.printInfo(() -> "Fetching " + link);
        try {
            HttpURLConnection connection = Requester.getConnectionFromRoute("https://api.redgifs.com/v1/", OEMBED_ROUTE, link);
            JSONObject obj = Requester.parseJSONObject(connection);
            String thumbnailUrl = obj.getString("thumbnail_url");

            String id = parseThumbnailUrl(thumbnailUrl);
            if (id == null) return null;

            return reconstructVideoUrl(id, hd);
        } catch (IOException | JSONException e) {
            Logger.printException(() -> "Failed to fetch gif", e);
        }

        return null;
    }


    private static String reconstructVideoUrl(String id, boolean hd) {
        var suffix = hd ? "" : "-mobile";
        return "https://media.redgifs.com/" + id + suffix + ".mp4";
    }

    private final static Pattern REDGIFS_THUMBNAIL_URL = Pattern.compile("https:\\/\\/media\\.redgifs\\.com\\/(\\w+)\\-poster\\.jpg");

    private static String parseThumbnailUrl(String url) {
        var matcher = REDGIFS_THUMBNAIL_URL.matcher(url);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }
}
