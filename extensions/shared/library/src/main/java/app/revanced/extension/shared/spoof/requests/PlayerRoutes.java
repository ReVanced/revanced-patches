package app.revanced.extension.shared.spoof.requests;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.requests.Requester;
import app.revanced.extension.shared.requests.Route;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.spoof.ClientType;

final class PlayerRoutes {
    static final Route.CompiledRoute GET_STREAMING_DATA = new Route(
            Route.Method.POST,
            "player" +
                    "?fields=streamingData" +
                    "&alt=proto"
    ).compile();

    private static final String YT_API_URL = "https://youtubei.googleapis.com/youtubei/v1/";

    /**
     * TCP connection and HTTP read timeout
     */
    private static final int CONNECTION_TIMEOUT_MILLISECONDS = 10 * 1000; // 10 Seconds.

    private PlayerRoutes() {
    }

    static String createInnertubeBody(ClientType clientType) {
        JSONObject innerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            // Can override default language only if no login is used.
            // Could use preferred audio for all clients that do not login,
            // but if this is a fall over client it will set the language even though
            // the audio language is not selectable in the UI.
            ClientType userSelectedClient = BaseSettings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get();
            AppLanguage language = userSelectedClient == ClientType.ANDROID_VR_NO_AUTH
                    ? BaseSettings.SPOOF_VIDEO_STREAMS_LANGUAGE.get()
                    : AppLanguage.DEFAULT;

            JSONObject client = new JSONObject();
            client.put("hl", language.getLanguage());
            client.put("clientName", clientType.clientName);
            client.put("clientVersion", clientType.clientVersion);
            client.put("deviceMake", clientType.deviceMake);
            client.put("deviceModel", clientType.deviceModel);
            client.put("osName", clientType.osName);
            client.put("osVersion", clientType.osVersion);
            if (clientType.androidSdkVersion != null) {
                client.put("androidSdkVersion", clientType.androidSdkVersion);
            }
            context.put("client", client);

            innerTubeBody.put("context", context);
            innerTubeBody.put("contentCheckOk", true);
            innerTubeBody.put("racyCheckOk", true);
            innerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to create innerTubeBody", e);
        }

        return innerTubeBody.toString();
    }

    /**
     * @noinspection SameParameterValue
     */
    static HttpURLConnection getPlayerResponseConnectionFromRoute(Route.CompiledRoute route, ClientType clientType) throws IOException {
        var connection = Requester.getConnectionFromCompiledRoute(YT_API_URL, route);

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", clientType.userAgent);
        connection.setRequestProperty("X-YouTube-Client-Version", String.valueOf(clientType.id));

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLISECONDS);
        connection.setReadTimeout(CONNECTION_TIMEOUT_MILLISECONDS);
        return connection;
    }
}