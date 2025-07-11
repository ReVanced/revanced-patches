package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.misc.fix.clienttoken.data.v0.ClienttokenHttp.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static app.revanced.extension.spotify.misc.fix.Constants.*;

class ClientTokenService {
    private static final String IOS_CLIENT_ID = "58bd3c95768941ea9eb4350aaa033eb3";
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

    private static final ConnectivitySdkData.Builder IOS_CONNECTIVITY_SDK_DATA =
            ConnectivitySdkData.newBuilder()
                    .setPlatformSpecificData(PlatformSpecificData.newBuilder()
                            .setIos(NativeIOSData.newBuilder()
                                    .setHwMachine(getHardwareMachine())
                                    .setSystemVersion(getSystemVersion())
                            )
                    );

    private static final ClientDataRequest.Builder IOS_CLIENT_DATA_REQUEST =
            ClientDataRequest.newBuilder()
                    .setClientVersion(getClientVersion())
                    .setClientId(IOS_CLIENT_ID);

    private static final ClientTokenRequest.Builder IOS_CLIENT_TOKEN_REQUEST =
            ClientTokenRequest.newBuilder()
                    .setRequestType(ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST);


    @NonNull
    static ClientTokenRequest newIOSClientTokenRequest(String deviceId) {
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
    static ClientTokenResponse getClientTokenResponse(@NonNull ClientTokenRequest request) {
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

    @Nullable
    static ClientTokenResponse serveClientTokenRequest(@NonNull InputStream inputStream) {
        ClientTokenRequest request;
        try {
            request = ClientTokenRequest.parseFrom(inputStream);
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to parse request from input stream", ex);
            return null;
        }
        Logger.printInfo(() -> "Request of type: " + request.getRequestType());

        ClientTokenResponse response = getClientTokenResponse(request);
        if (response != null) Logger.printInfo(() -> "Response of type: " + response.getResponseType());

        return response;
    }
}
