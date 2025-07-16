package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.misc.fix.clienttoken.data.v0.ClienttokenHttp.*;
import com.google.protobuf.MessageLite;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static app.revanced.extension.spotify.misc.fix.Constants.*;
import static app.revanced.extension.spotify.misc.fix.RequestListener.INTERNAL_ERROR_RESPONSE;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

class SpoofClientService {
    private final String requestListenerHost;

    private static final String IOS_CLIENT_ID = "58bd3c95768941ea9eb4350aaa033eb3";
    private static final String IOS_USER_AGENT;
    private static final int[] PLAY_PLAY_REQUEST_BODY_TOKEN_BYTES = {
            0x01, 0xAE, 0x93, 0x3B, 0x6E, 0xFB, 0xE8, 0xF3,
            0x53, 0xB8, 0xFD, 0x1A, 0x61, 0x56, 0xBE, 0x94
    };

    static {
        String clientVersion = getClientVersion();
        int commitHashIndex = clientVersion.lastIndexOf(".");
        String version = clientVersion.substring(
                clientVersion.indexOf("-") + 1,
                clientVersion.lastIndexOf(".", commitHashIndex - 1)
        );

        IOS_USER_AGENT = "Spotify/" + version + " iOS/" + getSystemVersion() + " (" + getHardwareMachine() + ")";
    }

    SpoofClientService(int requestListenerPort) {
        requestListenerHost = "127.0.0.1:" + requestListenerPort;
    }

    private final ConnectivitySdkData.Builder IOS_CONNECTIVITY_SDK_DATA =
            ConnectivitySdkData.newBuilder()
                    .setPlatformSpecificData(PlatformSpecificData.newBuilder()
                            .setIos(NativeIOSData.newBuilder()
                                    .setHwMachine(getHardwareMachine())
                                    .setSystemVersion(getSystemVersion())
                            )
                    );

    private final ClientDataRequest.Builder IOS_CLIENT_DATA_REQUEST =
            ClientDataRequest.newBuilder()
                    .setClientVersion(getClientVersion())
                    .setClientId(IOS_CLIENT_ID);

    private final ClientTokenRequest.Builder IOS_CLIENT_TOKEN_REQUEST =
            ClientTokenRequest.newBuilder()
                    .setRequestType(ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST);


    @NonNull
    ClientTokenRequest newIOSClientTokenRequest(String deviceId) {
        Logger.printInfo(() -> "Creating new iOS client token request with device ID: " + deviceId);

        return IOS_CLIENT_TOKEN_REQUEST
                .setClientData(IOS_CLIENT_DATA_REQUEST
                        .setConnectivitySdkData(IOS_CONNECTIVITY_SDK_DATA
                                .setDeviceId(deviceId)
                        )
                )
                .build();
    }

    @Nullable
    ClientTokenResponse getClientTokenResponse(@NonNull ClientTokenRequest request) {
        if (request.getRequestType() == ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST) {
            Logger.printInfo(() -> "Requesting iOS client token");
            String deviceId = request.getClientData().getConnectivitySdkData().getDeviceId();
            request = newIOSClientTokenRequest(deviceId);
        }

        ClientTokenResponse response;
        try {
            response = requestClientToken(request);
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to handle request", ex);
            return null;
        }

        return response;
    }

    @NonNull
    private ClientTokenResponse requestClientToken(@NonNull ClientTokenRequest request) throws IOException {

        HttpURLConnection connection = getHttpUrlConnection(
                "POST",
                "https://clienttoken.spotify.com/v1/clienttoken",
                new HashMap<>(), // So that the default headers are added in getHttpUrlConnection.
                new ByteArrayInputStream(request.toByteArray())
        );

        try (InputStream inputStream = connection.getInputStream()) {
            return ClientTokenResponse.parseFrom(inputStream);
        }
    }

    Response serveClientTokenRequest(@NonNull IHTTPSession session) {
        InputStream inputStream = getInputStream(session);

        ClientTokenRequest request;
        try {
            request = ClientTokenRequest.parseFrom(inputStream);
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to parse request from input stream", ex);
            return INTERNAL_ERROR_RESPONSE;
        }
        Logger.printInfo(() -> "Request of type: " + request.getRequestType());

        ClientTokenResponse response = getClientTokenResponse(request);
        if (response == null) return INTERNAL_ERROR_RESPONSE;

        Logger.printInfo(() -> "Response of type: " + response.getResponseType());
        return newResponse(response);

    }

    Response serveApResolveRequest(@NonNull IHTTPSession session) {
        // Make original request.
        HttpURLConnection connection;
        try {
            connection = getHttpUrlConnection(
                    "GET",
                    "https://apresolve.spotify.com/?" + session.getQueryParameterString(),
                    null,
                    null
            );
        } catch (IOException e) {
            Logger.printException(() -> "Failed to create HTTP connection for AP resolve request", e);
            return INTERNAL_ERROR_RESPONSE;
        }

        // Read response body.
        String response;
        try (InputStream inputStream = connection.getInputStream()) {
            response = inputStreamToString(inputStream);
        } catch (IOException e) {
            Logger.printException(() -> "Failed to read response body", e);
            return INTERNAL_ERROR_RESPONSE;
        }

        // Spoof spclient hosts.
        try {
            JSONObject responseJson = new JSONObject(response);

            if (responseJson.has("spclient")) {
                JSONArray spoofedHosts = new JSONArray();
                // TODO: Seems like Spotify does not connect to the host.
                //  It is possible that it needs a HTTPS connection.
                //  If so, TLS needs to be implemented, hopefully Spotify does not pin certificates.
                //  It is also possible that they expect a specific host, like "gew4-spclient.spotify.com".
                //  In this case, the DNS must be spoofed. Swapping ports may be possible, TLS may still be required.
                spoofedHosts.put("localhost:4345");
                responseJson.put("spclient", spoofedHosts);

                response = responseJson.toString();

                String finalResponse = response;
                Logger.printInfo(() -> "Spoofed AP resolve response: " + finalResponse);
            }
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to parse response JSON", e);
            return INTERNAL_ERROR_RESPONSE;
        }

        return newResponse(response, "application/json");
    }

