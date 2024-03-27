package app.revanced.integrations.youtube.patches;

import static app.revanced.integrations.youtube.shared.NavigationBar.NavigationButton;

import android.view.View;

import java.util.EnumMap;
import java.util.Map;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class NavigationButtonsPatch {

    private static final Map<NavigationButton, Boolean> shouldHideMap = new EnumMap<>(NavigationButton.class) {
        {
            put(NavigationButton.HOME, Settings.HIDE_HOME_BUTTON.get());
            put(NavigationButton.CREATE, Settings.HIDE_CREATE_BUTTON.get());
            put(NavigationButton.SHORTS, Settings.HIDE_SHORTS_BUTTON.get());
        }
    };

    private static final Boolean SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON
            = Settings.SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON.get();

    /**
     * Injection point.
     */
    public static boolean switchCreateWithNotificationButton() {
        return SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON;
    }

    /**
     * Injection point.
     */
    public static void navigationTabCreated(NavigationButton button, View tabView) {
        if (Boolean.TRUE.equals(shouldHideMap.get(button))) {
            tabView.setVisibility(View.GONE);
        }
    }
}
