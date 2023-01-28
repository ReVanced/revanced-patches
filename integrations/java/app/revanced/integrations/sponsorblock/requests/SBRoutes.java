package app.revanced.integrations.sponsorblock.requests;

import static app.revanced.integrations.requests.Route.Method.GET;
import static app.revanced.integrations.requests.Route.Method.POST;

import app.revanced.integrations.requests.Route;

class SBRoutes {
    static final Route IS_USER_VIP = new Route(GET, "/api/isUserVIP?userID={user_id}");
    static final Route GET_SEGMENTS = new Route(GET, "/api/skipSegments?videoID={video_id}&categories={categories}");
    static final Route VIEWED_SEGMENT = new Route(POST, "/api/viewedVideoSponsorTime?UUID={segment_id}");
    static final Route GET_USER_STATS = new Route(GET, "/api/userInfo?userID={user_id}&values=[\"userName\", \"minutesSaved\", \"segmentCount\", \"viewCount\"]");
    static final Route CHANGE_USERNAME = new Route(POST, "/api/setUsername?userID={user_id}&username={username}");
    static final Route SUBMIT_SEGMENTS = new Route(POST, "/api/skipSegments?videoID={video_id}&userID={user_id}&startTime={start_time}&endTime={end_time}&category={category}&videoDuration={duration}");
    static final Route VOTE_ON_SEGMENT_QUALITY = new Route(POST, "/api/voteOnSponsorTime?UUID={segment_id}&userID={user_id}&type={type}");
    static final Route VOTE_ON_SEGMENT_CATEGORY = new Route(POST, "/api/voteOnSponsorTime?UUID={segment_id}&userID={user_id}&category={category}");

    private SBRoutes() {
    }
}