package app.revanced.extension.youtube.returnyoutubedislike.requests;

import static app.revanced.extension.shared.requests.Route.Method.GET;
import static app.revanced.extension.shared.requests.Route.Method.POST;

import java.io.IOException;
import java.net.HttpURLConnection;

import app.revanced.extension.shared.requests.Requester;
import app.revanced.extension.shared.requests.Route;

class ReturnYouTubeDislikeRoutes {
    static final String RYD_API_URL = "https://returnyoutubedislikeapi.com/";

    static final Route SEND_VOTE = new Route(POST, "interact/vote");
    static final Route CONFIRM_VOTE = new Route(POST, "interact/confirmVote");
    static final Route GET_DISLIKES = new Route(GET, "votes?videoId={video_id}");
    static final Route GET_REGISTRATION = new Route(GET, "puzzle/registration?userId={user_id}");
    static final Route CONFIRM_REGISTRATION = new Route(POST, "puzzle/registration?userId={user_id}");

    private ReturnYouTubeDislikeRoutes() {
    }

    static HttpURLConnection getRYDConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(RYD_API_URL, route, params);
    }

}