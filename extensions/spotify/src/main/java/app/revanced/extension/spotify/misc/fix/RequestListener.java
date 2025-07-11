package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.misc.fix.clienttoken.data.v0.ClienttokenHttp.ClientTokenResponse;
import com.google.protobuf.MessageLite;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static app.revanced.extension.spotify.misc.fix.ClientTokenService.serveClientTokenRequest;
import static app.revanced.extension.spotify.misc.fix.Constants.CLIENT_TOKEN_API_PATH;
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;

class RequestListener extends NanoHTTPD {
    RequestListener(int port) {
        super(port);

        try {
            start();
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to start request listener on port " + port, ex);
            throw new RuntimeException(ex);
        }
    }

    @NonNull
    @Override
    public Response serve(@NonNull IHTTPSession session) {
        String uri = session.getUri();
        if (!uri.equals(CLIENT_TOKEN_API_PATH)) return INTERNAL_ERROR_RESPONSE;

        Logger.printInfo(() -> "Serving request for URI: " + uri);

        ClientTokenResponse response = serveClientTokenRequest(getInputStream(session));
        if (response != null) return newResponse(Response.Status.OK, response);

        Logger.printException(() -> "Failed to serve client token request");
        return INTERNAL_ERROR_RESPONSE;
    }

    @NonNull
    private static InputStream newLimitedInputStream(InputStream inputStream, long contentLength) {
        return new FilterInputStream(inputStream) {
            private long remaining = contentLength;

            @Override
            public int read() throws IOException {
                if (remaining <= 0) return -1;
                int result = super.read();
                if (result != -1) remaining--;
                return result;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (remaining <= 0) return -1;
                len = (int) Math.min(len, remaining);
                int result = super.read(b, off, len);
                if (result != -1) remaining -= result;
                return result;
            }
        };
    }

    @NonNull
    private static InputStream getInputStream(@NonNull IHTTPSession session) {
        long requestContentLength = Long.parseLong(Objects.requireNonNull(session.getHeaders().get("content-length")));
        return newLimitedInputStream(session.getInputStream(), requestContentLength);
    }

    private static final Response INTERNAL_ERROR_RESPONSE = newResponse(INTERNAL_ERROR);

    @SuppressWarnings("SameParameterValue")
    @NonNull
    private static Response newResponse(Response.Status status) {
        return newResponse(status, null);
    }

    @NonNull
    private static Response newResponse(Response.IStatus status, MessageLite messageLite) {
        if (messageLite == null) {
            return newFixedLengthResponse(status, "application/x-protobuf", null);
        }

        byte[] messageBytes = messageLite.toByteArray();
        InputStream stream = new ByteArrayInputStream(messageBytes);
        return newFixedLengthResponse(status, "application/x-protobuf", stream, messageBytes.length);
    }
}