    Response servePlayPlayRequest(@NonNull IHTTPSession session) {
        String path = session.getUri();
        Map<String, String> headers = session.getHeaders();

        // TODO: Do not hardcode the region. Spotify chooses one of the available regions.
        String url = "https://gew4-spclient.spotify.com/" + path;

        HttpURLConnection connection;
        try {
            connection = getHttpUrlConnection(
                    "POST",
                    url,
                    headers,
                    getInputStream(session)
            );
        } catch (IOException e) {
            Logger.printException(() -> "Failed to create HTTP connection for playplay request", e);
            return INTERNAL_ERROR_RESPONSE;
        }

        Response response;
        try {
            response = connectionToResponse(connection);
        } catch (IOException e) {
            Logger.printException(() -> "Failed to convert connection to response for playplay request", e);
            return INTERNAL_ERROR_RESPONSE;
        } finally {
            connection.disconnect();
        }

        byte[] bytes;
        try {
            bytes = inputStreamToBytes(response.getData());
        } catch (IOException e) {
            Logger.printException(() -> "Failed to read response data for playplay request", e);
            return INTERNAL_ERROR_RESPONSE;
        }

        for (int i = 0; i < PLAY_PLAY_REQUEST_BODY_TOKEN_BYTES.length; i++)
            bytes[i + 4] = (byte) PLAY_PLAY_REQUEST_BODY_TOKEN_BYTES[i];
        response.setData(new ByteArrayInputStream(bytes));

        return response;
    }

    Response serveSpClientRequest(@NonNull IHTTPSession session) {
        String method = session.getMethod().name();
        String path = session.getUri();
        String queryParameterString = session.getQueryParameterString();
        Map<String, String> headers = session.getHeaders();

        // TODO: Do not hardcode the region. Spotify chooses one of the available regions.
        String url = "https://gew4-spclient.spotify.com/" + path + (queryParameterString != null ? "?" + queryParameterString : "");

        HttpURLConnection connection;
        try {
            connection = getHttpUrlConnection(
                    method,
                    url,
                    headers,
                    headers.get("content-length") == null ? null : getInputStream(session)
            );
        } catch (IOException e) {
            Logger.printException(() -> "Failed to create HTTP connection", e);
            return INTERNAL_ERROR_RESPONSE;
        }

        try {
            return connectionToResponse(connection);
        } catch (IOException e) {
            Logger.printException(() -> "Failed to convert connection to response", e);
        } finally {
            connection.disconnect();
        }

        return INTERNAL_ERROR_RESPONSE;
    }

    private Response connectionToResponse(HttpURLConnection connection) throws IOException {
        // Parse connection.
        Status status = Status.lookup(connection.getResponseCode());
        Map<String, List<String>> headers = connection.getHeaderFields();
        String contentType = connection.getContentType();
        long contentLength = 0;
        InputStream responseBody = null;
        String contentLengthString = connection.getHeaderField("Content-Length");
        if (contentLengthString != null) {
            contentLength = Long.parseLong(contentLengthString);
            responseBody = connection.getInputStream();
        }

        // Create a response.
        Response response = newFixedLengthResponse(status, contentType, responseBody, contentLength);
        for (Map.Entry<String, List<String>> stringListEntry : headers.entrySet())
            for (String headerValue : stringListEntry.getValue())
                response.addHeader(stringListEntry.getKey(), headerValue);

        return response;
    }

    @NonNull
    private HttpURLConnection getHttpUrlConnection(
            @NonNull String method,
            @NonNull String url,
            Map<String, String> headers,
            InputStream bodyInputStream
    ) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet())
                connection.setRequestProperty(header.getKey(), header.getValue());

            connection.setRequestProperty("Content-Type", "application/x-protobuf");
            connection.setRequestProperty("Accept", "application/x-protobuf");
            connection.setRequestProperty("User-Agent", IOS_USER_AGENT);
        }

        if (bodyInputStream != null) {
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = bodyInputStream.read(buffer)) != -1) outputStream.write(buffer, 0, bytesRead);
            }
        }

        return connection;
    }

    private static String inputStreamToString(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    private static byte[] inputStreamToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    @NonNull
    private static Response newResponse(MessageLite messageLite) {
        byte[] messageBytes = messageLite.toByteArray();
        InputStream stream = new ByteArrayInputStream(messageBytes);
        return newFixedLengthResponse(OK, "application/x-protobuf", stream, messageBytes.length);
    }

    @NonNull
    private static Response newResponse(String string, String contentType) {
        byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(stringBytes);
        return newFixedLengthResponse(OK, contentType, stream, stringBytes.length);
    }

    @NonNull
    private static InputStream getInputStream(@NonNull IHTTPSession session) {
        return new FilterInputStream(session.getInputStream()) {
            private long remaining = Long.parseLong(Objects.requireNonNull(session.getHeaders().get("content-length")));

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
}
