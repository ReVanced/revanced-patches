package app.revanced.extension.youtube.patches.announcements.requests;

import app.revanced.extension.youtube.requests.Requester;
import app.revanced.extension.youtube.requests.Route;

import java.io.IOException;
import java.net.HttpURLConnection;

import static app.revanced.extension.youtube.requests.Route.Method.GET;

public class AnnouncementsRoutes {
    private static final String ANNOUNCEMENTS_PROVIDER = "https://api.revanced.app/v2";

    /**
     * 'language' parameter is IETF format (for USA it would be 'en-us').
     */
    public static final Route GET_LATEST_ANNOUNCEMENT = new Route(GET, "/announcements/youtube/latest?language={language}");

    private AnnouncementsRoutes() {
    }

    public static HttpURLConnection getAnnouncementsConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(ANNOUNCEMENTS_PROVIDER, route, params);
    }
}