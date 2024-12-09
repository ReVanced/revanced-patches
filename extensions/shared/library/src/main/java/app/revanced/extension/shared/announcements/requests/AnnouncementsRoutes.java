package app.revanced.extension.shared.announcements.requests;

import app.revanced.extension.shared.requests.Requester;
import app.revanced.extension.shared.requests.Route;

import java.io.IOException;
import java.net.HttpURLConnection;

import static app.revanced.extension.shared.requests.Route.Method.GET;

public class AnnouncementsRoutes {
    public final Route GET_LATEST_ANNOUNCEMENTS;
    public final Route GET_LATEST_ANNOUNCEMENT_IDS;

    public AnnouncementsRoutes(String tag) {
        this.GET_LATEST_ANNOUNCEMENTS = new Route(GET, "/announcements/latest?tag=" + tag);
        this.GET_LATEST_ANNOUNCEMENT_IDS = new Route(GET, "/announcements/latest/ids?tag=" + tag);
    }

    public HttpURLConnection getAnnouncementsConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute("https://api.revanced.app/v4", route, params);
    }
}
