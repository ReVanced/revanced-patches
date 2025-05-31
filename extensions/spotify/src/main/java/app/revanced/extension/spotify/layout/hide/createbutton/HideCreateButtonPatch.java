package app.revanced.extension.spotify.layout.hide.createbutton;

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
     * Injection point. This method is called on every navigation bar item to check whether it is the create button.
     */
    public static boolean isCreateButton(String stringifiedNavBarItem) {
        return CREATE_BUTTON_TITLE_RES_ID_LIST.stream()
                .anyMatch(createButtonTitleRes -> stringifiedNavBarItem.contains(createButtonTitleRes));
    }
}
