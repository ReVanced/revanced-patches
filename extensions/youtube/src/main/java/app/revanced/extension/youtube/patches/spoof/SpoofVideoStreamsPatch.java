package app.revanced.extension.youtube.patches.spoof;

import static app.revanced.extension.shared.spoof.ClientType.ANDROID_CREATOR;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_43_32;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_61_48;
import static app.revanced.extension.shared.spoof.ClientType.IPADOS;
import static app.revanced.extension.shared.spoof.ClientType.VISIONOS;

import java.util.List;

import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.shared.spoof.requests.StreamingDataRequest;

@SuppressWarnings("unused")
public class SpoofVideoStreamsPatch {

    /**
     * Injection point.
     */
    public static void setClientOrderToUse() {
        List<ClientType> availableClients = List.of(
                ANDROID_VR_1_61_48,
                ANDROID_CREATOR,
                VISIONOS,
                IPADOS,
                // VR 1.43 must be last as spoof streaming data handles it slightly differently.
                ANDROID_VR_1_43_32
        );

        StreamingDataRequest.setClientOrderToUse(availableClients,
                BaseSettings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get());
    }
}