package app.revanced.extension.music.patches.spoof;

import static app.revanced.extension.music.settings.Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_NO_SDK;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_43_32;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_61_48;
import static app.revanced.extension.shared.spoof.ClientType.VISIONOS;

import java.util.List;

import app.revanced.extension.shared.spoof.ClientType;

@SuppressWarnings("unused")
public class SpoofVideoStreamsPatch {

    /**
     * Injection point.
     */
    public static void setClientOrderToUse() {
        List<ClientType> availableClients = List.of(
                ANDROID_VR_1_43_32,
                ANDROID_NO_SDK,
                VISIONOS,
                ANDROID_VR_1_61_48
        );

        app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch.setClientsToUse(
                availableClients, SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get());
    }
}
