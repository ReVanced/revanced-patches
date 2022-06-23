package app.revanced.integrations.adremover.whitelist;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import app.revanced.integrations.utils.SharedPrefHelper;

public enum WhitelistType {
    ADS(SharedPrefHelper.SharedPrefNames.YOUTUBE, "vanced_whitelist_ads_enabled"),
    SPONSORBLOCK(SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, "vanced_whitelist_sb_enabled");

    private final String friendlyName;
    private final String preferencesName;
    private final String preferenceEnabledName;
    private final SharedPrefHelper.SharedPrefNames name;

    WhitelistType(SharedPrefHelper.SharedPrefNames name, String preferenceEnabledName) {
        this.friendlyName = str("vanced_whitelisting_" + name().toLowerCase());
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