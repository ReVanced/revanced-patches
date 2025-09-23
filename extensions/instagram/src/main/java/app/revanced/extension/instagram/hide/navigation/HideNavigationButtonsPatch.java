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
     * @return the list without the tab
     */
    public static List<Object> removeNavigationTabByName(
            List<Object> navigationButtonsList,
            String buttonNameToRemove,
            String enumNameField
    )
            throws IllegalAccessException, NoSuchFieldException {
        for (Object tab : navigationButtonsList) {
            Field f = tab.getClass().getDeclaredField(enumNameField);
            String currentTabName = (String) f.get(tab);

            if (buttonNameToRemove.equals(currentTabName)) {
                navigationButtonsList.remove(tab);
                break;
            }
        }
        return navigationButtonsList;
    }
}
