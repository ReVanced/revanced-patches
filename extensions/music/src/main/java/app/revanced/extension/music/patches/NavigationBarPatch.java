package app.revanced.extension.music.patches;

import static app.revanced.extension.shared.Utils.hideViewUnderCondition;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class NavigationBarPatch {
    @NonNull
    private static String lastYTNavigationEnumName = "";

    public static void setLastAppNavigationEnum(@Nullable Enum<?> ytNavigationEnumName) {
        if (ytNavigationEnumName != null) {
            lastYTNavigationEnumName = ytNavigationEnumName.name();
        }
    }

    public static void hideNavigationLabel(TextView textview) {
        hideViewUnderCondition(Settings.HIDE_NAVIGATION_BAR_LABEL.get(), textview);
    }

    public static void hideNavigationButton(@NonNull View view) {
        // Hide entire navigation bar.
        if (Settings.HIDE_NAVIGATION_BAR.get() && view.getParent() != null) {
            hideViewUnderCondition(true, (View) view.getParent());
            return;
        }

        // Hide navigation buttons based on their type.
        for (NavigationButton button : NavigationButton.values()) {
            if (button.ytEnumNames.equals(lastYTNavigationEnumName)) {
                hideViewUnderCondition(button.hidden, view);
                break;
            }
        }
    }

    private enum NavigationButton {
        HOME(
                "TAB_HOME",
                Settings.HIDE_NAVIGATION_BAR_HOME_BUTTON.get()
        ),
        SAMPLES(
                "TAB_SAMPLES",
                Settings.HIDE_NAVIGATION_BAR_SAMPLES_BUTTON.get()
        ),
        EXPLORE(
                "TAB_EXPLORE",
                Settings.HIDE_NAVIGATION_BAR_EXPLORE_BUTTON.get()
        ),
        LIBRARY(
                "LIBRARY_MUSIC",
                Settings.HIDE_NAVIGATION_BAR_LIBRARY_BUTTON.get()
        ),
        UPGRADE(
                "TAB_MUSIC_PREMIUM",
                Settings.HIDE_NAVIGATION_BAR_UPGRADE_BUTTON.get()
        );

        private final String ytEnumNames;
        private final boolean hidden;

        NavigationButton(@NonNull String ytEnumNames, boolean hidden) {
            this.ytEnumNames = ytEnumNames;
            this.hidden = hidden;
        }
    }
}
