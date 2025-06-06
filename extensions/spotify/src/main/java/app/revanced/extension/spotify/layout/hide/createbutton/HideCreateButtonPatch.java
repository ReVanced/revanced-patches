package app.revanced.extension.spotify.layout.hide.createbutton;

import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.spotify.shared.ComponentFilters.*;

@SuppressWarnings("unused")
public final class HideCreateButtonPatch {

    /**
     * A list of component filters that match whether a navigation bar item is the Create button.
     * The main approach used is matching the resource id for the Create button title.
     */
    private static final List<ComponentFilter> CREATE_BUTTON_COMPONENT_FILTERS = List.of(
            new ResourceIdComponentFilter("navigationbar_musicappitems_create_title", "string"),
            // Temporary fallback and fix for APKs merged with AntiSplit-M not having resources properly encoded,
            // and thus getting the resource identifier for the Create button title always return 0.
            // FIXME: Remove this once the above issue is no longer relevant.
            new StringComponentFilter("spotify:create-menu")
    );

    /**
     * A component filter for the old id of the resource which contained the Create button title.
     * Used in older versions of the app.
     */
    private static final ResourceIdComponentFilter OLD_CREATE_BUTTON_COMPONENT_FILTER =
            new ResourceIdComponentFilter("bottom_navigation_bar_create_tab_title", "string");

    /**
     * Injection point. This method is called on every navigation bar item to check whether it is the Create button.
     * If the navigation bar item is the Create button, it returns null to erase it.
     * The method fingerprint used to patch ensures we can safely return null here.
     */
    public static Object returnNullIfIsCreateButton(Object navigationBarItem) {
        if (navigationBarItem == null) {
            return null;
        }

        String stringifiedNavigationBarItem = navigationBarItem.toString();

        boolean isCreateButton = false;
        String matchedFilterRepresentation = null;

        for (ComponentFilter componentFilter : CREATE_BUTTON_COMPONENT_FILTERS) {
            if (componentFilter.filterUnavailable()) {
                Logger.printInfo(() -> "returnNullIfIsCreateButton: Filter " +
                        componentFilter.getFilterRepresentation() + " not available, skipping");
                continue;
            }

            if (stringifiedNavigationBarItem.contains(componentFilter.getFilterValue())) {
                isCreateButton = true;
                matchedFilterRepresentation = componentFilter.getFilterRepresentation();
            }
        }

        if (isCreateButton) {
            String finalMatchedFilterRepresentation = matchedFilterRepresentation;
            Logger.printInfo(() -> "Hiding Create button because the navigation bar item " + navigationBarItem +
                    " matched the filter " + finalMatchedFilterRepresentation);
            return null;
        }

        return navigationBarItem;
    }

    /**
     * Injection point. Called in older versions of the app. Returns whether the old navigation bar item is the old
     * Create button.
     */
    public static boolean isOldCreateButton(int oldNavigationBarItemTitleResId) {
        // In case the resource id has not been found.
        if (OLD_CREATE_BUTTON_COMPONENT_FILTER.getResourceId() == 0) {
            Logger.printInfo(() -> "Resource id " +  OLD_CREATE_BUTTON_COMPONENT_FILTER.resourceName +
                    " was not found, skipping hiding old Create button");
            return false;
        }

        boolean isCreateButton = oldNavigationBarItemTitleResId == OLD_CREATE_BUTTON_COMPONENT_FILTER.getResourceId();
        if (isCreateButton) {
            Logger.printInfo(() -> "Hiding old Create button because the navigation bar item title resource id" +
                    " matched " + OLD_CREATE_BUTTON_COMPONENT_FILTER.getFilterRepresentation());
            return true;
        }

        return false;
    }
}
