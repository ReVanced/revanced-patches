package app.revanced.extension.youtube.patches.spoof;

import static app.revanced.extension.shared.spoof.ClientType.ANDROID_CREATOR;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_43_32;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_61_48;
import static app.revanced.extension.shared.spoof.ClientType.IPADOS;
import static app.revanced.extension.shared.spoof.ClientType.VISIONOS;

import java.util.List;

import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SpoofVideoStreamsPatch {

    /**
     * Injection point.
     */
    public static void setClientOrderToUse() {
        // VR 1.61 uses VP9/AV1, and cannot disable VPC to use AVC.
        final boolean disableVP9 = Settings.DISABLE_VP9_CODEC.get();

        ClientType client = Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get();
        if (disableVP9 && client == ANDROID_VR_1_61_48) {
            client = ANDROID_VR_1_43_32; // Use VR 1.43 instead.
        }

        List<ClientType> availableClients = disableVP9
                ? List.of(
                ANDROID_VR_1_43_32,
                VISIONOS,
                ANDROID_CREATOR,
                ANDROID_VR_1_61_48,
                IPADOS)
                : List.of(
                ANDROID_VR_1_61_48,
                VISIONOS,
                ANDROID_CREATOR,
                ANDROID_VR_1_43_32,
                IPADOS
        );

        app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch.setClientsToUse(
                availableClients, client);
    }
}