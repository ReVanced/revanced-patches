package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.clienttoken.data.v0.ClienttokenHttp.*;

import java.io.IOException;

import static app.revanced.extension.spotify.misc.fix.Constants.*;

class ClientTokenService {
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

    interface ClientTokenRequestHandler {
        @NonNull
        ClientTokenResponse request(@NonNull ClientTokenRequest request) throws IOException;
    }

    @Nullable
    static ClientTokenResponse getClientTokenResponse(
            @NonNull ClientTokenRequest request,
            @NonNull ClientTokenRequestHandler handler
    ) {
        StringBuilder hex = new StringBuilder();
        for (byte b : request.toByteArray()) {
            hex.append(String.format("%02X", b)); // Uppercase hex
        }
        Logger.printInfo(() -> "original request " + hex);

        ClientTokenRequestType clientTokenRequestType = request.getRequestType();
        if (clientTokenRequestType == ClientTokenRequestType.REQUEST_CLIENT_DATA_REQUEST) {
            Logger.printInfo(() -> "Client token data request received");
            String deviceId = request.getClientData().getConnectivitySdkData().getDeviceId();
            request = newIOSClientTokenRequest(deviceId);
            StringBuilder hex2 = new StringBuilder();
            for (byte b : request.toByteArray()) {
                hex2.append(String.format("%02X", b)); // Uppercase hex
            }
            Logger.printInfo(() -> "spoofed request " + hex2);
        } else if (clientTokenRequestType == ClientTokenRequestType.REQUEST_CHALLENGE_ANSWERS_REQUEST) {
            Logger.printInfo(() -> "Client token challenge answers request received");
        }


        ClientTokenResponse clientTokenResponse;
        try {
            clientTokenResponse = handler.request(request);
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to request client token", ex);
            return null;
        }

        ClientTokenResponseType clientTokenResponseType = clientTokenResponse.getResponseType();
        if (clientTokenResponseType == ClientTokenResponseType.RESPONSE_GRANTED_TOKEN_RESPONSE) {
            Logger.printInfo(() -> "Fetched iOS client token: " + clientTokenResponse.getGrantedToken().getToken());
        } else if (clientTokenResponseType == ClientTokenResponseType.RESPONSE_CHALLENGES_RESPONSE) {
            Logger.printInfo(() -> "Received client token challenge");
        }

        return clientTokenResponse;
    }
}
