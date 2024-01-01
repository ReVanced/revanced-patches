package app.revanced.integrations.twitch.adblock;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.twitch.api.RetrofitClient;
import okhttp3.HttpUrl;
import okhttp3.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static app.revanced.integrations.shared.StringRef.str;

public class PurpleAdblockService implements IAdblockService {
    private final Map<String, Boolean> tunnels = new HashMap<>() {{
        put("https://eu1.jupter.ga", false);
        put("https://eu2.jupter.ga", false);
    }};

    @Override
    public String friendlyName() {
        return str("revanced_proxy_purpleadblock");
    }

    @Override
    public Integer maxAttempts() {
        return 3;
    }

    @Override
    public Boolean isAvailable() {
        for (String tunnel : tunnels.keySet()) {
            var success = true;

            try {
                var response = RetrofitClient.getInstance().getPurpleAdblockApi().ping(tunnel).execute();
                if (!response.isSuccessful()) {
                    Logger.printException(() ->
                            "PurpleAdBlock tunnel $tunnel returned an error: HTTP code " + response.code()
                    );
                    Logger.printDebug(response::message);

                    try (var errorBody = response.errorBody()) {
                        if (errorBody != null) {
                            Logger.printDebug(() -> {
                                try {
                                    return errorBody.string();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }

                    success = false;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "PurpleAdBlock tunnel $tunnel is unavailable", ex);
                success = false;
            }

            // Cache availability data
            tunnels.put(tunnel, success);

            if (success)
                return true;
        }

        return false;
    }

    @Override
    public Request rewriteHlsRequest(Request originalRequest) {
        for (Map.Entry<String, Boolean> entry : tunnels.entrySet()) {
            if (!entry.getValue()) continue;

            var server = entry.getKey();

            // Compose new URL
            var url = HttpUrl.parse(server + "/channel/" + IAdblockService.channelName(originalRequest));
            if (url == null) {
                Logger.printException(() -> "Failed to parse rewritten URL");
                return null;
            }

            // Overwrite old request
            return new Request.Builder()
                    .get()
                    .url(url)
                    .build();
        }

        Logger.printException(() -> "No tunnels are available");
        return null;
    }
}
