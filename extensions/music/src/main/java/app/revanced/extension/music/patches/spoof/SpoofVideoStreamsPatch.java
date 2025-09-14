package app.revanced.extension.music.patches.spoof;

import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_43_32;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_61_48;

import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.shared.spoof.requests.StreamingDataRequest;

@SuppressWarnings("unused")
public class SpoofVideoStreamsPatch {

    /**
     * Injection point.
     */
    public static void setClientOrderToUse() {
        ClientType[] availableClients = {
                ANDROID_VR_1_43_32,
                ANDROID_VR_1_61_48,
        };

        StreamingDataRequest.setClientOrderToUse(availableClients, ANDROID_VR_1_43_32);
    }
}
