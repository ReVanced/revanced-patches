package app.revanced.integrations.adremover.whitelist.requests;

import static app.revanced.integrations.adremover.whitelist.requests.Route.Method.POST;

public class WhitelistRoutes {
    public static final Route GET_CHANNEL_DETAILS = new Route(POST, "player?key={api_key}");

    private WhitelistRoutes() {
    }
}