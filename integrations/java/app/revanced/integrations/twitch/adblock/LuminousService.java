package app.revanced.integrations.twitch.adblock;

import app.revanced.integrations.shared.Logger;
import okhttp3.HttpUrl;
import okhttp3.Request;

import static app.revanced.integrations.shared.StringRef.str;

public class LuminousService implements IAdblockService {
    @Override
    public String friendlyName() {
        return str("revanced_proxy_luminous");
    }

    @Override
    public Integer maxAttempts() {
        return 2;
    }

    @Override
    public Boolean isAvailable() {
        return true;
    }

    @Override
    public Request rewriteHlsRequest(Request originalRequest) {
        var type = IAdblockService.isVod(originalRequest) ? "vod" : "playlist";
        var url = HttpUrl.parse("https://eu.luminous.dev/" +
                type +
                "/" +
                IAdblockService.channelName(originalRequest) +
                ".m3u8" +
                "%3Fallow_source%3Dtrue%26allow_audio_only%3Dtrue%26fast_bread%3Dtrue"
        );

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
}
