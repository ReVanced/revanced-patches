package app.revanced.extension.instagram.hide.navigation;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class HideNavigationButtonsPatch {

    /**
     * Injection point.
     *
     */
    public static List<Object> removeNavigationTabByName(List<Object> tabs, String tabNameToRemove) throws IllegalAccessException, NoSuchFieldException {
        Iterator<Object> it = tabs.iterator();

        while (it.hasNext()) {
            Object el = it.next();

            Field f = el.getClass().getDeclaredField("A04");
            String currentTabName = (String) f.get(el);

            if (tabNameToRemove.equals(currentTabName)) {
                it.remove();
            }
        }
        return tabs;
    }
}
