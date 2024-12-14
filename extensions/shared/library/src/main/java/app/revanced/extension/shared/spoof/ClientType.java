package app.revanced.extension.shared.spoof;

import android.os.Build;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.settings.BaseSettings;

public enum ClientType {
    // Specific purpose for age restricted, or private videos, because the iOS client is not logged in.
    // https://dumps.tadiphone.dev/dumps/oculus/eureka
    ANDROID_VR(28,
            "ANDROID_VR",
            "Quest 3",
            "12",
            "com.google.android.apps.youtube.vr.oculus/1.56.21 (Linux; U; Android 12; GB) gzip",
            "32", // Android 12.1
            "1.56.21",
            true,
            true),
    // Specific for kids videos.
    IOS(5,
            "IOS",
            forceAVC()
                    ? "iPhone12,5"  // 11 Pro Max (last device with iOS 13)
                    : "iPhone16,2", // 15 Pro Max
            // iOS 13 and earlier uses only AVC.  14+ adds VP9 and AV1.
            forceAVC()
                    ? "13.7.17H35" // Last release of iOS 13.
                    : "17.5.1.21F90",
            forceAVC()
                    ? "com.google.ios.youtube/17.40.5 (iPhone; U; CPU iOS 13_7 like Mac OS X)"
                    : "com.google.ios.youtube/19.47.7 (iPhone; U; CPU iOS 17_5_1 like Mac OS X)",
            null,
            // Version number should be a valid iOS release.
            // https://www.ipa4fun.com/history/185230
            forceAVC()
                    // Some newer versions can also force AVC,
                    // but 17.40 is the last version that supports iOS 13.
                    ? "17.40.5"
                    : "19.47.7",
            false,
            true),
    /**
     * Android VR with no language code.
     * Used for age restricted videos and YouTube Music to disable stable volume.
     */
    ANDROID_VR_NO_HL(
            ANDROID_VR.id,
            ANDROID_VR.clientName,
            ANDROID_VR.deviceModel,
            ANDROID_VR.osVersion,
            ANDROID_VR.userAgent,
            ANDROID_VR.androidSdkVersion,
            ANDROID_VR.clientVersion,
            ANDROID_VR.canLogin,
            false);

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
     * Device model, equivalent to {@link Build#MODEL} (System property: ro.product.model)
     */
    public final String deviceModel;

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
     * App version.
     */
    public final String clientVersion;

    /**
     * If the client can access the API logged in.
     */
    public final boolean canLogin;

    /**
     * If a language code should be used.
     */
    public final boolean useLanguageCode;

    ClientType(int id,
               String clientName,
               String deviceModel,
               String osVersion,
               String userAgent,
               @Nullable String androidSdkVersion,
               String clientVersion,
               boolean canLogin,
               boolean useLanguageCode) {
        this.id = id;
        this.clientName = clientName;
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.userAgent = userAgent;
        this.androidSdkVersion = androidSdkVersion;
        this.clientVersion = clientVersion;
        this.canLogin = canLogin;
        this.useLanguageCode = useLanguageCode;
    }
}
