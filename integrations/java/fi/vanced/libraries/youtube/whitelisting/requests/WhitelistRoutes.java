package fi.vanced.libraries.youtube.whitelisting.requests;

import static fi.vanced.utils.requests.Route.Method.GET;

import fi.vanced.utils.requests.Route;

public class WhitelistRoutes {
    public static final Route GET_CHANNEL_DETAILS = new Route(GET, "player?key={api_key");

    private WhitelistRoutes() {}
}