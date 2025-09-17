package app.revanced.extension.youtube.patches.spoof;

import static app.revanced.extension.shared.spoof.ClientType.ANDROID_CREATOR;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_43_32;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_61_48;
import static app.revanced.extension.shared.spoof.ClientType.IPADOS;
import static app.revanced.extension.shared.spoof.ClientType.VISIONOS;

import java.util.List;

import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.shared.spoof.requests.StreamingDataRequest;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SpoofVideoStreamsPatch {

    /**
     * Injection point.
     */
    public static void setClientOrderToUse() {
        List<ClientType> availableClients = List.of(
                ANDROID_VR_1_61_48,
                VISIONOS,
                ANDROID_CREATOR,
                ANDROID_VR_1_43_32,
                IPADOS
        );

        ClientType client = Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get();
        app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch.setPreferredClient(client);
        StreamingDataRequest.setClientOrderToUse(availableClients, client);
    }
}