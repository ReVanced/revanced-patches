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
        ClientType client = Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get();


        if (Settings.FORCE_AVC_CODEC.get() && client == ANDROID_VR_1_61_48) {
            // VR 1.61 uses VP9/AV1, and cannot force AVC. Use 1.43 instead.
            client = ANDROID_VR_1_43_32;
        }

        List<ClientType> availableClients = List.of(
                ANDROID_VR_1_43_32,
                VISIONOS,
                ANDROID_CREATOR,
                ANDROID_VR_1_61_48,
                IPADOS);

        app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch.setClientsToUse(
                availableClients, client);
    }
}