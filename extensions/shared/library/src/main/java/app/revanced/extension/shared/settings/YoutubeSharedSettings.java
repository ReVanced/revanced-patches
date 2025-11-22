package app.revanced.extension.shared.settings;

import static app.revanced.extension.shared.settings.Setting.parent;
import static java.lang.Boolean.FALSE;

public class YoutubeSharedSettings extends BaseSettings {
    // Custom filter
    public static final BooleanSetting CUSTOM_FILTER = new BooleanSetting("revanced_custom_filter", FALSE);
    public static final StringSetting CUSTOM_FILTER_STRINGS = new StringSetting("revanced_custom_filter_strings", "", true, parent(CUSTOM_FILTER));

    // TODO: Add debug settings
}
