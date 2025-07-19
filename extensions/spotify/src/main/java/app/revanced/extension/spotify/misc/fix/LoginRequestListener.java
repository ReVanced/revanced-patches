package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.spotify.login5v3.Login5.*;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static app.revanced.extension.spotify.misc.fix.Session.FAILED_TO_RENEW_SESSION;
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;

class LoginRequestListener extends NanoHTTPD {
    LoginRequestListener(int port) {
        super(port);

        try {
            start();
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to start login request listener on port " + port, ex);
            throw new RuntimeException(ex);
        }
    }

    @NonNull
    @Override
    public Response serve(IHTTPSession request) {
        
        Logger.printInfo(() -> "Serving request for URI: " + request.getUri());

        InputStream requestBodyInputStream = getRequestBodyInputStream(request);

        LoginRequest loginRequest;
        try {
            loginRequest = LoginRequest.parseFrom(requestBodyInputStream);
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to parse LoginRequest", ex);
            return newResponse(INTERNAL_ERROR);
        }

        MessageLite loginResponse;

        // A request may be made concurrently by Spotify,
        // however a webview can only handle one request at a time due to singleton cookie manager.
        // Therefore, synchronize to ensure that only one webview handles the request at a time.
        synchronized (this) {
            try {
                loginResponse = getLoginResponse(loginRequest);
            } catch (Exception ex) {
                Logger.printException(() -> "Failed to get login response", ex);
                return newResponse(INTERNAL_ERROR);
            }
        }

        return newResponse(Response.Status.OK, loginResponse);
    }


    private static LoginResponse getLoginResponse(@NonNull LoginRequest loginRequest) {
        Session session;

        if (!loginRequest.hasStoredCredential()) {
            Logger.printInfo(() -> "Received request for initial login");
            session = WebApp.currentSession; // Session obtained from WebApp.launchLogin, can be null if still in progress.
        } else {
            Logger.printInfo(() -> "Received request to restore saved session");
            session = Session.read(loginRequest.getStoredCredential().getUsername());
        }

        return toLoginResponse(session);
    }

    private static LoginResponse toLoginResponse(@Nullable Session session) {
        LoginResponse.Builder builder = LoginResponse.newBuilder();

        if (session == null) {
            Logger.printException(() -> "Session is null. An initial login may still be in progress, returning try again later error");
            builder.setError(LoginError.TRY_AGAIN_LATER);
        } else if (session.accessTokenExpired()) {
            Logger.printInfo(() -> "Access token expired, renewing session");
            WebApp.renewSessionBlocking(session.cookies);
            return toLoginResponse(WebApp.currentSession);
        } else if (session.username == null) {
            Logger.printException(() -> "Session username is null, likely caused by invalid cookies, returning invalid credentials error");
            session.delete();
            builder.setError(LoginError.INVALID_CREDENTIALS);
        } else if (session == FAILED_TO_RENEW_SESSION) {
            Logger.printException(() -> "Failed to renew session, likely caused by a timeout, returning try again later error");
            builder.setError(LoginError.TRY_AGAIN_LATER);
        } else {
            session.save();
            Logger.printInfo(() -> "Returning session for username: " + session.username);
            builder.setOk(LoginOk.newBuilder()
                    .setUsername(session.username)
                    .setAccessToken(session.accessToken)
                    .setStoredCredential(ByteString.fromHex("00")) // Placeholder, as it cannot be null or empty.
                    .setAccessTokenExpiresIn(session.accessTokenExpiresInSeconds())
                    .build());
        }

        return builder.build();
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
