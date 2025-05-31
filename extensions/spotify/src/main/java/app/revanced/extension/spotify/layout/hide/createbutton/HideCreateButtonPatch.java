package app.revanced.extension.spotify.layout.hide.createbutton;

import java.util.List;
import java.util.LinkedHashSet;

import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class HideCreateButtonPatch {

    /**
     * A list of ids of resources which contain the Create button title.
     */
    private static final List<String> CREATE_BUTTON_TITLE_RES_ID_LIST = List.of(
            // Resource which is currently used by latest versions.
            Integer.toString(Utils.getResourceIdentifier("navigationbar_musicappitems_create_title", "string"))
    );

    /**
     * Injection point. This method is called on every navigation bar item to check whether it is the Create button.
     * If the navigation bar item is the Create button, it returns null to erase it.
     * The method fingerprint used to patch ensures we can safely return null here.
     */
    public static Object returnNullIfIsCreateButton(Object navigationBarItem) {
        if (navigationBarItem == null) {
            return navigationBarItem;
        }

        String stringifiedNavigationBarItem = navigationBarItem.toString();
        boolean isCreateButton = CREATE_BUTTON_TITLE_RES_ID_LIST.stream()
                .anyMatch(createButtonTitleRes -> stringifiedNavigationBarItem.contains(createButtonTitleRes));

        if (isCreateButton) {
            return null;
        }

        return navigationBarItem;
    }
}
