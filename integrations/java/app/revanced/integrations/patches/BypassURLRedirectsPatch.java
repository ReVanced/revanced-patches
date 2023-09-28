package app.revanced.integrations.patches;

import android.net.Uri;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class BypassURLRedirectsPatch {
    private static final String YOUTUBE_REDIRECT_PATH = "/redirect";

    /**
     * Convert the YouTube redirect URI string to the redirect query URI.
     *
     * @param uri The YouTube redirect URI string.
     * @return The redirect query URI.
     */
    public static Uri parseRedirectUri(String uri) {
        final var parsed = Uri.parse(uri);

        if (SettingsEnum.BYPASS_URL_REDIRECTS.getBoolean() && parsed.getPath().equals(YOUTUBE_REDIRECT_PATH)) {
            var query = Uri.parse(Uri.decode(parsed.getQueryParameter("q")));

            LogHelper.printDebug(() -> "Bypassing YouTube redirect URI: " + query);

            return query;
        }

        return parsed;
    }
}
