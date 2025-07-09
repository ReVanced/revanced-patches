package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.clienttoken.data.v0.ClienttokenHttp;
import com.google.protobuf.MessageLite;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

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
    public Response serve(@NonNull IHTTPSession request) {
        String uri = request.getUri();
        Logger.printInfo(() -> "Serving request for URI: " + uri);

        if (uri.equals(ClientTokenFetcher.CLIENT_TOKEN_PATH)) {
            InputStream requestBodyInputStream = getRequestBodyInputStream(request);

            ClienttokenHttp.ClientTokenRequest clientTokenRequest;
            try {
                clientTokenRequest = ClienttokenHttp.ClientTokenRequest.parseFrom(requestBodyInputStream);
            } catch (IOException ex) {
                Logger.printException(() -> "Failed to parse client token request", ex);
                return newResponse(INTERNAL_ERROR);
            }

            try {
                ClienttokenHttp.ClientTokenResponse clientTokenResponse =
                        ClientTokenFetcher.fetchClientToken(clientTokenRequest);

                if (clientTokenResponse == null) {
                    return newResponse(INTERNAL_ERROR);
                }

                ClienttokenHttp.ClientTokenResponseType responseGranted =
                        ClienttokenHttp.ClientTokenResponseType.RESPONSE_GRANTED_TOKEN_RESPONSE;
                if (clientTokenResponse.getResponseType() == responseGranted) {
                    Logger.printInfo(() -> "Fetched iOS client token: " +
                            clientTokenResponse.getGrantedToken().getToken());
                }

                return newResponse(Response.Status.OK, clientTokenResponse);
            } catch (IOException ex) {
                Logger.printException(() -> "Failed to get client token response", ex);
            }
        }

        return newResponse(INTERNAL_ERROR);
    }

    @NonNull
    private static InputStream limitedInputStream(InputStream inputStream, long contentLength) {
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
    private static InputStream getRequestBodyInputStream(@NonNull IHTTPSession request) {
        long requestContentLength =
                Long.parseLong(Objects.requireNonNull(request.getHeaders().get("content-length")));
        return limitedInputStream(request.getInputStream(), requestContentLength);
    }

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
