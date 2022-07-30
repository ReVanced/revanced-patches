package app.revanced.integrations.whitelist;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.SharedPrefHelper;

public enum WhitelistType {
    ADS(SharedPrefHelper.SharedPrefNames.YOUTUBE, SettingsEnum.ENABLE_WHITELIST.getPath()),
    SPONSORBLOCK(SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, "revanced_whitelist_sb_enabled");

    private final String friendlyName;
    private final String preferencesName;
    private final String preferenceEnabledName;
    private final SharedPrefHelper.SharedPrefNames name;

    WhitelistType(SharedPrefHelper.SharedPrefNames name, String preferenceEnabledName) {
        this.friendlyName = str("revanced_whitelisting_" + name().toLowerCase());
        this.name = name;
        this.preferencesName = "whitelist_" + name();
        this.preferenceEnabledName = preferenceEnabledName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public SharedPrefHelper.SharedPrefNames getSharedPreferencesName() {
        return name;
    }

    public String getPreferencesName() {
        return preferencesName;
    }

    public String getPreferenceEnabledName() {
        return preferenceEnabledName;
    }
}