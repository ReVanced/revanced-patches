package app.revanced.extension.instagram.hide.navigation;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings("unused")
public class HideNavigationButtonsPatch {

    /**
     * Injection point.
     * @param navigationTabList the list of navigation tabs, as an (obfuscated) Enum type
     * @param tabNameToRemove the name of the tab we want to remove
     * @param enumNameField the field in the nav button enum class which contains the name of the button
     * @return the list without the tab
     */
    public static List<Object> removeNavigationTabByName(
            List<Object> navigationTabList,
            String tabNameToRemove,
            String enumNameField
    )
            throws IllegalAccessException, NoSuchFieldException {
        for (Object tab : navigationTabList) {
            Field f = tab.getClass().getDeclaredField(enumNameField);
            String currentTabName = (String) f.get(tab);

            if (tabNameToRemove.equals(currentTabName)) {
                navigationTabList.remove(tab);
                break;
            }
        }
        return navigationTabList;
    }
}
