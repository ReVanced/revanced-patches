package app.revanced.extension.shared.spoof;

import android.os.Build;

import androidx.annotation.Nullable;

import java.util.Locale;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;

public enum ClientType {
    // https://dumps.tadiphone.dev/dumps/oculus/eureka
    ANDROID_VR_NO_AUTH(
            28,
            "ANDROID_VR",
            "com.google.android.apps.youtube.vr.oculus",
            "Oculus",
            "Quest 3",
            "Qualcomm;SXR2230P",
            null,
            "Android",
            "12",
            "32", // Android 12.1
            "SQ3A.220605.009.A1",
            "1.56.21",
            false,
            "Android VR No auth"
    ),
    // Chromecast with Google TV 4K.
    // https://dumps.tadiphone.dev/dumps/google/kirkwood
    ANDROID_UNPLUGGED(
            29,
            "ANDROID_UNPLUGGED",
            "com.google.android.apps.youtube.unplugged",
            "Google",
            "Google TV Streamer",
            "Mediatek;MT8696",
            "244336107",
            "Android",
            "14",
            "34",
            "UTT3.240625.001.K5",
            "8.49.0",
            true,
            "Android TV"
    ),
    // Cannot play livestreams and lacks HDR, but can play videos with music and labeled "for children".
    // Google Pixel 9 Pro Fold
    // https://dumps.tadiphone.dev/dumps/google/barbet
    ANDROID_CREATOR(
            14,
            "ANDROID_CREATOR",
            "com.google.android.apps.youtube.creator",
            "Google",
            "Pixel 9 Pro Fold",
            "Google;Tensor G4",
            "244738035",
            "Android",
            "15",
            "35",
            "AP3A.241005.015.A2",
            "23.47.101",
            true,
            "Android Creator"
    ),
    ANDROID_VR(
            ANDROID_VR_NO_AUTH.id,
            ANDROID_VR_NO_AUTH.clientName,
            ANDROID_VR_NO_AUTH.packageName,
            ANDROID_VR_NO_AUTH.deviceMake,
            ANDROID_VR_NO_AUTH.deviceModel,
            ANDROID_VR_NO_AUTH.chipset,
            ANDROID_VR_NO_AUTH.gmscoreVersionCode,
            ANDROID_VR_NO_AUTH.osName,
            ANDROID_VR_NO_AUTH.osVersion,
            ANDROID_VR_NO_AUTH.androidSdkVersion,
            ANDROID_VR_NO_AUTH.buildId,
            ANDROID_VR_NO_AUTH.clientVersion,
            true,
            "Android VR"
    ),
    IOS_UNPLUGGED(
            33,
            "IOS_UNPLUGGED",
            "com.google.ios.youtubeunplugged",
            "Apple",
            forceAVC()
                    ? "iPhone12,5"  // 11 Pro Max (last device with iOS 13)
                    : "iPhone16,2", // 15 Pro Max
            null,
            null,
            "iOS",
            // iOS 13 and earlier uses only AVC. 14+ adds VP9 and AV1.
            forceAVC()
                    ? "13.7.17H35" // Last release of iOS 13.
                    : "18.2.22C152",
            null,
            null,
            // Version number should be a valid iOS release.
            // https://www.ipa4fun.com/history/152043/
            // Some newer versions can also force AVC,
            // but 6.45 is the last version that supports iOS 13.
            forceAVC()
                    ? "6.45"
                    : "8.49",
            true,
            forceAVC()
                    ? "iOS TV Force AVC"
                    : "iOS TV"
    );

    private static boolean forceAVC() {
        return BaseSettings.SPOOF_VIDEO_STREAMS_IOS_FORCE_AVC.get();
    }

    /**
     * YouTube
     * <a href="https://github.com/zerodytrash/YouTube-Internal-Clients?tab=readme-ov-file#clients">client type</a>
     */
    public final int id;

    public final String clientName;

    /**
     * App package name.
     */
    public final String packageName;

    /**
     * Device model, equivalent to {@link Build#MANUFACTURER} (System property: ro.product.vendor.manufacturer)
     */
    public final String deviceMake;

    /**
     * Device model, equivalent to {@link Build#MODEL} (System property: ro.product.vendor.model)
     */
    public final String deviceModel;

    /**
     * Android chipset.
     * Field is null if not applicable.
     */
    @Nullable
    public final String chipset;

    /**
     * GmsCore versionCode.
     * Field is null if not applicable.
     */
    @Nullable
    public final String gmscoreVersionCode; // JSON field does not use 'gmsCore' casing.

    /**
     * Device OS name.
     */
    public final String osName;

    /**
     * Device OS version.
     */
    public final String osVersion;

    /**
     * Player user-agent.
     */
    public final String userAgent;

    /**
     * Android SDK version, equivalent to {@link Build.VERSION#SDK} (System property: ro.build.version.sdk)
     * Field is null if not applicable.
     */
    @Nullable
    public final String androidSdkVersion;

    /**
     * Android build id, equivalent to {@link Build#ID}.
     * Field is null if not applicable.
     */
    @Nullable
    public final String buildId;

    /**
     * App version.
     */
    public final String clientVersion;

    /**
     * If the client can access the API logged in.
     */
    public final boolean canLogin;

    /**
     * Friendly name displayed in stats for nerds.
     */
    public final String friendlyName;

    @SuppressWarnings("ConstantLocale")
    ClientType(int id,
               String clientName,
               String packageName,
               String deviceMake,
               String deviceModel,
               @Nullable String chipset,
               @Nullable String gmscoreVersionCode,
               String osName,
               String osVersion,
               @Nullable String androidSdkVersion,
               @Nullable String buildId,
               String clientVersion,
               boolean canLogin,
               String friendlyName) {
        this.id = id;
        this.clientName = clientName;
        this.packageName = packageName;
        this.deviceMake = deviceMake;
        this.deviceModel = deviceModel;
        this.chipset = chipset;
        this.gmscoreVersionCode = gmscoreVersionCode;
        this.osName = osName;
        this.osVersion = osVersion;
        this.androidSdkVersion = androidSdkVersion;
        this.buildId = buildId;
        Locale defaultLocale = Locale.getDefault();
        if (androidSdkVersion != null) {
            // https://whatmyuseragent.com/apps/youtube
            this.userAgent = packageName + "/" + clientVersion + "(Linux; U; Android " + osVersion + "; "
                    + defaultLocale + "; " + deviceModel + " " + "Build/" + buildId + ") gzip";
        } else {
            // https://github.com/mitmproxy/mitmproxy/issues/4836
            // Convert version from '18.2.22C152' into '18_2_22'
            String userAgentOsVersion = osVersion
                    .replaceAll("(\\d+\\.\\d+\\.\\d+).*", "$1")
                    .replace(".", "_");
            this.userAgent = packageName + "/" + clientVersion + "(" + deviceModel + "; U; CPU " +
                    userAgentOsVersion + " like Mac OS X; " + defaultLocale + ")";
        }
        Logger.printDebug(() -> "user agent: " + this.userAgent);
        this.clientVersion = clientVersion;
        this.canLogin = canLogin;
        this.friendlyName = friendlyName;
    }

}
