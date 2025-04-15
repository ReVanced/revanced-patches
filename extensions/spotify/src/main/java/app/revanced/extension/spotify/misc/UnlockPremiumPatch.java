package app.revanced.extension.spotify.misc;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.spotify.home.evopage.homeapi.proto.Section;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public final class UnlockPremiumPatch {

    private static final String SPOTIFY_MAIN_ACTIVITY_LEGACY = "com.spotify.music.MainActivity";

    /**
     * If the app target is 8.6.98.900.
     */
    private static final boolean IS_SPOTIFY_LEGACY_APP_TARGET;

    static {
        boolean legacy;
        try {
            Class.forName(SPOTIFY_MAIN_ACTIVITY_LEGACY);
            legacy = true;
        } catch (ClassNotFoundException ex) {
            legacy = false;
        }

        IS_SPOTIFY_LEGACY_APP_TARGET = legacy;
    }

    private static class OverrideAttribute {
        /**
         * Account attribute key.
         */
        final String key;

        /**
         * Override value.
         */
        final Object overrideValue;

        /**
         * If this attribute is expected to be present in all situations.
         * If false, then no error is raised if the attribute is missing.
         */
        final boolean isExpected;

        OverrideAttribute(String key, Object overrideValue) {
            this(key, overrideValue, true);
        }

        OverrideAttribute(String key, Object overrideValue, boolean isExpected) {
            this.key = Objects.requireNonNull(key);
            this.overrideValue = Objects.requireNonNull(overrideValue);
            this.isExpected = isExpected;
        }
    }

    private static final List<OverrideAttribute> OVERRIDES = List.of(
            // Disables player and app ads.
            new OverrideAttribute("ads", FALSE),
            // Works along on-demand, allows playing any song without restriction.
            new OverrideAttribute("player-license", "premium"),
            // Disables shuffle being initially enabled when first playing a playlist.
            new OverrideAttribute("shuffle", FALSE),
            // Allows playing any song on-demand, without a shuffled order.
            new OverrideAttribute("on-demand", TRUE),
            // Make sure playing songs is not disabled remotely and playlists show up.
            new OverrideAttribute("streaming", TRUE),
            // Allows adding songs to queue and removes the smart shuffle mode restriction,
            // allowing to pick any of the other modes. Flag is not present in legacy app target.
            new OverrideAttribute("pick-and-shuffle", FALSE, !IS_SPOTIFY_LEGACY_APP_TARGET),
            // Disables shuffle-mode streaming-rule, which forces songs to be played shuffled
            // and breaks the player when other patches are applied.
            new OverrideAttribute("streaming-rules", ""),
            // Enables premium UI in settings and removes the premium button in the nav-bar.
            new OverrideAttribute("nft-disabled", "1"),
            // Enable Spotify Connect and disable other premium related UI, like buying premium.
            // It also removes the download button.
            new OverrideAttribute("type", "premium"),
            // Enable Spotify Car Thing hardware device.
            // Device is discontinued and no longer works with the latest releases,
            // but it might still work with older app targets.
            new OverrideAttribute("can_use_superbird", TRUE, false),
            // Removes the premium button in the nav-bar for tablet users.
            new OverrideAttribute("tablet-free", FALSE, false)
    );

    private static final List<Integer> REMOVED_HOME_SECTIONS = List.of(
            Section.VIDEO_BRAND_AD_FIELD_NUMBER,
            Section.IMAGE_BRAND_AD_FIELD_NUMBER
    );

    /**
     * Injection point. Override account attributes.
     */
    public static void overrideAttribute(Map<String, /*AccountAttribute*/ Object> attributes) {
        try {
            for (var override : OVERRIDES) {
                var attribute = attributes.get(override.key);
                if (attribute == null) {
                    if (override.isExpected) {
                        Logger.printException(() -> "'" + override.key + "' expected but not found");
                    }
                } else {
                    Object overrideValue = override.overrideValue;
                    if (IS_SPOTIFY_LEGACY_APP_TARGET) {
                        ((com.spotify.useraccount.v1.AccountAttribute) attribute).value_ = overrideValue;
                    } else {
                        ((com.spotify.remoteconfig.internal.AccountAttribute) attribute).value_ = overrideValue;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "overrideAttribute failure", ex);
        }
    }

    /**
     * Injection point. Remove station data from Google assistant URI.
     */
    public static String removeStationString(String spotifyUriOrUrl) {
        return spotifyUriOrUrl.replace("spotify:station:", "spotify:");
    }

    /**
     * Injection point. Remove ads sections from home.
     */
    public static void removeHomeSections(List<Section> sections) {
        try {
            sections.removeIf(section -> REMOVED_HOME_SECTIONS.contains(section.featureTypeCase_));
        } catch (Exception ex) {
            Logger.printException(() -> "Remove home sections failure", ex);
        }
    }
}
