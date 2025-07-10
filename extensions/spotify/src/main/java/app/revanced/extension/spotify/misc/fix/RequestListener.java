package app.revanced.extension.spotify.misc.fix;

import android.graphics.MeshSpecification;
import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.clienttoken.data.v0.ClienttokenHttp.ClientTokenRequest;
import app.revanced.extension.spotify.clienttoken.data.v0.ClienttokenHttp.ClientTokenResponse;
import com.google.protobuf.MessageLite;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import static app.revanced.extension.spotify.misc.fix.ClientTokenService.getClientTokenResponse;
import static app.revanced.extension.spotify.misc.fix.Constants.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;

class RequestListener extends NanoHTTPD {
    private static final String CLIENT_TOKEN_API_PATH = "/v1/clienttoken";
    private static final String CLIENT_TOKEN_API_URL = "https://clienttoken.spotify.com" + CLIENT_TOKEN_API_PATH;

    private static final String IOS_USER_AGENT;

    static {
        String clientVersion = getClientVersion();
        int commitHashIndex = clientVersion.lastIndexOf(".");
        String version = clientVersion.substring(
                clientVersion.indexOf("-") + 1,
                clientVersion.lastIndexOf(".", commitHashIndex - 1)
        );

        IOS_USER_AGENT = "Spotify/" + version + " iOS/" + getSystemVersion() + " (" + getHardwareMachine() + ")";
    }

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
        Logger.printInfo(() -> "Serving request for URI: " + uri);
        if (!uri.equals(CLIENT_TOKEN_API_PATH)) {
            return INTERNAL_ERROR_RESPONSE;
        }

        ClientTokenRequest clientTokenRequest;
        try (InputStream inputStream = getInputStream(session)) {
            clientTokenRequest = ClientTokenRequest.parseFrom(inputStream);
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to parse client token request from input stream", ex);
            return INTERNAL_ERROR_RESPONSE;
        }

        ClientTokenResponse response = getClientTokenResponse(clientTokenRequest, RequestListener::requestClientToken);
        if (response == null) {
            Logger.printException(() -> "Failed to get client token response");
            return INTERNAL_ERROR_RESPONSE;
        }

        return newResponse(Response.Status.OK, response);
    }

    @NonNull
    private static ClientTokenResponse requestClientToken(@NonNull ClientTokenRequest request) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(CLIENT_TOKEN_API_URL).openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/x-protobuf");
        urlConnection.setRequestProperty("Accept", "application/x-protobuf");
        urlConnection.setRequestProperty("User-Agent", IOS_USER_AGENT);

        byte[] requestArray = request.toByteArray();
        urlConnection.setFixedLengthStreamingMode(requestArray.length);
        urlConnection.getOutputStream().write(requestArray);

        try (InputStream inputStream = urlConnection.getInputStream()) {
            return ClientTokenResponse.parseFrom(inputStream);
        }
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
