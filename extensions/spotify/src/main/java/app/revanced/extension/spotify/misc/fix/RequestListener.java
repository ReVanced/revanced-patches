package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import fi.iki.elonen.NanoHTTPD;

import java.io.*;
import java.util.*;

import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;

class RequestListener extends NanoHTTPD {
    private final SpoofClientService service;

    private final Set<Map.Entry<String, RouteHandler>> routeHandlers;

    RequestListener(int port) {
        super(port);

        service = new SpoofClientService(port);
        routeHandlers = new HashMap<String, RouteHandler>() {
            {
                put("/ap", service::serveApResolveRequest);
                put("/v1/clienttoken", service::serveClientTokenRequest);
                put("/playplay", service::servePlayPlayRequest);
            }
        }.entrySet();

        try {
            start();
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to start request listener on port " + port, ex);
            throw new RuntimeException(ex);
        }
    }

    interface RouteHandler {
        @NonNull
        Response serve(@NonNull IHTTPSession session);
    }


    @NonNull
    @Override
    public Response serve(@NonNull IHTTPSession session) {
        String uri = session.getUri();
        Logger.printInfo(() -> "Serving request for URI: " + uri);

        for (Map.Entry<String, RouteHandler> entry : routeHandlers) {
            String pathPrefix = entry.getKey();
            if (!uri.startsWith(pathPrefix)) continue;

            RouteHandler handler = entry.getValue();
            return handler.serve(session);
        }

        // All other requests should only be spclient requests.
        return service.serveSpClientRequest(session);
    }

    static final Response INTERNAL_ERROR_RESPONSE = newFixedLengthResponse(INTERNAL_ERROR, "application/x-protobuf", null);
}
