package app.revanced.extension.shared.spoof;

import android.os.Build;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.settings.BaseSettings;

public enum ClientType {
    // https://dumps.tadiphone.dev/dumps/oculus/eureka
    ANDROID_VR_NO_AUTH(
            28,
            "ANDROID_VR",
            "Quest 3",
            "12",
            "com.google.android.apps.youtube.vr.oculus/1.56.21 (Linux; U; Android 12; GB) gzip",
            "32", // Android 12.1
            "1.56.21",
            false,
            "Android VR No auth"
    ),
    ANDROID_UNPLUGGED(
            29,
            "ANDROID_UNPLUGGED",
            "Google TV Streamer",
            "14",
            "com.google.android.apps.youtube.unplugged/8.49.0 (Linux; U; Android 14; GB) gzip",
            "34",
            "8.49.0",
            true,
            "Android TV"
    ),
    ANDROID_VR(
            ANDROID_VR_NO_AUTH.id,
            ANDROID_VR_NO_AUTH.clientName,
            ANDROID_VR_NO_AUTH.deviceModel,
            ANDROID_VR_NO_AUTH.osVersion,
            ANDROID_VR_NO_AUTH.userAgent,
            ANDROID_VR_NO_AUTH.androidSdkVersion,
            ANDROID_VR_NO_AUTH.clientVersion,
            true,
            "Android VR"
    ),
    IOS_UNPLUGGED(33,
            "IOS_UNPLUGGED",
            forceAVC()
                    ? "iPhone12,5"  // 11 Pro Max (last device with iOS 13)
                    : "iPhone16,2", // 15 Pro Max
            // iOS 13 and earlier uses only AVC. 14+ adds VP9 and AV1.
            forceAVC()
                    ? "13.7.17H35" // Last release of iOS 13.
                    : "18.1.1.22B91",
            forceAVC()
                    ? "com.google.ios.youtubeunplugged/6.45 (iPhone; U; CPU iOS 13_7 like Mac OS X)"
                    : "com.google.ios.youtubeunplugged/8.33 (iPhone; U; CPU iOS 18_1_1 like Mac OS X)",
            null,
            // Version number should be a valid iOS release.
            // https://www.ipa4fun.com/history/152043/
            // Some newer versions can also force AVC,
            // but 6.45 is the last version that supports iOS 13.
            forceAVC()
                    ? "6.45"
                    : "8.33",
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
     * Friendly name displayed in stats for nerds.
     */
    public final String friendlyName;

    ClientType(int id,
               String clientName,
               String deviceModel,
               String osVersion,
               String userAgent,
               @Nullable String androidSdkVersion,
               String clientVersion,
               boolean canLogin,
               String friendlyName) {
        this.id = id;
        this.clientName = clientName;
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.userAgent = userAgent;
        this.androidSdkVersion = androidSdkVersion;
        this.clientVersion = clientVersion;
        this.canLogin = canLogin;
        this.friendlyName = friendlyName;
    }

}
