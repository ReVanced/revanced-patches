package app.revanced.extension.youtube.patches;

import app.revanced.extension.shared.Utils;

public class VersionCheckPatch {
    public static final boolean IS_19_17_OR_GREATER;
    public static final boolean IS_19_20_OR_GREATER;
    public static final boolean IS_19_21_OR_GREATER;
    public static final boolean IS_19_26_OR_GREATER;
    public static final boolean IS_19_29_OR_GREATER;
    public static final boolean IS_19_34_OR_GREATER;
    public static final boolean IS_20_07_OR_GREATER;

    static {
        String appVersionName = Utils.getAppVersionName();
        IS_19_17_OR_GREATER = appVersionName.compareTo("19.17.00") >= 0;
        IS_19_20_OR_GREATER = appVersionName.compareTo("19.20.00") >= 0;
        IS_19_21_OR_GREATER = appVersionName.compareTo("19.21.00") >= 0;
        IS_19_26_OR_GREATER = appVersionName.compareTo("19.26.00") >= 0;
        IS_19_29_OR_GREATER = appVersionName.compareTo("19.29.00") >= 0;
        IS_19_34_OR_GREATER = appVersionName.compareTo("19.34.00") >= 0;
        IS_20_07_OR_GREATER = appVersionName.compareTo("20.07.00") >= 0;
    }
}
