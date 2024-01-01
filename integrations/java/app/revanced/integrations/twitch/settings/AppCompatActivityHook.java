package app.revanced.integrations.twitch.settings;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.twitch.settings.preference.ReVancedPreferenceFragment;
import tv.twitch.android.feature.settings.menu.SettingsMenuGroup;
import tv.twitch.android.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Hooks AppCompatActivity.
 * <p>
 * This class is responsible for injecting our own fragment by replacing the AppCompatActivity.
 * @noinspection unused
 */
public class AppCompatActivityHook {
    private static final int REVANCED_SETTINGS_MENU_ITEM_ID = 0x7;
    private static final String EXTRA_REVANCED_SETTINGS = "app.revanced.twitch.settings";

    /**
     * Launches SettingsActivity and show ReVanced settings
     */
    public static void startSettingsActivity() {
        Logger.printDebug(() -> "Launching ReVanced settings");

        final var context = app.revanced.integrations.shared.Utils.getContext();

        if (context != null) {
            Intent intent = new Intent(context, SettingsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean(EXTRA_REVANCED_SETTINGS, true);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * Helper for easy access in smali
     * @return Returns string resource id
     */
    public static int getReVancedSettingsString() {
        return app.revanced.integrations.twitch.Utils.getStringId("revanced_settings");
    }

    /**
     * Intercepts settings menu group list creation in SettingsMenuPresenter$Event.MenuGroupsUpdated
     * @return Returns a modified list of menu groups
     */
    public static List<SettingsMenuGroup> handleSettingMenuCreation(List<SettingsMenuGroup> settingGroups, Object revancedEntry) {
        List<SettingsMenuGroup> groups = new ArrayList<>(settingGroups);

        if (groups.isEmpty()) {
            // Create new menu group if none exist yet
            List<Object> items = new ArrayList<>();
            items.add(revancedEntry);
            groups.add(new SettingsMenuGroup(items));
        } else {
            // Add to last menu group
            int groupIdx = groups.size() - 1;
            List<Object> items = new ArrayList<>(groups.remove(groupIdx).getSettingsMenuItems());
            items.add(revancedEntry);
            groups.add(new SettingsMenuGroup(items));
        }

        Logger.printDebug(() -> settingGroups.size() + " menu groups in list");
        return groups;
    }

    /**
     * Intercepts settings menu group onclick events
     * @return Returns true if handled, otherwise false
     */
    @SuppressWarnings("rawtypes")
    public static boolean handleSettingMenuOnClick(Enum item) {
        Logger.printDebug(() -> "item " + item.ordinal() + " clicked");
        if (item.ordinal() != REVANCED_SETTINGS_MENU_ITEM_ID) {
            return false;
        }

        startSettingsActivity();
        return true;
    }

    /**
     * Intercepts fragment loading in SettingsActivity.onCreate
     * @return Returns true if the revanced settings have been requested by the user, otherwise false
     */
    public static boolean handleSettingsCreation(androidx.appcompat.app.AppCompatActivity base) {
        if (!base.getIntent().getBooleanExtra(EXTRA_REVANCED_SETTINGS, false)) {
            Logger.printDebug(() -> "Revanced settings not requested");
            return false; // User wants to enter another settings fragment
        }
        Logger.printDebug(() -> "ReVanced settings requested");

        ReVancedPreferenceFragment fragment = new ReVancedPreferenceFragment();
        ActionBar supportActionBar = base.getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setTitle(app.revanced.integrations.twitch.Utils.getStringId("revanced_settings"));

        base.getFragmentManager()
                .beginTransaction()
                .replace(Utils.getResourceIdentifier("fragment_container", "id"), fragment)
                .commit();
        return true;
    }
}
