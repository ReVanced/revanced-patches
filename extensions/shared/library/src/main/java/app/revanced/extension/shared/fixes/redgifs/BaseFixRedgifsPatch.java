package app.revanced.extension.shared.fixes.redgifs;

import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.requests.Requester;
import app.revanced.extension.shared.requests.Route;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static app.revanced.extension.shared.requests.Route.Method.GET;

public abstract class BaseFixRedgifsPatch {
    protected static BaseFixRedgifsPatch INSTANCE;

    private static final String MEDIA_HOST = "media.redgifs.com";
    private static final String API_V1_URL = "https://api.redgifs.com/v1/";
    private static final Route OEMBED_ROUTE = new Route(GET, "oembed?url={link}");

    public @Nullable String getVideoUrl(String link, boolean hd) {
        Logger.printInfo(() -> "Fetching: " + link);
        try {
            HttpURLConnection connection = Requester.getConnectionFromRoute(API_V1_URL, OEMBED_ROUTE, link);
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

    private String reconstructVideoUrl(String id, boolean hd) {
        var suffix = hd ? "" : "-mobile";
        return "https://" + MEDIA_HOST + "/" + id + suffix + ".mp4";
    }

    private @Nullable String parseThumbnailUrl(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);

        String host = url.getHost();
        if (!host.equalsIgnoreCase(MEDIA_HOST)) return null;

        String path = url.getPath();
        int idx = path.indexOf("-poster.jpg");
        if (idx < 0) return null;

        return path.substring(1, idx);
    }
}
