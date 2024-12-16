package app.revanced.extension.youtube.patches;

import static app.revanced.extension.shared.Utils.hideViewUnderCondition;
import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.os.Build;
import android.view.View;

import java.util.EnumMap;
import java.util.Map;

import android.widget.TextView;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class NavigationButtonsPatch {

    private static final Map<NavigationButton, Boolean> shouldHideMap = new EnumMap<>(NavigationButton.class) {
        {
            put(NavigationButton.HOME, Settings.HIDE_HOME_BUTTON.get());
            put(NavigationButton.CREATE, Settings.HIDE_CREATE_BUTTON.get());
            put(NavigationButton.SHORTS, Settings.HIDE_SHORTS_BUTTON.get());
            put(NavigationButton.SUBSCRIPTIONS, Settings.HIDE_SUBSCRIPTIONS_BUTTON.get());
        }
    };

    private static final boolean SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON
            = Settings.SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON.get();

    private static final Boolean TRANSLUCENT_STATUS_BAR
            = Settings.TRANSLUCENT_STATUS_BAR.get();

    private static final Boolean TRANSLUCENT_NAVIGATION_BUTTONS
            = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && Settings.TRANSLUCENT_NAVIGATION_BUTTONS.get();

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

    /**
     * Injection point.
     */
    public static void hideNavigationButtonLabels(TextView navigationLabelsView) {
        hideViewUnderCondition(Settings.HIDE_NAVIGATION_BUTTON_LABELS, navigationLabelsView);
    }

    /**
     * Injection point.
     */
    public static boolean useTranslucentNavigationStatusBar(boolean original) {
        return TRANSLUCENT_STATUS_BAR;
    }

    /**
     * Injection point.
     */
    public static boolean useTranslucentNavigationButtons(boolean original) {
        return TRANSLUCENT_NAVIGATION_BUTTONS;
    }
}
