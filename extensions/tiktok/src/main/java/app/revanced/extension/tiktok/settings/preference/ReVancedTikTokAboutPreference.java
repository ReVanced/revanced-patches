package app.revanced.extension.tiktok.settings.preference;

import android.content.Context;
import android.view.View;

import java.util.Map;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.preference.ReVancedAboutPreference;
import app.revanced.extension.tiktok.Utils;

@SuppressWarnings("deprecation")
public class ReVancedTikTokAboutPreference extends ReVancedAboutPreference {

    /**
     * Because resources cannot be added to TikTok,
     * these strings are copied from the shared strings.xml file.
     * <p>
     * Changes here must also be made in strings.xml
     */
    private final Map<String, String> aboutStrings = Map.of(
            "revanced_settings_about_links_body", "You are using ReVanced Patches version <i>%s</i>",
            "revanced_settings_about_links_dev_header", "Note",
            "revanced_settings_about_links_dev_body", "This version is a pre-release and you may experience unexpected issues",
            "revanced_settings_about_links_header", "Official links"
    );

    public ReVancedTikTokAboutPreference(Context context) {
        super(context);

        setTitle("About");
        setSummary("About ReVanced");
    }

    @Override
    protected String getString(String key, Object ... args) {
        String format = aboutStrings.get(key);

        if (format == null) {
            Logger.printException(() -> "Unknown key: " + key);
            return "";
        }

        return String.format(format, args);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Utils.setTitleAndSummaryColor(view);
    }
}
