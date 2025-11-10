package app.revanced.extension.youtube.patches.spoof;

import static app.revanced.extension.shared.spoof.ClientType.ANDROID_CREATOR;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_NO_SDK;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_43_32;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_61_48;
import static app.revanced.extension.shared.spoof.ClientType.IPADOS;
import static app.revanced.extension.shared.spoof.ClientType.VISIONOS;

import java.util.List;

import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SpoofVideoStreamsPatch {

    public static final class SpoofClientAv1Availability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.isAvailable()
                    && Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get() == ANDROID_VR_1_43_32;
        }

        @Override
        public List<Setting<?>> getParentSettings() {
            return List.of(Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE);
        }
    }

    /**
     * Injection point.
     */
    public static void setClientOrderToUse() {
        ClientType client = Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get();

        // Use VR 1.61 client that has AV1 if user settings allow it.
        // AVC cannot be forced with VR 1.61 because it uses VP9 and AV1.
        // If both settings are on, then force AVC takes priority and VR 1.43 is used.
        if (client == ANDROID_VR_1_43_32 && Settings.SPOOF_VIDEO_STREAMS_AV1.get()
                && !Settings.FORCE_AVC_CODEC.get()) {
            client = ANDROID_VR_1_61_48;
        }

        List<ClientType> availableClients = List.of(
                VISIONOS,
                ANDROID_CREATOR,
                ANDROID_VR_1_43_32,
                ANDROID_NO_SDK,
                IPADOS);

        app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch.setClientsToUse(
                availableClients, client);
    }
}