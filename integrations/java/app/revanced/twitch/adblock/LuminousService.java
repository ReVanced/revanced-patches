package app.revanced.twitch.adblock;

import app.revanced.twitch.utils.LogHelper;
import app.revanced.twitch.utils.ReVancedUtils;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class LuminousService implements IAdblockService {
    @Override
    public String friendlyName() {
        return ReVancedUtils.getString("revanced_proxy_luminous");
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
            LogHelper.error("Failed to parse rewritten URL");
            return null;
        }

        // Overwrite old request
        return new Request.Builder()
                .get()
                .url(url)
                .build();
    }
}
