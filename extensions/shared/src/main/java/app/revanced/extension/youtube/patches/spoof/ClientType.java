package app.revanced.extension.youtube.patches.spoof;

import static app.revanced.extension.youtube.patches.spoof.DeviceHardwareSupport.allowAV1;
import static app.revanced.extension.youtube.patches.spoof.DeviceHardwareSupport.allowVP9;

import android.os.Build;

import androidx.annotation.Nullable;

public enum ClientType {
    // https://dumps.tadiphone.dev/dumps/oculus/eureka
    IOS(5,
            // iPhone 15 supports AV1 hardware decoding.
            // Only use if this Android device also has hardware decoding.
            allowAV1()
                    ? "iPhone16,2"  // 15 Pro Max
                    : "iPhone11,4", // XS Max
            // iOS 14+ forces VP9.
            allowVP9()
                    ? "17.5.1.21F90"
                    : "13.7.17H35",
            allowVP9()
                    ? "com.google.ios.youtube/19.10.7 (iPhone; U; CPU iOS 17_5_1 like Mac OS X)"
                    : "com.google.ios.youtube/19.10.7 (iPhone; U; CPU iOS 13_7 like Mac OS X)",
            null,
            // Version number should be a valid iOS release.
            // https://www.ipa4fun.com/history/185230
            "19.10.7"
    ),
    ANDROID_VR(28,
            "Quest 3",
            "12",
            "com.google.android.apps.youtube.vr.oculus/1.56.21 (Linux; U; Android 12; GB) gzip",
            "32", // Android 12.1
            "1.56.21"
    );

    /**
     * YouTube
     * <a href="https://github.com/zerodytrash/YouTube-Internal-Clients?tab=readme-ov-file#clients">client type</a>
     */
    public final int id;

    /**
     * Device model, equivalent to {@link Build#MODEL} (System property: ro.product.model)
     */
    public final String model;

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
    public final String appVersion;

    ClientType(int id, String model, String osVersion, String userAgent, @Nullable String androidSdkVersion, String appVersion) {
        this.id = id;
        this.model = model;
        this.osVersion = osVersion;
        this.userAgent = userAgent;
        this.androidSdkVersion = androidSdkVersion;
        this.appVersion = appVersion;
    }
}
