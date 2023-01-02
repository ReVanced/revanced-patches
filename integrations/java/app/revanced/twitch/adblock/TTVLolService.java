package app.revanced.twitch.adblock;

import java.util.ArrayList;
import java.util.Random;

import app.revanced.twitch.utils.LogHelper;
import app.revanced.twitch.utils.ReVancedUtils;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class TTVLolService implements IAdblockService {
    @Override
    public String friendlyName() {
        return ReVancedUtils.getString("revanced_proxy_ttv_lol");
    }

    // TTV.lol is sometimes unstable
    @Override
    public Integer maxAttempts() {
        return 4;
    }

    @Override
    public Boolean isAvailable() {
        return true;
    }

    @Override
    public Request rewriteHlsRequest(Request originalRequest) {

        var type = "vod";
        if (!IAdblockService.isVod(originalRequest))
            type = "playlist";

        var url = HttpUrl.parse("https://api.ttv.lol/" +
                type + "/" +
                IAdblockService.channelName(originalRequest) +
                ".m3u8" + nextQuery()
        );

        if (url == null) {
            LogHelper.error("Failed to parse rewritten URL");
            return null;
        }

        // Overwrite old request
        return new Request.Builder()
                .get()
                .url(url)
                .addHeader("X-Donate-To", "https://ttv.lol/donate")
                .build();
    }

    private String nextQuery() {
        return SAMPLE_QUERY.replace("<SESSION>", generateSessionId());
    }

    private String generateSessionId() {
        final var chars = "abcdef0123456789".toCharArray();

        var sessionId = new ArrayList<Character>();
        for (int i = 0; i < 32; i++)
            sessionId.add(chars[randomSource.nextInt(16)]);

        return sessionId.toString();
    }

    private final Random randomSource = new Random();
    private final String SAMPLE_QUERY = "%3Fallow_source%3Dtrue%26fast_bread%3Dtrue%26allow_audio_only%3Dtrue%26p%3D0%26play_session_id%3D<SESSION>%26player_backend%3Dmediaplayer%26warp%3Dfalse%26force_preroll%3Dfalse%26mobile_cellular%3Dfalse";
}
