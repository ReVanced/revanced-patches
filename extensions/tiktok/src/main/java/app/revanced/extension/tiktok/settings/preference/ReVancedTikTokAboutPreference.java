package app.revanced.extension.tiktok.settings.preference;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Map;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.preference.ReVancedAboutPreference;

public class ReVancedTikTokAboutPreference extends ReVancedAboutPreference {

    /**
     * Because resources cannot be added to TikTok,
     * these strings are copied from the shared strings.xml file.
     *
     * Changes here must also be made in strings.xml
     */
    private final Map<String, String> aboutStrings = Map.of(
            "revanced_settings_about_links_body", "You are using ReVanced Patches version <i>%s</i>",
            "revanced_settings_about_links_dev_header", "Note",
            "revanced_settings_about_links_dev_body", "This version is a pre-release and you may experience unexpected issues",
            "revanced_settings_about_links_header", "Official links"
    );

    {
        //noinspection deprecation
        setTitle("About");
    }

    public ReVancedTikTokAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ReVancedTikTokAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ReVancedTikTokAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ReVancedTikTokAboutPreference(Context context) {
        super(context);
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
}
