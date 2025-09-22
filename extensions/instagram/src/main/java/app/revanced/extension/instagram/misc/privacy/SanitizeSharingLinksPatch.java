package app.revanced.extension.instagram.misc.privacy;

import android.net.Uri;
import app.revanced.extension.shared.Logger;

import java.util.List;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch {

    /**
     * Parameters that are considered undesirable and should be stripped away.
     */
    private static final List<String> SHARE_PARAMETERS_TO_REMOVE = List.of(
            "si", // Share tracking parameter.
            "utm_source" // Share source, such as "copy-link".
    );

    /**
     * Injection point.
     */
    public static void sanitizeUrl(String url, String str2) {
            Logger.printInfo(() -> "String1 " + url + " String2 " + str2);
    }
}
