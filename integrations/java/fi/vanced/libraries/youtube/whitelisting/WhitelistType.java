package fi.vanced.libraries.youtube.whitelisting;

import pl.jakubweg.SponsorBlockSettings;

public enum WhitelistType {
    ADS("Ads", "youtube", "vanced_whitelist_ads_enabled"),
    SPONSORBLOCK("SponsorBlock", SponsorBlockSettings.PREFERENCES_NAME, "vanced_whitelist_sb_enabled");

    private final String friendlyName;
    private final String preferencesName;
    private final String sharedPreferencesName;
    private final String preferenceEnabledName;

    WhitelistType(String friendlyName, String sharedPreferencesName, String preferenceEnabledName) {
        this.friendlyName = friendlyName;
        this.sharedPreferencesName = sharedPreferencesName;
        this.preferencesName = "whitelist_" + name();
        this.preferenceEnabledName = preferenceEnabledName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getSharedPreferencesName() {
        return sharedPreferencesName;
    }

    public String getPreferencesName() {
        return preferencesName;
    }

    public String getPreferenceEnabledName() {
        return preferenceEnabledName;
    }
}