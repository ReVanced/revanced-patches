package app.revanced.extension.spotify.layout.hide.createbutton;

import java.util.List;

import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class HideCreateButtonPatch {

    /**
     * A list of ids of resources which contain the Create button title.
     */
    private static final List<String> CREATE_BUTTON_TITLE_RES_ID_LIST = List.of(
            Integer.toString(Utils.getResourceIdentifier("navigationbar_musicappitems_create_title", "string"))
    );

    /**
     * The old id of the resource which contained the Create button title. Used in older versions of the app.
     */
    private static final int OLD_CREATE_BUTTON_TITLE_RES_ID =
            Utils.getResourceIdentifier("bottom_navigation_bar_create_tab_title", "string");

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
        boolean isCreateButton = CREATE_BUTTON_TITLE_RES_ID_LIST.stream()
                .anyMatch(stringifiedNavigationBarItem::contains);

        if (isCreateButton) {
            return null;
        }

        return navigationBarItem;
    }

    /**
     * Injection point. Called in older versions of the app. Returns whether the old navigation bar item is the old
     * Create button.
     */
    public static boolean isOldCreateButton(int oldNavigationBarItemTitleResId) {
        return oldNavigationBarItemTitleResId == OLD_CREATE_BUTTON_TITLE_RES_ID;
    }
}
