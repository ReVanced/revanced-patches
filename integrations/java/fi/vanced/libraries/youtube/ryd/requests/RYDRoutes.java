package fi.vanced.libraries.youtube.ryd.requests;

import static fi.vanced.utils.requests.Route.Method.GET;
import static fi.vanced.utils.requests.Route.Method.POST;

import fi.vanced.utils.requests.Route;

public class RYDRoutes {
    public static final Route SEND_VOTE            = new Route(POST,"interact/vote");
    public static final Route CONFIRM_VOTE         = new Route(POST,"interact/confirmVote");
    public static final Route GET_DISLIKES         = new Route(GET, "votes?videoId={video_id}");
    public static final Route GET_REGISTRATION     = new Route(GET, "puzzle/registration?userId={user_id}");
    public static final Route CONFIRM_REGISTRATION = new Route(POST,"puzzle/registration?userId={user_id}");

    private RYDRoutes() {}
}