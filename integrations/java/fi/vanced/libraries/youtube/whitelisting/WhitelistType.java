package fi.vanced.libraries.youtube.whitelisting;

public enum WhitelistType {
    ADS("vanced_whitelist_ads_enabled"),
    SPONSORBLOCK("vanced_whitelist_sb_enabled");

    private final String preferencesName;
    private final String preferenceEnabledName;

    WhitelistType(String preferenceEnabledName) {
        this.preferencesName = "whitelist_" + name();
        this.preferenceEnabledName = preferenceEnabledName;
    }

    public String getPreferencesName() {
        return preferencesName;
    }

    public String getPreferenceEnabledName() {
        return preferenceEnabledName;
    }
}