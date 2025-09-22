package app.revanced.extension.instagram.feed;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class LimitFeedToFollowedProfiles {

    /**
     * Injection point.
     */
    public static Map<String, String> setFollowingHeader(Map<String, String> requestHeaderMap) {
        String paginationHeaderName = "pagination_source";

        // Patch the header only if it's trying to fetch the default feed
        String currentHeader = requestHeaderMap.get(paginationHeaderName);
        if (currentHeader != null && !currentHeader.equals("feed_recs")) {
            return requestHeaderMap;
        }

        // Create new map as original is unmodifiable.
        Map<String, String> patchedRequestHeaderMap = new HashMap<>(requestHeaderMap);
        patchedRequestHeaderMap.put(paginationHeaderName, "following");
        return patchedRequestHeaderMap;
    }
}
