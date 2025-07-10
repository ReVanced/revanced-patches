package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.clienttoken.data.v0.ClienttokenHttp.*;

import java.io.IOException;
import java.security.PrivateKey;

import static app.revanced.extension.spotify.misc.fix.Constants.*;

class ClientTokenService {
    private static final String IOS_CLIENT_ID = "58bd3c95768941ea9eb4350aaa033eb3";

    private static final ConnectivitySdkData IOS_CONNECTIVITY_SDK_DATA =
            ConnectivitySdkData.newBuilder()
                    .setPlatformSpecificData(PlatformSpecificData.newBuilder()
                            .setIos(NativeIOSData.newBuilder()
                                    .setHwMachine(getHardwareMachine())
                                    .setSystemVersion(getSystemVersion())
                                    .build())
                            .build())
                    .build();

    private static final ClientDataRequest IOS_CLIENT_DATA_REQUEST =
            ClientDataRequest.newBuilder()
                    .setClientVersion(getClientVersion())
                    .setClientId(IOS_CLIENT_ID)
                    .build();

    private static final ClientTokenRequest IOS_CLIENT_TOKEN_REQUEST =
            ClientTokenRequest.newBuilder()
                    .setRequestType(ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST)
                    .build();


    @NonNull
    static ClientTokenRequest newIOSClientTokenRequest(String deviceId) {
        Logger.printInfo(() -> "Creating new iOS client token request with device ID: " + deviceId);
        return IOS_CLIENT_TOKEN_REQUEST
                .toBuilder()
                .setClientData(IOS_CLIENT_DATA_REQUEST
                        .toBuilder()
                        .setConnectivitySdkData(IOS_CONNECTIVITY_SDK_DATA
                                .toBuilder()
                                .setDeviceId(deviceId)
                        )
                ).build();
    }

    interface ClientTokenRequestHandler {
        @NonNull
        ClientTokenResponse request(@NonNull ClientTokenRequest request) throws IOException;
    }

    static ClientTokenResponse getClientTokenResponse(
            @NonNull ClientTokenRequest request,
            @NonNull ClientTokenRequestHandler handler
    ) {
        if (request.getRequestType() == ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST) {
            String deviceId = request.getClientData().getConnectivitySdkData().getDeviceId();
            request = newIOSClientTokenRequest(deviceId);
        } else {
            Logger.printInfo(() -> "Client token request type is not REQUEST_CLIENT_DATA_REQUEST");
            return null;
        }

        ClientTokenResponse clientTokenResponse;
        try {
            clientTokenResponse = handler.request(request);
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to request client token", ex);
            return null;
        }

        if (clientTokenResponse.getResponseType() != ClientTokenResponseType.RESPONSE_GRANTED_TOKEN_RESPONSE) {
            Logger.printException(() -> "Unexpected client token response type: " +
                    clientTokenResponse.getResponseType());
            return null;
        }

        return clientTokenResponse;
    }
}
