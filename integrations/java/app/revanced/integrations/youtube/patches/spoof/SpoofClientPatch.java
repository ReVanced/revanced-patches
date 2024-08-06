package app.revanced.integrations.youtube.patches.spoof;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.Uri;
import android.os.Build;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.patches.BackgroundPlaybackPatch;
import app.revanced.integrations.youtube.settings.Settings;
import org.chromium.net.ExperimentalUrlRequest;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static final boolean SPOOF_CLIENT_ENABLED = Settings.SPOOF_CLIENT.get();
    private static final ClientType SPOOF_CLIENT_TYPE = Settings.SPOOF_CLIENT_USE_IOS.get() ? ClientType.IOS : ClientType.ANDROID_VR;
    private static final boolean SPOOFING_TO_IOS = SPOOF_CLIENT_ENABLED && SPOOF_CLIENT_TYPE == ClientType.IOS;

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URI_STRING = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URI_STRING);

    /**
     * Injection point.
     * Blocks /get_watch requests by returning an unreachable URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return An unreachable URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        if (SPOOF_CLIENT_ENABLED) {
            try {
                String path = playerRequestUri.getPath();

                if (path != null && path.contains("get_watch")) {
                    Logger.printDebug(() -> "Blocking 'get_watch' by returning unreachable uri");

                    return UNREACHABLE_HOST_URI;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "blockGetWatchRequest failure", ex);
            }
        }

        return playerRequestUri;
    }

    /**
     * Injection point.
     * <p>
     * Blocks /initplayback requests.
     */
    public static String blockInitPlaybackRequest(String originalUrlString) {
        if (SPOOF_CLIENT_ENABLED) {
            try {
                var originalUri = Uri.parse(originalUrlString);
                String path = originalUri.getPath();

                if (path != null && path.contains("initplayback")) {
                    Logger.printDebug(() -> "Blocking 'initplayback' by returning unreachable url");

                    return UNREACHABLE_HOST_URI_STRING;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "blockInitPlaybackRequest failure", ex);
            }
        }

        return originalUrlString;
    }

    /**
     * Injection point.
     */
    public static int getClientTypeId(int originalClientTypeId) {
        return SPOOF_CLIENT_ENABLED ? SPOOF_CLIENT_TYPE.id : originalClientTypeId;
    }

    /**
     * Injection point.
     */
    public static String getClientVersion(String originalClientVersion) {
        return SPOOF_CLIENT_ENABLED ? SPOOF_CLIENT_TYPE.version : originalClientVersion;
    }

    /**
     * Injection point.
     */
    public static String getClientModel(String originalClientModel) {
        return SPOOF_CLIENT_ENABLED ? SPOOF_CLIENT_TYPE.model : originalClientModel;
    }

    /**
     * Injection point.
     * Fix video qualities missing, if spoofing to iOS by using the correct client OS version.
     */
    public static String getOsVersion(String originalOsVersion) {
        return SPOOFING_TO_IOS ? ClientType.IOS.osVersion : originalOsVersion;
    }

    /**
     * Injection point.
     */
    public static boolean enablePlayerGesture(boolean original) {
        return SPOOF_CLIENT_ENABLED || original;
    }

    /**
     * Injection point.
     */
    public static boolean isClientSpoofingEnabled() {
        return SPOOF_CLIENT_ENABLED;
    }

    /**
     * Injection point.
     * When spoofing the client to iOS, the playback speed menu is missing from the player response.
     * Return true to force create the playback speed menu.
     */
    public static boolean forceCreatePlaybackSpeedMenu(boolean original) {
        return SPOOFING_TO_IOS || original;
    }

    /**
     * Injection point.
     * When spoofing the client to iOS, background audio only playback of livestreams fails.
     * Return true to force enable audio background play.
     */
    public static boolean overrideBackgroundAudioPlayback() {
        return SPOOFING_TO_IOS && BackgroundPlaybackPatch.playbackIsNotShort();
    }

    /**
     * Injection point.
     * Fix video qualities missing, if spoofing to iOS by using the correct iOS user-agent.
     */
    public static ExperimentalUrlRequest overrideUserAgent(ExperimentalUrlRequest.Builder builder, String url) {
        if (SPOOFING_TO_IOS) {
            String path = Uri.parse(url).getPath();
            if (path != null && path.contains("player")) {
                return builder.addHeader("User-Agent", ClientType.IOS.userAgent).build();
            }
        }

        return builder.build();
    }

    private enum ClientType {
        // https://dumps.tadiphone.dev/dumps/oculus/eureka
        ANDROID_VR(28,
                "Quest 3",
                "1.56.21",
                "12",
                "com.google.android.apps.youtube.vr.oculus/1.56.21 (Linux; U; Android 12; GB) gzip"
        ),
        // 11,4 = iPhone XS Max.
        // 16,2 = iPhone 15 Pro Max.
        // Since the 15 supports AV1 hardware decoding, only spoof that device if this
        // Android device also has hardware decoding.
        //
        // Version number should be a valid iOS release.
        // https://www.ipa4fun.com/history/185230
        IOS(5,
                deviceHasAV1HardwareDecoding() ? "iPhone16,2" : "iPhone11,4",
                "19.10.7",
                "17.5.1.21F90",
                "com.google.ios.youtube/19.10.7 (iPhone; U; CPU iOS 17_5_1 like Mac OS X)"
        );

        /**
         * YouTube
         * <a href="https://github.com/zerodytrash/YouTube-Internal-Clients?tab=readme-ov-file#clients">client type</a>
         */
        final int id;

        /**
         * Device model, equivalent to {@link Build#MODEL} (System property: ro.product.model)
         */
        final String model;

        /**
         * App version.
         */
        final String version;

        /**
         * Device OS version.
         */
        final String osVersion;

        /**
         * Player user-agent.
         */
        final String userAgent;

        ClientType(int id, String model, String version, String osVersion, String userAgent) {
            this.id = id;
            this.model = model;
            this.version = version;
            this.osVersion = osVersion;
            this.userAgent = userAgent;
        }
    }

    private static boolean deviceHasAV1HardwareDecoding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);

            for (MediaCodecInfo codecInfo : codecList.getCodecInfos()) {
                if (codecInfo.isHardwareAccelerated() && !codecInfo.isEncoder()) {
                    String[] supportedTypes = codecInfo.getSupportedTypes();
                    for (String type : supportedTypes) {
                        if (type.equalsIgnoreCase("video/av01")) {
                            MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                            if (capabilities != null) {
                                Logger.printDebug(() -> "Device supports AV1 hardware decoding.");
                                return true;
                            }
                        }
                    }
                }
            }
        }

        Logger.printDebug(() -> "Device does not support AV1 hardware decoding.");
        return false;
    }
}
