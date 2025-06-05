package app.revanced.extension.spotify.misc;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.spotify.home.evopage.homeapi.proto.Section;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class UnlockPremiumPatch {

    private static final String SPOTIFY_MAIN_ACTIVITY_LEGACY = "com.spotify.music.MainActivity";

    /**
     * If the app target is 8.6.98.900.
     */
    private static final boolean IS_SPOTIFY_LEGACY_APP_TARGET;

    static {
        boolean isLegacy;
        try {
            Class.forName(SPOTIFY_MAIN_ACTIVITY_LEGACY);
            isLegacy = true;
        } catch (ClassNotFoundException ex) {
            isLegacy = false;
        }

        IS_SPOTIFY_LEGACY_APP_TARGET = isLegacy;
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

    private static final List<OverrideAttribute> PREMIUM_OVERRIDES = List.of(
            // Disables player and app ads.
            new OverrideAttribute("ads", FALSE),
            // Works along on-demand, allows playing any song without restriction.
            new OverrideAttribute("player-license", "premium"),
            new OverrideAttribute("player-license-v2", "premium", !IS_SPOTIFY_LEGACY_APP_TARGET),
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

    /**
     * A list of home sections feature types ids which should be removed. These ids match the ones from the protobuf
     * response which delivers home sections.
     */
    private static final List<Integer> REMOVED_HOME_SECTIONS = List.of(
            Section.VIDEO_BRAND_AD_FIELD_NUMBER,
            Section.IMAGE_BRAND_AD_FIELD_NUMBER
    );

    /**
     * A list of lists which contain strings that match whether a context menu item should be filtered out.
     * The main approach used is matching context menu items by the id of their text resource.
     */
    private static final List<List<String>> FILTERED_CONTEXT_MENU_ITEMS_BY_STRINGS = List.of(
            // "Listen to music ad-free" upsell on playlists.
            List.of(getResourceIdentifier("context_menu_remove_ads")),
            // "Listen to music ad-free" upsell on albums.
            List.of(getResourceIdentifier("playlist_entity_reinventfree_adsfree_context_menu_item")),
            // "Start a Jam" context menu item, but only filtered if the user does not have premium and the item is
            // being used as a Premium upsell (ad).
            List.of(
                    getResourceIdentifier("group_session_context_menu_start"),
                    "isPremiumUpsell=true"
            )
    );

    /**
     * Utility method for returning resources ids as strings.
     */
    private static String getResourceIdentifier(String resourceIdentifierName) {
        return Integer.toString(Utils.getResourceIdentifier(resourceIdentifierName, "id"));
    }

    /**
     * Injection point. Override account attributes.
     */
    public static void overrideAttributes(Map<String, /*AccountAttribute*/ Object> attributes) {
        try {
            for (OverrideAttribute override : PREMIUM_OVERRIDES) {
                Object attribute = attributes.get(override.key);

                if (attribute == null) {
                    if (override.isExpected) {
                        Logger.printException(() -> "Attribute " + override.key + " expected but not found");
                    }
                    continue;
                }

                Object overrideValue = override.overrideValue;
                Object originalValue;
                if (IS_SPOTIFY_LEGACY_APP_TARGET) {
                    originalValue = ((com.spotify.useraccount.v1.AccountAttribute) attribute).value_;
                } else {
                    originalValue = ((com.spotify.remoteconfig.internal.AccountAttribute) attribute).value_;
                }

                if (overrideValue == originalValue) {
                    continue;
                }

                Logger.printInfo(() -> "Overriding account attribute " + override.key +
                        " from " + originalValue + " to " + overrideValue);

                if (IS_SPOTIFY_LEGACY_APP_TARGET) {
                    ((com.spotify.useraccount.v1.AccountAttribute) attribute).value_ = overrideValue;
                } else {
                    ((com.spotify.remoteconfig.internal.AccountAttribute) attribute).value_ = overrideValue;
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "overrideAttributes failure", ex);
        }
    }

    /**
     * Injection point. Remove station data from Google Assistant URI.
     */
    public static String removeStationString(String spotifyUriOrUrl) {
        try {
            Logger.printInfo(() -> "Removing station string from " + spotifyUriOrUrl);
            return spotifyUriOrUrl.replace("spotify:station:", "spotify:");
        } catch (Exception ex) {
            Logger.printException(() -> "removeStationString failure", ex);
            return spotifyUriOrUrl;
        }
    }

    /**
     * Injection point. Remove ads sections from home.
     * Depends on patching abstract protobuf list ensureIsMutable method.
     */
    public static void removeHomeSections(List<Section> sections) {
        try {
            Iterator<Section> iterator = sections.iterator();

            while (iterator.hasNext()) {
                Section section = iterator.next();
                if (REMOVED_HOME_SECTIONS.contains(section.featureTypeCase_)) {
                    Logger.printInfo(() -> "Removing home section with feature type id " + section.featureTypeCase_);
                    iterator.remove();
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "removeHomeSections failure", ex);
        }
    }

    /**
     * Injection point. Returns whether the context menu item is a Premium ad.
     */
    public static boolean isFilteredContextMenuItem(Object contextMenuItem) {
        if (contextMenuItem == null) {
            return false;
        }

        String stringifiedContextMenuItem = contextMenuItem.toString();
        for (List<String> stringList : FILTERED_CONTEXT_MENU_ITEMS_BY_STRINGS) {
            boolean allMatch = true;
            StringBuilder matchedStrings = new StringBuilder();

            for (int i = 0; i < stringList.size(); i++) {
                String string = stringList.get(i);

                // In case the string is a resource id, and it has not been found.
                if (string.equals("0")) {
                    continue;
                }

                if (!stringifiedContextMenuItem.contains(string)) {
                    allMatch = false;
                    break;
                }

                matchedStrings.append(string);
                if (i < stringList.size() - 1) {
                    matchedStrings.append(", ");
                }
            }

            if (allMatch) {
                Logger.printInfo(() -> "Filtering context menu item " + stringifiedContextMenuItem +
                        " because the following strings matched: " + matchedStrings);
                return true;
            }
        }

        return false;
    }
}
