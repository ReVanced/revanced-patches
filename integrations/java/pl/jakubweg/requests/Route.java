package pl.jakubweg.requests;

import pl.jakubweg.SponsorBlockUtils;

import static pl.jakubweg.requests.Route.Method.*;

public class Route {
    public static final Route GET_SEGMENTS =             new Route(GET,   "skipSegments?videoID={video_id}&categories={categories}");
    public static final Route VIEWED_SEGMENT =           new Route(POST,  "viewedVideoSponsorTime?UUID={segment_id}");
    public static final Route GET_USER_STATS =           new Route(GET,   "userInfo?userID={user_id}&values=[\"userName\", \"minutesSaved\", \"segmentCount\", \"viewCount\"]");
    public static final Route CHANGE_USERNAME =          new Route(POST,  "setUsername?userID={user_id}&username={username}");
    public static final Route SUBMIT_SEGMENTS =          new Route(POST,  "skipSegments?videoID={video_id}&userID={user_id}&startTime={start_time}&endTime={end_time}&category={category}");
    public static final Route VOTE_ON_SEGMENT_QUALITY =  new Route(POST,  "voteOnSponsorTime?UUID={segment_id}&userID={user_id}&type={type}");
    public static final Route VOTE_ON_SEGMENT_CATEGORY = new Route(POST,  "voteOnSponsorTime?UUID={segment_id}&userID={user_id}&category={category}");

    private final String route;
    private final Method method;
    private final int paramCount;

    private Route(Method method, String route) {
        this.method = method;
        this.route = route;
        this.paramCount = SponsorBlockUtils.countMatches(route, '{');

        if (paramCount != SponsorBlockUtils.countMatches(route, '}'))
            throw new IllegalArgumentException("Not enough parameters");
    }

    public Method getMethod() {
        return method;
    }

    public CompiledRoute compile(String... params) {
        if (params.length != paramCount)
            throw new IllegalArgumentException("Error compiling route [" + route + "], incorrect amount of parameters provided. " +
                    "Expected: " + paramCount + ", provided: " + params.length);

        StringBuilder compiledRoute = new StringBuilder(route);
        for (int i = 0; i < paramCount; i++) {
            int paramStart = compiledRoute.indexOf("{");
            int paramEnd = compiledRoute.indexOf("}");
            compiledRoute.replace(paramStart, paramEnd + 1, params[i]);
        }
        return new CompiledRoute(this, compiledRoute.toString());
    }

    public static class CompiledRoute {
        private final Route baseRoute;
        private final String compiledRoute;

        private CompiledRoute(Route baseRoute, String compiledRoute) {
            this.baseRoute = baseRoute;
            this.compiledRoute = compiledRoute;
        }

        public Route getBaseRoute() {
            return baseRoute;
        }

        public String getCompiledRoute() {
            return compiledRoute;
        }

        public Method getMethod() {
            return baseRoute.method;
        }
    }

    public enum Method {
        GET,
        POST
    }
}