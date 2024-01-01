package app.revanced.integrations.twitch.adblock;

import okhttp3.Request;

public interface IAdblockService {
    String friendlyName();

    Integer maxAttempts();

    Boolean isAvailable();

    Request rewriteHlsRequest(Request originalRequest);

    static boolean isVod(Request request){
        return request.url().pathSegments().contains("vod");
    }

    static String channelName(Request request) {
        for (String pathSegment : request.url().pathSegments()) {
            if (pathSegment.endsWith(".m3u8")) {
                return pathSegment.replace(".m3u8", "");
            }
        }
        return null;
    }
}
