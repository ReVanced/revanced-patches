package app.revanced.integrations.youtube.patches.announcements.requests;

import app.revanced.integrations.youtube.requests.Requester;
import app.revanced.integrations.youtube.requests.Route;

import java.io.IOException;
import java.net.HttpURLConnection;

import static app.revanced.integrations.youtube.requests.Route.Method.GET;

public class AnnouncementsRoutes {
    private static final String ANNOUNCEMENTS_PROVIDER = "https://api.revanced.app/v2";


    public static final Route GET_LATEST_ANNOUNCEMENT = new Route(GET, "/announcements/youtube/latest?consumer={consumer}");

    private AnnouncementsRoutes() {
    }

    public static HttpURLConnection getAnnouncementsConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(ANNOUNCEMENTS_PROVIDER, route, params);
    }
}