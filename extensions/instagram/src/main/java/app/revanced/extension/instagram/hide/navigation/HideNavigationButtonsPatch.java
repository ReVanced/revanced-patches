package app.revanced.extension.instagram.hide.navigation;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings("unused")
public class HideNavigationButtonsPatch {

    /**
     * Injection point.
     * @param navigationButtonsList the list of navigation buttons, as an (obfuscated) Enum type
     * @param buttonNameToRemove the name of the button we want to remove
     * @param enumNameField the field in the nav button enum class which contains the name of the button
     * @return the patched list of navigation buttons
     */
    public static List<Object> removeNavigationButtonByName(
            List<Object> navigationButtonsList,
            String buttonNameToRemove,
            String enumNameField
    )
            throws IllegalAccessException, NoSuchFieldException {
        for (Object button : navigationButtonsList) {
            Field f = button.getClass().getDeclaredField(enumNameField);
            String currentButtonEnumName = (String) f.get(button);

            if (buttonNameToRemove.equals(currentButtonEnumName)) {
                navigationButtonsList.remove(button);
                break;
            }
        }
        return navigationButtonsList;
    }
}
