package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.clienttoken.data.v0.ClienttokenHttp;
import com.google.protobuf.MessageLite;
import fi.iki.elonen.NanoHTTPD;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

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
