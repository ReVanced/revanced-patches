package app.revanced.integrations.patches;

import android.net.Uri;
import app.revanced.integrations.settings.SettingsEnum;

public class OpenLinksDirectlyPatch {
    private static final String YOUTUBE_REDIRECT_PATH = "redirect";

    /**
     * Parses the given YouTube redirect uri by extracting the redirect query.
     *
     * @param uri The YouTube redirect uri.
     * @return The redirect query.
     */
    public static Uri parseRedirectUri(String uri) {
        if (SettingsEnum.OPEN_LINKS_DIRECTLY.getBoolean()) {
            final var parsed = Uri.parse(uri);

            if (parsed.getPath().equals(YOUTUBE_REDIRECT_PATH))
                Uri.parse(parsed.getQueryParameter("q"));
        }

        return Uri.parse(uri);
    }
}
