package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.shared.Utils;

public class VersionCheckPatch {
    public static final boolean IS_19_20_OR_GREATER = Utils.getAppVersionName().compareTo("19.20.00") >= 0;
    public static final boolean IS_19_21_OR_GREATER = Utils.getAppVersionName().compareTo("19.21.00") >= 0;
    public static final boolean IS_19_26_OR_GREATER = Utils.getAppVersionName().compareTo("19.26.00") >= 0;
    public static final boolean IS_19_29_OR_GREATER = Utils.getAppVersionName().compareTo("19.29.00") >= 0;
}
