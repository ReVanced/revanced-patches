package app.revanced.extension.youtube.patches.announcements.requests;

import app.revanced.extension.shared.requests.Requester;
import app.revanced.extension.shared.requests.Route;

import java.io.IOException;
import java.net.HttpURLConnection;

import static app.revanced.extension.shared.requests.Route.Method.GET;

public class AnnouncementsRoutes {
    private static final String ANNOUNCEMENTS_PROVIDER = "https://api.revanced.app/v4";
    public static final Route GET_LATEST_ANNOUNCEMENT_IDS = new Route(GET, "/announcements/latest/id?tag=\uD83C\uDF9E\uFE0F%20YouTube");
    public static final Route GET_LATEST_ANNOUNCEMENTS = new Route(GET, "/announcements/latest?tag=\uD83C\uDF9E\uFE0F%20YouTube");

    private AnnouncementsRoutes() {
    }

    public static HttpURLConnection getAnnouncementsConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(ANNOUNCEMENTS_PROVIDER, route, params);
    }
}