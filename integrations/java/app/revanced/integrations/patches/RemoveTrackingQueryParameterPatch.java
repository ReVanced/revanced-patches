package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class RemoveTrackingQueryParameterPatch {
    private static final String NEW_TRACKING_PARAMETER_REGEX = ".si=.+";
    private static final String OLD_TRACKING_PARAMETER_REGEX = ".feature=.+";

    public static String sanitize(String url) {
        if (!SettingsEnum.REMOVE_TRACKING_QUERY_PARAMETER.getBoolean()) return url;

        return url
                .replaceAll(NEW_TRACKING_PARAMETER_REGEX, "")
                .replaceAll(OLD_TRACKING_PARAMETER_REGEX, "");
    }
}
