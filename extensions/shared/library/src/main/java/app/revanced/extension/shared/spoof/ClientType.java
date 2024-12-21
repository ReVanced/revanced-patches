package app.revanced.extension.shared.spoof;

import android.os.Build;

import androidx.annotation.Nullable;

public enum ClientType {
    // https://dumps.tadiphone.dev/dumps/oculus/eureka
    ANDROID_VR(
            28,
            "Quest 3",
            "12",
            "com.google.android.apps.youtube.vr.oculus/1.56.21 (Linux; U; Android 12; GB) gzip",
            "32", // Android 12.1
            "1.56.21",
            true
    ),
    ANDROID_UNPLUGGED(
            29,
            "Google TV Streamer",
            "14",
            "com.google.android.apps.youtube.unplugged/8.49.0 (Linux; U; Android 14; GB) gzip",
            "34",
            "8.49.0",
            true
    ),
    ANDROID_CREATOR(
        14,
        "Android",
        "11",
        "com.google.android.apps.youtube.creator/24.45.100 (Linux; U; Android 11) gzip",
        "30",
        "24.45.100",
        true
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

    ClientType(int id,
               String deviceModel,
               String osVersion,
               String userAgent,
               @Nullable String androidSdkVersion,
               String clientVersion,
               boolean canLogin) {
        this.id = id;
        this.clientName = name();
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.userAgent = userAgent;
        this.androidSdkVersion = androidSdkVersion;
        this.clientVersion = clientVersion;
        this.canLogin = canLogin;
    }
}
