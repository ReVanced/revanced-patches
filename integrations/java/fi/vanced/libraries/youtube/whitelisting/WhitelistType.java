package fi.vanced.libraries.youtube.whitelisting;

public enum WhitelistType {
    ADS("Ads", "vanced_whitelist_ads_enabled"),
    SPONSORBLOCK("SponsorBlock", "vanced_whitelist_sb_enabled");

    private final String friendlyName;
    private final String preferencesName;
    private final String preferenceEnabledName;

    WhitelistType(String friendlyName, String preferenceEnabledName) {
        this.friendlyName = friendlyName;
        this.preferencesName = "whitelist_" + name();
        this.preferenceEnabledName = preferenceEnabledName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getPreferencesName() {
        return preferencesName;
    }

    public String getPreferenceEnabledName() {
        return preferenceEnabledName;
    }
}