package app.revanced.extension.music;

import app.revanced.extension.shared.Utils;

public class VersionCheckUtils {
    private static boolean isVersionOrGreater(String version) {
        return Utils.getAppVersionName().compareTo(version) >= 0;
    }

    public static final boolean IS_8_40_OR_GREATER = isVersionOrGreater("8.40.00");
}

