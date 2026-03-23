package app.revanced.extension.instagram.misc.download;

@SuppressWarnings("unused")
public class DownloadMediaPatch {

    /**
     * Injection point.
     * Always return true to allow downloading all media regardless of creator settings.
     */
    public static boolean isDownloadAllowed() {
        return true;
    }

    /**
     * Injection point.
     * Force third-party downloads to be enabled.
     */
    public static int forceThirdPartyDownloadsEnabled(int originalValue) {
        return 1;
    }
}
