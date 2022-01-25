package fi.vanced.utils.requests;

import fi.vanced.utils.VancedUtils;

public class Route {
    private final String route;
    private final Route.Method method;
    private final int paramCount;

    public Route(Route.Method method, String route) {
        this.method = method;
        this.route = route;
        this.paramCount = VancedUtils.countMatches(route, '{');

        if (paramCount != VancedUtils.countMatches(route, '}'))
            throw new IllegalArgumentException("Not enough parameters");
    }

    public Route.Method getMethod() {
        return method;
    }

    public Route.CompiledRoute compile(String... params) {
        if (params.length != paramCount)
            throw new IllegalArgumentException("Error compiling route [" + route + "], incorrect amount of parameters provided. " +
                    "Expected: " + paramCount + ", provided: " + params.length);

        StringBuilder compiledRoute = new StringBuilder(route);
        for (int i = 0; i < paramCount; i++) {
            int paramStart = compiledRoute.indexOf("{");
            int paramEnd = compiledRoute.indexOf("}");
            compiledRoute.replace(paramStart, paramEnd + 1, params[i]);
        }
        return new Route.CompiledRoute(this, compiledRoute.toString());
    }

    public static class CompiledRoute {
        private final Route baseRoute;
        private final String compiledRoute;

        private CompiledRoute(Route baseRoute, String compiledRoute) {
            this.baseRoute = baseRoute;
            this.compiledRoute = compiledRoute;
        }

        public String getCompiledRoute() {
            return compiledRoute;
        }

        public Route.Method getMethod() {
            return baseRoute.method;
        }
    }

    public enum Method {
        GET,
        POST
    }
}