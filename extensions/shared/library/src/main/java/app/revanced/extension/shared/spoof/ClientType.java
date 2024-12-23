package app.revanced.extension.shared.spoof;

import android.os.Build;

import androidx.annotation.Nullable;

public enum ClientType {
    // https://dumps.tadiphone.dev/dumps/oculus/eureka
    ANDROID_VR_NO_AUTH( // Use as first fallback.
            28,
            "ANDROID_VR",
            "Quest 3",
            "12",
            "com.google.android.apps.youtube.vr.oculus/1.56.21 (Linux; U; Android 12; GB) gzip",
            "32", // Android 12.1
            "1.56.21",
            false,
            "Android VR (No auth)"
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
    ), // Requires login.
    // Fall over to authenticated ('hl' is ignored and audio is same as language set in users Google account).
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
            "iPhone16,2",
            "17.7.2.21H221",
            "com.google.ios.youtubeunplugged/8.33 (iPhone16,2; U; CPU iOS 17_7_2 like Mac OS X)",
            null,
            "8.33",
            true,
            "iOS TV"
    );

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
