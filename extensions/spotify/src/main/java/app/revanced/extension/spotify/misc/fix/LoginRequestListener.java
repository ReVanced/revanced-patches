package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.login5.v4.proto.Login5.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;

class LoginRequestListener extends NanoHTTPD {
    LoginRequestListener(int port) {
        super(port);
    }

    @NonNull
    @Override
    public Response serve(IHTTPSession request) {
        Logger.printInfo(() -> "Request URI: " + request.getUri());

        InputStream requestBodyInputStream = getRequestBodyInputStream(request);

        LoginRequest loginRequest;
        try {
            loginRequest = LoginRequest.parseFrom(requestBodyInputStream);
        } catch (IOException e) {
            Logger.printException(() -> "Failed to parse LoginRequest", e);
            return newResponse(INTERNAL_ERROR);
        }

        MessageLite loginResponse;

        // A request may be made concurrently by Spotify,
        // however a webview can only handle one request at a time due to singleton cookie manager.
        // Therefore, synchronize to ensure that only one webview handles the request at a time.
        synchronized (this) {
            loginResponse = getLoginResponse(loginRequest);
        }

        if (loginResponse != null) {
            return newResponse(Response.Status.OK, loginResponse);
        }

        return newResponse(INTERNAL_ERROR);
    }


    @Nullable
    private static LoginResponse getLoginResponse(@NonNull LoginRequest loginRequest) {
        Session session;

        boolean isInitialLogin = !loginRequest.hasStoredCredential();
        if (isInitialLogin) {
            Logger.printInfo(() -> "Initial login request");
            session = WebApp.currentSession; // Session obtained from WebApp.login.
        } else {
            Logger.printInfo(() -> "Session restore request");
            session = Session.read(loginRequest.getStoredCredential().getUsername());
        }

        return toLoginResponse(session, isInitialLogin);
    }


    private static LoginResponse toLoginResponse(Session session, boolean isInitialLogin) {
        LoginResponse.Builder builder = LoginResponse.newBuilder();

        if (session == null) {
            if (isInitialLogin) {
                Logger.printInfo(() -> "Session is null, returning try again later error for initial login");
                builder.setError(LoginError.TRY_AGAIN_LATER);
            } else {
                Logger.printInfo(() -> "Session is null, returning invalid credentials error for stored credential login");
                builder.setError(LoginError.INVALID_CREDENTIALS);
            }
        } else if (session.username == null) {
            Logger.printInfo(() -> "Session username is null, returning invalid credentials error");
            builder.setError(LoginError.INVALID_CREDENTIALS);
        } else if (session.accessTokenExpired()) {
            Logger.printInfo(() -> "Access token has expired, renewing session");
            WebApp.refreshSession(session.cookies);
            return toLoginResponse(WebApp.currentSession, isInitialLogin);
        } else {
            Logger.printInfo(() -> "Returning session for username: " + session.username);
            session.save();
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
