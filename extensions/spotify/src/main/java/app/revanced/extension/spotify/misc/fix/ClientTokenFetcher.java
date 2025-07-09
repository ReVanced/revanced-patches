package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.clienttoken.data.v0.ClienttokenHttp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ClientTokenFetcher {
    @NonNull
    static final String IOS_CLIENT_ID = "58bd3c95768941ea9eb4350aaa033eb3";

    @NonNull
    static final String CLIENT_TOKEN_PATH = "/v1/clienttoken";
    @NonNull
    static final String CLIENT_TOKEN_ENDPOINT = "https://clienttoken.spotify.com" + CLIENT_TOKEN_PATH;

    // The return value of these methods are overriden by the patch.
    @NonNull
    static String getClientVersion() {
        return "";
    }

    @NonNull
    static String getSystemVersion() {
        return "";
    }

    @NonNull
    static String getHardwareMachine() {
        return "";
    }

    @NonNull
    static ClienttokenHttp.ClientTokenRequest buildSpoofedClientTokenRequest(String deviceId) {
        return ClienttokenHttp.ClientTokenRequest.newBuilder()
                .setRequestType(ClienttokenHttp.ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST)
                .setClientData(ClienttokenHttp.ClientDataRequest.newBuilder()
                        .setClientVersion(getClientVersion())
                        .setClientId(IOS_CLIENT_ID)
                        .setConnectivitySdkData(ClienttokenHttp.ConnectivitySdkData.newBuilder()
                                .setDeviceId(deviceId)
                                .setPlatformSpecificData(ClienttokenHttp.PlatformSpecificData.newBuilder()
                                        .setIos(ClienttokenHttp.NativeIOSData.newBuilder()
                                                .setHwMachine(getHardwareMachine())
                                                .setSystemVersion(getSystemVersion())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Nullable
    static ClienttokenHttp.ClientTokenResponse fetchClientToken(
            @NonNull ClienttokenHttp.ClientTokenRequest originalClientTokenRequest
    ) throws IOException {
        String iosUserAgent = getIOSUserAgent();
        if (iosUserAgent == null) {
            return null;
        }

        ClienttokenHttp.ClientTokenRequest clientTokenRequest = spoofClientTokenRequest(originalClientTokenRequest);

        HttpURLConnection clientTokenRequestConnection = createProtobufRequestConnection(CLIENT_TOKEN_ENDPOINT);
        clientTokenRequestConnection.setRequestProperty("User-Agent", iosUserAgent);
        clientTokenRequestConnection.getOutputStream().write(clientTokenRequest.toByteArray());

        return ClienttokenHttp.ClientTokenResponse.parseFrom(clientTokenRequestConnection.getInputStream());
    }

    @NonNull
    static ClienttokenHttp.ClientTokenRequest spoofClientTokenRequest(
            @NonNull ClienttokenHttp.ClientTokenRequest originalClientTokenRequest
    ) {
        ClienttokenHttp.ClientTokenRequestType clientTokenRequestType = originalClientTokenRequest.getRequestType();

        if (clientTokenRequestType != ClienttokenHttp.ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST) {
            return originalClientTokenRequest;
        }

        String deviceId = originalClientTokenRequest.getClientData().getConnectivitySdkData().getDeviceId();
        return buildSpoofedClientTokenRequest(deviceId);
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
