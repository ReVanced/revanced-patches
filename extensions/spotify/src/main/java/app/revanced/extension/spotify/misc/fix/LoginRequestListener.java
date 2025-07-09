package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.clienttoken.data.v0.ClienttokenHttp;
import app.revanced.extension.spotify.login5.v4.proto.Login5.LoginError;
import app.revanced.extension.spotify.login5.v4.proto.Login5.LoginOk;
import app.revanced.extension.spotify.login5.v4.proto.Login5.LoginRequest;
import app.revanced.extension.spotify.login5.v4.proto.Login5.LoginResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import fi.iki.elonen.NanoHTTPD;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
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
        String uri = request.getUri();

        Logger.printInfo(() -> "Serving request for URI: " + uri);

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            Logger.printInfo(() -> entry.getKey() + " = " + entry.getValue());
        }

        InputStream requestBodyInputStream = getRequestBodyInputStream(request);
        byte[] requestBodyBytes;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int b;
            while ((b = requestBodyInputStream.read()) != -1) {
                buffer.write(b);
            }
            requestBodyBytes = buffer.toByteArray();
        } catch (IOException ignored) {
            return newResponse(INTERNAL_ERROR);
        }

        StringBuilder hexString = new StringBuilder();
        for (byte bb : requestBodyBytes) {
            hexString.append(String.format("%02X", bb));
        }
        Logger.printInfo(hexString::toString);

        if (uri.equals("/v1/clienttoken")) {
            ClienttokenHttp.ClientTokenRequest clientTokenRequest;
            try {
                clientTokenRequest = ClienttokenHttp.ClientTokenRequest.parseFrom(requestBodyBytes);
            } catch (IOException ex) {
                Logger.printException(() -> "Failed to parse ClientTokenRequest", ex);
                return newResponse(INTERNAL_ERROR);
            }

            try {
                HttpURLConnection clientTokenRequestConnection =
                        (HttpURLConnection) new URL("https://clienttoken.spotify.com/v1/clienttoken").openConnection();
                clientTokenRequestConnection.setRequestMethod("POST");
                clientTokenRequestConnection.setDoOutput(true);
                clientTokenRequestConnection.setRequestProperty("Content-Type", "application/x-protobuf");
                clientTokenRequestConnection.setRequestProperty("Accept", "application/x-protobuf");
                clientTokenRequestConnection.setRequestProperty("Connection", "Keep-Alive");
                clientTokenRequestConnection.setRequestProperty("User-Agent", request.getHeaders().get("user-agent"));

                byte[] clientTokenRequestData;

                if (clientTokenRequest.getRequestType() == ClienttokenHttp.ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST) {
                    Logger.printInfo(() -> "Spoofing ios client token");
                    clientTokenRequestData = clientTokenRequest.toBuilder()
                            .setClientData(clientTokenRequest.getClientData().toBuilder()
                                    .setClientVersion("iphone-9.0.58.558.g200011c")
                                    .setConnectivitySdkData(ClienttokenHttp.ConnectivitySdkData.newBuilder()
                                            .setDeviceId(clientTokenRequest.getClientData().getConnectivitySdkData().getDeviceId())
                                            .setPlatformSpecificData(ClienttokenHttp.PlatformSpecificData.newBuilder()
                                                    .setIos(ClienttokenHttp.NativeIOSData.newBuilder()
                                                            .setHwMachine("iPad8,11")
                                                            .setSystemVersion("19.0")
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build().toByteArray();
                } else {
                    clientTokenRequestData = clientTokenRequest.toByteArray();
                }

                hexString = new StringBuilder();
                for (byte bb : clientTokenRequestData) {
                    hexString.append(String.format("%02X", bb));
                }
                Logger.printInfo(hexString::toString);

                clientTokenRequestConnection.setRequestProperty("Content-Length", Integer.toString(clientTokenRequestData.length));
                clientTokenRequestConnection.getOutputStream().write(clientTokenRequestData);

                ClienttokenHttp.ClientTokenResponse clientTokenResponse =
                        ClienttokenHttp.ClientTokenResponse.parseFrom(clientTokenRequestConnection.getInputStream());

                if (clientTokenResponse.hasGrantedToken()) {
                    Logger.printInfo(() -> "Got client token " + clientTokenResponse.getGrantedToken().getToken());
                }

                return newResponse(Response.Status.OK, clientTokenResponse);
            } catch (Exception ex) {
                Logger.printException(() -> "ClientTokenRequest failure", ex);
                return newResponse(INTERNAL_ERROR);
            }
        }

//        if (uri.equals("/v3/login") || uri.equals("/v4/login")) {
//            LoginRequest loginRequest;
//            try {
//                loginRequest = LoginRequest.parseFrom(requestBodyBytes);
//            } catch (IOException ex) {
//                Logger.printException(() -> "Failed to parse LoginRequest", ex);
//                return newResponse(INTERNAL_ERROR);
//            }
//
//            try {
////                byte[] spoofedLoginRequest = loginRequest.toBuilder()
////                        .setClientInfo(loginRequest.getClientInfo().toBuilder()
////                                .setClientId("58bd3c95768941ea9eb4350aaa033eb3")
////                                .build())
////                        .setInteraction(loginRequest.getInteraction().toBuilder()
////                                .setFinish(loginRequest.getInteraction().getFinish().toBuilder()
////                                        .setUri(loginRequest.getInteraction().getFinish().getUri().replace("android", "ios"))
////                                        .build())
////                                .build())
////                        .build().toByteArray();
//
//                byte[] spoofedLoginRequest = loginRequest.toByteArray();
//
//                hexString = new StringBuilder();
//                for (byte bb : spoofedLoginRequest) {
//                    hexString.append(String.format("%02X", bb));
//                }
//                Logger.printInfo(hexString::toString);
//
//                HttpURLConnection loginRequestConnection =
//                        (HttpURLConnection) new URL("https://login5.spotify.com" + uri).openConnection();
//                loginRequestConnection.setRequestMethod("POST");
//                loginRequestConnection.setDoOutput(true);
//                loginRequestConnection.setRequestProperty("Content-Type", "application/x-protobuf");
//                loginRequestConnection.setRequestProperty("Accept", "application/x-protobuf");
//                loginRequestConnection.setRequestProperty("Connection", "Keep-Alive");
//                loginRequestConnection.setRequestProperty("User-Agent", "Spotify/9.0.50 iOS/17.7.2 (iPhone16,1)");
//                loginRequestConnection.setRequestProperty("client-token", SpoofClientPatch.clientToken);
//
//                loginRequestConnection.setRequestProperty("Content-Length", Integer.toString(spoofedLoginRequest.length));
//                loginRequestConnection.getOutputStream().write(spoofedLoginRequest);
//
//                int responseCode = loginRequestConnection.getResponseCode();
//                Logger.printInfo(() -> "Status code: " + responseCode);
//
//                LoginResponse loginResponse = LoginResponse.parseFrom(loginRequestConnection.getInputStream());
//                Logger.printInfo(() -> "Got error " + loginResponse.getErrorValue());
//                return newResponse(Response.Status.OK, loginResponse);
//            } catch (Exception ex) {
//                Logger.printException(() -> "LoginRequest failure", ex);
//                return newResponse(INTERNAL_ERROR);
//            }
//        }

        return newResponse(INTERNAL_ERROR);

        /* MessageLite loginResponse;

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

        return newResponse(Response.Status.OK, loginResponse); */
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
