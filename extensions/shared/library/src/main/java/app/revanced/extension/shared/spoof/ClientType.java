package app.revanced.extension.shared.spoof;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("ConstantLocale")
public enum ClientType {
    /**
     * Video not playable: Paid, Movie, Private, Age-restricted.
     * Uses non-adaptive bitrate.
     * AV1 codec available.
     */
    ANDROID_REEL(
            3,
            "ANDROID",
            "com.google.android.youtube",
            Build.MANUFACTURER,
            Build.MODEL,
            "Android",
            Build.VERSION.RELEASE,
            String.valueOf(Build.VERSION.SDK_INT),
            Build.ID,
            "20.44.38",
            // This client has been used by most open-source YouTube stream extraction tools since 2024, including NewPipe Extractor, SmartTube, and Grayjay.
            // This client can log in, but if an access token is used in the request, GVS can more easily identify the request as coming from ReVanced.
            // This means that the GVS server can strengthen its validation of the ANDROID_REEL client.
            true,
            true,
            false,
            "Android Reel"
    ),
    /**
     * Video not playable: Kids / Paid / Movie / Private / Age-restricted.
     * This client can only be used when logged out.
     */
    // https://dumps.tadiphone.dev/dumps/oculus/eureka
    ANDROID_VR_1_61_48(
            28,
            "ANDROID_VR",
            "com.google.android.apps.youtube.vr.oculus",
            "Oculus",
            "Quest 3",
            "Android",
            "12",
            // Android 12.1
            "32",
            "SQ3A.220605.009.A1",
            "1.61.48",
            false,
            false,
            true,
            "Android VR 1.61"
    ),
    /**
     * Uses non adaptive bitrate, which fixes audio stuttering with YT Music.
     * Does not use AV1.
     */
    ANDROID_VR_1_43_32(
            ANDROID_VR_1_61_48.id,
            ANDROID_VR_1_61_48.clientName,
            Objects.requireNonNull(ANDROID_VR_1_61_48.packageName),
            ANDROID_VR_1_61_48.deviceMake,
            ANDROID_VR_1_61_48.deviceModel,
            ANDROID_VR_1_61_48.osName,
            ANDROID_VR_1_61_48.osVersion,
            Objects.requireNonNull(ANDROID_VR_1_61_48.androidSdkVersion),
            Objects.requireNonNull(ANDROID_VR_1_61_48.buildId),
            "1.43.32",
            ANDROID_VR_1_61_48.useAuth,
            ANDROID_VR_1_61_48.supportsMultiAudioTracks,
            ANDROID_VR_1_61_48.usePlayerEndpoint,
            "Android VR 1.43"
    ),
    /**
     * Cannot play livestreams and lacks HDR, but can play videos with music and labeled "for children".
     * <a href="https://dumps.tadiphone.dev/dumps/google/barbet">Google Pixel 9 Pro Fold</a>
     */
    ANDROID_CREATOR(
            14,
            "ANDROID_CREATOR",
            "com.google.android.apps.youtube.creator",
            "Google",
            "Pixel 9 Pro Fold",
            "Android",
            "15",
            "35",
            "AP3A.241005.015.A2",
            "23.47.101",
            true,
            false,
            true,
            "Android Studio"
    ),
    /**
     * Internal YT client for an unreleased YT client. May stop working at any time.
     */
    VISIONOS(101,
            "VISIONOS",
            "Apple",
            "RealityDevice14,1",
            "visionOS",
            "1.3.21O771",
            "0.1",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15",
            false,
            false,
            true,
            "visionOS"
    );

    /**
     * YouTube
     * <a href="https://github.com/zerodytrash/YouTube-Internal-Clients?tab=readme-ov-file#clients">client type</a>
     */
    public final int id;

    public final String clientName;

    /**
     * App package name.
     */
    @Nullable
    private final String packageName;

    /**
     * Player user-agent.
     */
    public final String userAgent;

    /**
     * Device model, equivalent to {@link Build#MANUFACTURER} (System property: ro.product.vendor.manufacturer)
     */
    public final String deviceMake;

    /**
     * Device model, equivalent to {@link Build#MODEL} (System property: ro.product.vendor.model)
     */
    public final String deviceModel;

    /**
     * Device OS name.
     */
    public final String osName;

    /**
     * Device OS version.
     */
    public final String osVersion;

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
    private final String buildId;

    /**
     * App version.
     */
    public final String clientVersion;

    /**
     * If the client should use authentication if available.
     */
    public final boolean useAuth;

    /**
     * If the client supports multiple audio tracks.
     */
    public final boolean supportsMultiAudioTracks;

    /**
     * If the client should use the player endpoint for stream extraction.
     */
    public final boolean usePlayerEndpoint;

    /**
     * Friendly name displayed in stats for nerds.
     */
    public final String friendlyName;

    /**
     * Android constructor.
     */
    ClientType(int id,
               String clientName,
               @NonNull String packageName,
               String deviceMake,
               String deviceModel,
               String osName,
               String osVersion,
               @NonNull String androidSdkVersion,
               @NonNull String buildId,
               String clientVersion,
               boolean useAuth,
               boolean supportsMultiAudioTracks,
               boolean usePlayerEndpoint,
               String friendlyName) {
        this.id = id;
        this.clientName = clientName;
        this.packageName = packageName;
        this.deviceMake = deviceMake;
        this.deviceModel = deviceModel;
        this.osName = osName;
        this.osVersion = osVersion;
        this.androidSdkVersion = androidSdkVersion;
        this.buildId = buildId;
        this.clientVersion = clientVersion;
        this.useAuth = useAuth;
        this.supportsMultiAudioTracks = supportsMultiAudioTracks;
        this.usePlayerEndpoint = usePlayerEndpoint;
        this.friendlyName = friendlyName;

        Locale defaultLocale = Locale.getDefault();
        this.userAgent = String.format("%s/%s (Linux; U; Android %s; %s; %s; Build/%s)",
                packageName,
                clientVersion,
                osVersion,
                defaultLocale,
                deviceModel,
                buildId
        );
        Logger.printDebug(() -> "userAgent: " + this.userAgent);
    }

    @SuppressWarnings("ConstantLocale")
    ClientType(int id,
               String clientName,
               String deviceMake,
               String deviceModel,
               String osName,
               String osVersion,
               String clientVersion,
               String userAgent,
               boolean useAuth,
               boolean supportsMultiAudioTracks,
               boolean usePlayerEndpoint,
               String friendlyName) {
        this.id = id;
        this.clientName = clientName;
        this.deviceMake = deviceMake;
        this.deviceModel = deviceModel;
        this.osName = osName;
        this.osVersion = osVersion;
        this.clientVersion = clientVersion;
        this.userAgent = userAgent;
        this.useAuth = useAuth;
        this.supportsMultiAudioTracks = supportsMultiAudioTracks;
        this.usePlayerEndpoint = usePlayerEndpoint;
        this.friendlyName = friendlyName;
        this.packageName = null;
        this.androidSdkVersion = null;
        this.buildId = null;
    }
}
