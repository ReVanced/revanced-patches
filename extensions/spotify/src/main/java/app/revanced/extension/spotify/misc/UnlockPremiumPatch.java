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

    /**
     * If the app is target is not 8.6.98.900.
     */
    private static final boolean USING_NON_LEGACY_APP_TARGET;
    static {
        boolean nonLegacy;
        try {
            Class.forName("com.spotify.remoteconfig.internal.AccountAttribute");
            nonLegacy = false;
        } catch (ClassNotFoundException ex) {
            nonLegacy = true;
        }

        USING_NON_LEGACY_APP_TARGET = nonLegacy;
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
            new OverrideAttribute("pick-and-shuffle", FALSE, !USING_NON_LEGACY_APP_TARGET),
            // Disables shuffle-mode streaming-rule, which forces songs to be played shuffled
            // and breaks the player when other patches are applied.
            new OverrideAttribute("streaming-rules", ""),
            // Enables premium UI in settings and removes the premium button in the nav-bar.
            new OverrideAttribute("nft-disabled", "1"),
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
     * Override attributes injection point.
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
                    if (USING_NON_LEGACY_APP_TARGET) {
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
     * Remove ads sections from home injection point.
     */
    public static void removeHomeSections(List<Section> sections) {
        try {
            sections.removeIf(section -> REMOVED_HOME_SECTIONS.contains(section.featureTypeCase_));
        } catch (Exception ex) {
            Logger.printException(() -> "Remove home sections failure", ex);
        }
    }
}
