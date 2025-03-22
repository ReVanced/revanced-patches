package app.revanced.extension.youtube.patches;

import app.revanced.extension.shared.Utils;

public class VersionCheckPatch {
    private static boolean isVersionOrGreater(String version) {
        return Utils.getAppVersionName().compareTo(version) >= 0;
    }

    public static final boolean IS_19_17_OR_GREATER = isVersionOrGreater("19.17.00");
    public static final boolean IS_19_20_OR_GREATER = isVersionOrGreater("19.20.00");
    public static final boolean IS_19_21_OR_GREATER = isVersionOrGreater("19.21.00");
    public static final boolean IS_19_26_OR_GREATER = isVersionOrGreater("19.26.00");
    public static final boolean IS_19_29_OR_GREATER = isVersionOrGreater("19.29.00");
    public static final boolean IS_19_34_OR_GREATER = isVersionOrGreater("19.34.00");
    public static final boolean IS_20_07_OR_GREATER = isVersionOrGreater("20.07.00");
}
