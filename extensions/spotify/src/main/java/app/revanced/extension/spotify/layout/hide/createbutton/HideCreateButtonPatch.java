package app.revanced.extension.spotify.layout.hide.createbutton;

import java.util.LinkedHashSet;

import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class HideCreateButtonPatch {

    private static final String CREATE_BUTTON_TITLE_RES =
            String.valueOf(Utils.getResourceIdentifier("navigationbar_musicappitems_create_title", "string"));

    /**
     * Injection point.
     */
    public static boolean isCreateButton(String stringifiedNavBarItem) {
        return stringifiedNavBarItem.contains(CREATE_BUTTON_TITLE_RES);
    }
}
