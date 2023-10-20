package app.revanced.integrations.patches.spoof.requests;

import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.requests.Route;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

final class PlayerRoutes {
    private static final String YT_API_URL = "https://www.youtube.com/youtubei/v1/";
    static final Route.CompiledRoute GET_STORYBOARD_SPEC_RENDERER = new Route(
            Route.Method.POST,
            "player" +
                    "?fields=storyboards.playerStoryboardSpecRenderer," +
                    "storyboards.playerLiveStoryboardSpecRenderer," +
                    "playabilityStatus.status"
    ).compile();

    static final String ANDROID_INNER_TUBE_BODY;
    static final String TV_EMBED_INNER_TUBE_BODY;

    static {
        JSONObject innerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            JSONObject client = new JSONObject();
            client.put("clientName", "ANDROID");
            client.put("clientVersion", "18.37.36");
            client.put("androidSdkVersion", 34);

            context.put("client", client);

            innerTubeBody.put("context", context);
            innerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            LogHelper.printException(() -> "Failed to create innerTubeBody", e);
        }

        ANDROID_INNER_TUBE_BODY = innerTubeBody.toString();

        JSONObject tvEmbedInnerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            JSONObject client = new JSONObject();
            client.put("clientName", "TVHTML5_SIMPLY_EMBEDDED_PLAYER");
            client.put("clientVersion", "2.0");
            client.put("platform", "TV");
            client.put("clientScreen", "EMBED");

            JSONObject thirdParty = new JSONObject();
            thirdParty.put("embedUrl", "https://www.youtube.com/watch?v=%s");

            context.put("thirdParty", thirdParty);
            context.put("client", client);

            tvEmbedInnerTubeBody.put("context", context);
            tvEmbedInnerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            LogHelper.printException(() -> "Failed to create tvEmbedInnerTubeBody", e);
        }

        TV_EMBED_INNER_TUBE_BODY = tvEmbedInnerTubeBody.toString();
    }

    private PlayerRoutes() {
    }

    /** @noinspection SameParameterValue*/
    static HttpURLConnection getPlayerResponseConnectionFromRoute(Route.CompiledRoute route) throws IOException {
        var connection = Requester.getConnectionFromCompiledRoute(YT_API_URL, route);

        connection.setRequestProperty(
                "User-Agent", "com.google.android.youtube/" +
                        ReVancedUtils.getVersionName() +
                        " (Linux; U; Android 12; GB) gzip"
        );
        connection.setRequestProperty("X-Goog-Api-Format-Version", "2");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection;
    }
}