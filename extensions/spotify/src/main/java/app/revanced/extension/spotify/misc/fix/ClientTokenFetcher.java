package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.clienttoken.data.v0.ClienttokenHttp.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ClientTokenFetcher {
    @NonNull
    static final String IOS_CLIENT_ID = "58bd3c95768941ea9eb4350aaa033eb3";

    @NonNull
    static final String CLIENT_TOKEN_API_PATH = "/v1/clienttoken";
    @NonNull
    static final String CLIENT_TOKEN_API_URL = "https://clienttoken.spotify.com" + CLIENT_TOKEN_API_PATH;

    // Modified by a patch. Do not touch.
    @NonNull
    static String getClientVersion() {
        return "";
    }

    // Modified by a patch. Do not touch.
    @NonNull
    static String getSystemVersion() {
        return "";
    }

    // Modified by a patch. Do not touch.
    @NonNull
    static String getHardwareMachine() {
        return "";
    }

    private static final ConnectivitySdkData DEFAULT_CONNECTIVITY_SDK_DATA =
            ConnectivitySdkData.newBuilder()
                    .setPlatformSpecificData(PlatformSpecificData.newBuilder()
                            .setIos(NativeIOSData.newBuilder()
                                    .setHwMachine(getHardwareMachine())
                                    .setSystemVersion(getSystemVersion())
                                    .build())
                            .build())
                    .build();

    private static final ClientDataRequest DEFAULT_CLIENT_DATA_REQUEST =
            ClientDataRequest.newBuilder()
                    .setClientVersion(getClientVersion())
                    .setClientId(IOS_CLIENT_ID)
                    .build();

    private static final ClientTokenRequest DEFAULT_CLIENT_TOKEN_REQUEST =
            ClientTokenRequest.newBuilder()
                    .setRequestType(ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST)
                    .build();


    @NonNull
    static ClientTokenRequest buildClientTokenRequest(String deviceId) {
        return DEFAULT_CLIENT_TOKEN_REQUEST
                .toBuilder()
                .setClientData(DEFAULT_CLIENT_DATA_REQUEST
                        .toBuilder()
                        .setConnectivitySdkData(DEFAULT_CONNECTIVITY_SDK_DATA
                                .toBuilder()
                                .setDeviceId(deviceId)
                                .build()
                        ).build()
                ).build();
    }

    @Nullable
    static ClientTokenResponse fetchClientToken(
            @NonNull ClientTokenRequest originalClientTokenRequest
    ) throws IOException {
        String iosUserAgent = getIOSUserAgent();
        if (iosUserAgent == null) {
            return null;
        }

        ClientTokenRequest clientTokenRequest = spoofClientTokenRequest(originalClientTokenRequest);

        HttpURLConnection clientTokenRequestConnection = createProtobufRequestConnection(CLIENT_TOKEN_API_URL);
        clientTokenRequestConnection.setRequestProperty("User-Agent", iosUserAgent);
        clientTokenRequestConnection.getOutputStream().write(clientTokenRequest.toByteArray());

        return ClientTokenResponse.parseFrom(clientTokenRequestConnection.getInputStream());
    }

    @NonNull
    static ClientTokenRequest spoofClientTokenRequest(
            @NonNull ClientTokenRequest originalClientTokenRequest
    ) {
        ClientTokenRequestType clientTokenRequestType = originalClientTokenRequest.getRequestType();

        if (clientTokenRequestType != ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST) {
            return originalClientTokenRequest;
        }

        String deviceId = originalClientTokenRequest.getClientData().getConnectivitySdkData().getDeviceId();
        return buildClientTokenRequest(deviceId);
    }

    @SuppressWarnings("SameParameterValue")
    @NonNull
    static HttpURLConnection createProtobufRequestConnection(String url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/x-protobuf");
        urlConnection.setRequestProperty("Accept", "application/x-protobuf");

        return urlConnection;
    }

    @Nullable
    static String getIOSUserAgent() {
        Pattern iosSpotifyVersionPattern = Pattern.compile("iphone-(\\d+\\.\\d+\\.\\d+)\\.\\d+");
        Matcher iosSpotifyVersionMatcher = iosSpotifyVersionPattern.matcher(getClientVersion());

        if (!iosSpotifyVersionMatcher.find()) {
            Logger.printException(() -> "Failed to match iOS Spotify version from the client version " +
                    getClientVersion());
            return null;
        }

        String iosSpotifyVersion = iosSpotifyVersionMatcher.group(1);
        return "Spotify/" + iosSpotifyVersion + " iOS/" +
                getSystemVersion() + " (" +
                getHardwareMachine() + ")";
    }
}
