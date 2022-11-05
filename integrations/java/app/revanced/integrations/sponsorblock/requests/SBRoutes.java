package app.revanced.integrations.sponsorblock.requests;

import static app.revanced.integrations.requests.Route.Method.GET;
import static app.revanced.integrations.requests.Route.Method.POST;

import app.revanced.integrations.requests.Route;

public class SBRoutes {
    public static final Route IS_USER_VIP = new Route(GET, "isUserVIP?userID={user_id}");
    public static final Route GET_SEGMENTS = new Route(GET, "skipSegments?videoID={video_id}&categories={categories}");
    public static final Route VIEWED_SEGMENT = new Route(POST, "viewedVideoSponsorTime?UUID={segment_id}");
    public static final Route GET_USER_STATS = new Route(GET, "userInfo?userID={user_id}&values=[\"userName\", \"minutesSaved\", \"segmentCount\", \"viewCount\"]");
    public static final Route CHANGE_USERNAME = new Route(POST, "setUsername?userID={user_id}&username={username}");
    public static final Route SUBMIT_SEGMENTS = new Route(POST, "skipSegments?videoID={video_id}&userID={user_id}&startTime={start_time}&endTime={end_time}&category={category}&videoDuration={duration}");
    public static final Route VOTE_ON_SEGMENT_QUALITY = new Route(POST, "voteOnSponsorTime?UUID={segment_id}&userID={user_id}&type={type}");
    public static final Route VOTE_ON_SEGMENT_CATEGORY = new Route(POST, "voteOnSponsorTime?UUID={segment_id}&userID={user_id}&category={category}");

    private SBRoutes() {
    }
}