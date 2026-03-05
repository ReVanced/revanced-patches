package app.revanced.extension.youtube.patches;

import app.revanced.extension.shared.Utils;

public class VersionCheckPatch {
    private static boolean isVersionOrGreater(String version) {
        return Utils.getAppVersionName().compareTo(version) >= 0;
    }

    public static final boolean IS_20_21_OR_GREATER = isVersionOrGreater("20.21.00");

    public static final boolean IS_20_22_OR_GREATER = isVersionOrGreater("20.22.00");

    public static final boolean IS_20_31_OR_GREATER = isVersionOrGreater("20.31.00");

    public static final boolean IS_20_37_OR_GREATER = isVersionOrGreater("20.37.00");
}
