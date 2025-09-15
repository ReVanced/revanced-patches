package app.revanced.extension.youtube.patches.spoof;

import static app.revanced.extension.shared.spoof.ClientType.ANDROID_CREATOR;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_UNPLUGGED;
import static app.revanced.extension.shared.spoof.ClientType.ANDROID_VR_1_61_48;
import static app.revanced.extension.shared.spoof.ClientType.IOS_UNPLUGGED;

import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.shared.spoof.requests.StreamingDataRequest;

@SuppressWarnings("unused")
public class SpoofVideoStreamsPatch {

    /**
     * Injection point.
     */
    public static void setClientOrderToUse() {
        ClientType[] availableClients = {
                ANDROID_VR_1_61_48,
                ANDROID_UNPLUGGED,
                ANDROID_CREATOR,
                IOS_UNPLUGGED
        };

        StreamingDataRequest.setClientOrderToUse(availableClients,
                BaseSettings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get());
    }
}