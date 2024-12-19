package app.revanced.extension.youtube.patches;

import app.revanced.extension.shared.Utils;

public class VersionCheckPatch {
    public static final boolean IS_19_17_OR_GREATER = Utils.getAppVersionName().compareTo("19.17.00") >= 0;
    public static final boolean IS_19_20_OR_GREATER = Utils.getAppVersionName().compareTo("19.20.00") >= 0;
    public static final boolean IS_19_21_OR_GREATER = Utils.getAppVersionName().compareTo("19.21.00") >= 0;
    public static final boolean IS_19_26_OR_GREATER = Utils.getAppVersionName().compareTo("19.26.00") >= 0;
    public static final boolean IS_19_29_OR_GREATER = Utils.getAppVersionName().compareTo("19.29.00") >= 0;
    public static final boolean IS_19_34_OR_GREATER = Utils.getAppVersionName().compareTo("19.34.00") >= 0;
    public static final boolean IS_19_46_OR_GREATER = Utils.getAppVersionName().compareTo("19.46.00") >= 0;
}
