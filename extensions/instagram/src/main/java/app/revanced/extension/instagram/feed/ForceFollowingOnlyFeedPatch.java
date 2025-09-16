package app.revanced.extension.instagram.feed;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ForceFollowingOnlyFeedPatch {

    /**
     * Injection point.
     */
    public static Map<String, String> setFollowingHeader(Map<String, String> requestHeaderMap) {
        Map<String, String> patchedRequestHeaderMap = new HashMap<>(requestHeaderMap);
        patchedRequestHeaderMap.put("pagination_source", "following");
        return patchedRequestHeaderMap;
    }
}
