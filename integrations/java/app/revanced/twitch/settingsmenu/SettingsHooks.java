package app.revanced.twitch.settingsmenu;

import static app.revanced.twitch.utils.ReVancedUtils.getIdentifier;
import static app.revanced.twitch.utils.ReVancedUtils.getStringId;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import app.revanced.twitch.utils.ReVancedUtils;
import app.revanced.twitch.utils.LogHelper;
import tv.twitch.android.feature.settings.menu.SettingsMenuGroup;
import tv.twitch.android.settings.SettingsActivity;

public class SettingsHooks {
    private static final int REVANCED_SETTINGS_MENU_ITEM_ID = 0x7;
    private static final String EXTRA_REVANCED_SETTINGS = "app.revanced.twitch.settings";

    /**
     * Launches SettingsActivity and show ReVanced settings
     */
    public static void startSettingsActivity() {
        LogHelper.debug("Launching ReVanced settings");

        ReVancedUtils.ifContextAttached((c) -> {
            Intent intent = new Intent(c, SettingsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean(EXTRA_REVANCED_SETTINGS, true);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(intent);
        });
    }

    /**
     * Helper for easy access in smali
     * @return Returns string resource id
     */
    public static int getReVancedSettingsString() {
        return getStringId("revanced_settings");
    }

    /**
     * Intercepts settings menu group list creation in SettingsMenuPresenter$Event.MenuGroupsUpdated
     * @return Returns a modified list of menu groups
     */
    public static List<SettingsMenuGroup> handleSettingMenuCreation(List<SettingsMenuGroup> settingGroups, Object revancedEntry) {
        List<SettingsMenuGroup> groups = new ArrayList<>(settingGroups);

        if(groups.size() < 1) {
            // Create new menu group if none exist yet
            List<Object> items = new ArrayList<>();
            items.add(revancedEntry);
            groups.add(new SettingsMenuGroup(items));
        }
        else {
            // Add to last menu group
            int groupIdx = groups.size() - 1;
            List<Object> items = new ArrayList<>(groups.remove(groupIdx).getSettingsMenuItems());
            items.add(revancedEntry);
            groups.add(new SettingsMenuGroup(items));
        }

        LogHelper.debug("%d menu groups in list", settingGroups.size());
        return groups;
    }

    /**
     * Intercepts settings menu group onclick events
     * @return Returns true if handled, otherwise false
     */
    @SuppressWarnings("rawtypes")
    public static boolean handleSettingMenuOnClick(Enum item) {
        LogHelper.debug("item %d clicked", item.ordinal());
        if(item.ordinal() != REVANCED_SETTINGS_MENU_ITEM_ID) {
            return false;
        }

        startSettingsActivity();
        return true;
    }

    /**
     * Intercepts fragment loading in SettingsActivity.onCreate
     * @return Returns true if the revanced settings have been requested by the user, otherwise false
     */
    public static boolean handleSettingsCreation(AppCompatActivity base) {
        if(!base.getIntent().getBooleanExtra(EXTRA_REVANCED_SETTINGS, false)) {
            LogHelper.debug("Revanced settings not requested");
            return false; // User wants to enter another settings fragment
        }
        LogHelper.debug("ReVanced settings requested");

        ReVancedSettingsFragment fragment = new ReVancedSettingsFragment();
        ActionBar supportActionBar = base.getSupportActionBar();
        if(supportActionBar != null)
            supportActionBar.setTitle(getStringId("revanced_settings"));

        base.getFragmentManager()
                .beginTransaction()
                .replace(getIdentifier("fragment_container", "id"), fragment)
                .commit();
        return true;
    }
}
