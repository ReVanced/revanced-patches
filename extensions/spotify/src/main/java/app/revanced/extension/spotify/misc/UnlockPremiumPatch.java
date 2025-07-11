package app.revanced.extension.spotify.misc;

import app.revanced.ContextMenuItemPlaceholder;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.shared.ComponentFilters.ComponentFilter;
import app.revanced.extension.spotify.shared.ComponentFilters.ResourceIdComponentFilter;
import app.revanced.extension.spotify.shared.ComponentFilters.StringComponentFilter;
import com.spotify.remoteconfig.internal.AccountAttribute;

import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@SuppressWarnings("unused")
public final class UnlockPremiumPatch {

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
            new OverrideAttribute("player-license-v2", "premium"),
            // Disables shuffle being initially enabled when first playing a playlist.
            new OverrideAttribute("shuffle", FALSE),
            // Allows playing any song on-demand, without a shuffled order.
            new OverrideAttribute("on-demand", TRUE),
            // Make sure playing songs is not disabled remotely and playlists show up.
            new OverrideAttribute("streaming", TRUE),
            // Allows adding songs to queue and removes the smart shuffle mode restriction,
            // allowing to pick any of the other modes. Flag is not present in legacy app target.
            new OverrideAttribute("pick-and-shuffle", FALSE),
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
            com.spotify.home.evopage.homeapi.proto.Section.VIDEO_BRAND_AD_FIELD_NUMBER,
            com.spotify.home.evopage.homeapi.proto.Section.IMAGE_BRAND_AD_FIELD_NUMBER
    );

    /**
     * A list of browse sections feature types ids which should be removed. These ids match the ones from the protobuf
     * response which delivers browse sections.
     */
    private static final List<Integer> REMOVED_BROWSE_SECTIONS = List.of(
            com.spotify.browsita.v1.resolved.Section.BRAND_ADS_FIELD_NUMBER
    );

    /**
     * A list of lists which contain component filters that match whether a context menu item should be filtered out.
     * The main approach used is matching context menu items by the id of their title resource.
     */
    private static final List<List<ComponentFilter>> CONTEXT_MENU_ITEMS_COMPONENT_FILTERS = List.of(
            // "Listen to music ad-free" upsell on playlists.
            List.of(new ResourceIdComponentFilter("context_menu_remove_ads", "string")),
            // "Listen to music ad-free" upsell on albums.
            List.of(new ResourceIdComponentFilter("playlist_entity_reinventfree_adsfree_context_menu_item", "string")),
            // "Start a Jam" context menu item, but only filtered if the user does not have premium and the item is
            // being used as a Premium upsell (ad).
            List.of(
                    new ResourceIdComponentFilter("group_session_context_menu_start", "string"),
                    new StringComponentFilter("isPremiumUpsell=true")
            )
    );

    /**
     * Injection point. Override account attributes.
     */
    public static void overrideAttributes(Map<String, AccountAttribute> attributes) {
        try {
            for (OverrideAttribute override : PREMIUM_OVERRIDES) {
                AccountAttribute attribute = attributes.get(override.key);

                if (attribute == null) {
                    if (override.isExpected) {
                        Logger.printException(() -> "Attribute " + override.key + " expected but not found");
                    }
                    continue;
                }

                Object overrideValue = override.overrideValue;
                Object originalValue;
                originalValue = attribute.value_;

                if (overrideValue.equals(originalValue)) {
                    continue;
                }

                Logger.printInfo(() -> "Overriding account attribute " + override.key +
                        " from " + originalValue + " to " + overrideValue);

                attribute.value_ = overrideValue;
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

    private interface FeatureTypeIdProvider<T> {
        int getFeatureTypeId(T section);
    }

    private static <T> void removeSections(
            List<T> sections,
            FeatureTypeIdProvider<T> featureTypeExtractor,
            List<Integer> idsToRemove
    ) {
        try {
            Iterator<T> iterator = sections.iterator();

            while (iterator.hasNext()) {
                T section = iterator.next();
                int featureTypeId = featureTypeExtractor.getFeatureTypeId(section);
                if (idsToRemove.contains(featureTypeId)) {
                    Logger.printInfo(() -> "Removing section with feature type id " + featureTypeId);
                    iterator.remove();
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "removeSections failure", ex);
        }
    }

    /**
     * Injection point. Remove ads sections from home.
     * Depends on patching abstract protobuf list ensureIsMutable method.
     */
    public static void removeHomeSections(List<com.spotify.home.evopage.homeapi.proto.Section> sections) {
        Logger.printInfo(() -> "Removing ads section from home");
        removeSections(
                sections,
                section -> section.featureTypeCase_,
                REMOVED_HOME_SECTIONS
        );
    }

    /**
     * Injection point. Remove ads sections from browse.
     * Depends on patching abstract protobuf list ensureIsMutable method.
     */
    public static void removeBrowseSections(List<com.spotify.browsita.v1.resolved.Section> sections) {
        Logger.printInfo(() -> "Removing ads section from browse");
        removeSections(
                sections,
                section -> section.sectionTypeCase_,
                REMOVED_BROWSE_SECTIONS
        );
    }

    /**
     * Injection point. Returns whether the context menu item is a Premium ad. Used for versions older than
     * "9.0.60.128".
     */
    public static boolean isFilteredContextMenuItem(Object contextMenuItem) {
        if (contextMenuItem == null) {
            return false;
        }

        try {
            String stringifiedContextMenuItem = contextMenuItem.toString();

            for (List<ComponentFilter> componentFilters : CONTEXT_MENU_ITEMS_COMPONENT_FILTERS) {
                boolean allMatch = true;
                StringBuilder matchedFilterRepresentations = new StringBuilder();

                for (int i = 0, filterSize = componentFilters.size(); i < filterSize; i++) {
                    ComponentFilter componentFilter = componentFilters.get(i);

                    if (componentFilter.filterUnavailable()) {
                        Logger.printInfo(() -> "isFilteredContextMenuItem: Filter " +
                                componentFilter.getFilterRepresentation() + " not available, skipping");
                        allMatch = false;
                        break;
                    }

                    if (!stringifiedContextMenuItem.contains(componentFilter.getFilterValue())) {
                        allMatch = false;
                        break;
                    }

                    matchedFilterRepresentations.append(componentFilter.getFilterRepresentation());
                    if (i < filterSize - 1) {
                        matchedFilterRepresentations.append(", ");
                    }
                }

                if (allMatch) {
                    Logger.printInfo(() -> "Filtering context menu item " + stringifiedContextMenuItem +
                            " because the following filters matched: " + matchedFilterRepresentations);
                    return true;
                }
            }
        } catch (Throwable ex) {
            // Catch Throwable as calling toString can cause crashes with wrongfully generated code that throws
            // NoSuchMethod errors.
            Logger.printException(() -> "isFilteredContextMenuItem failure", ex);
        }

        return false;
    }

    /**
     * Injection point. Returns a new list with the context menu items which are a Premium ad filtered.
     * The original list is immutable and cannot be modified without an extra patch.
     * The method fingerprint used to patch ensures we can return a "List" here.
     * ContextMenuItemPlaceholder interface name and getViewModel return value are replaced by a patch to match
     * the minified names used at runtime. Used in newer versions of the app.
     */
    public static List<Object> filterContextMenuItems(List<Object> originalContextMenuItems) {
        try {
            ArrayList<Object> filteredContextMenuItems = new ArrayList<>(originalContextMenuItems.size());

            for (Object contextMenuItem : originalContextMenuItems) {
                if (isFilteredContextMenuItem(((ContextMenuItemPlaceholder) contextMenuItem).getViewModel())) {
                    continue;
                }

                filteredContextMenuItems.add(contextMenuItem);
            }

            return filteredContextMenuItems;
        } catch (Exception ex) {
            Logger.printException(() -> "filterContextMenuItems failure", ex);
        }

        return originalContextMenuItems;
    }
}
