package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class RemoveTrackingQueryParameterPatch {
    private static final String NEW_TRACKING_PARAMETER_REGEX = ".si=.+";
    private static final String OLD_TRACKING_PARAMETER_REGEX = ".feature=.+";

    public static String sanitize(String url) {
        if (!Settings.REMOVE_TRACKING_QUERY_PARAMETER.get()) return url;

        return url
                .replaceAll(NEW_TRACKING_PARAMETER_REGEX, "")
                .replaceAll(OLD_TRACKING_PARAMETER_REGEX, "");
    }
}
