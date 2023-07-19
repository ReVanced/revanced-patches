package app.revanced.tiktok.settingsmenu;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import app.revanced.tiktok.utils.LogHelper;
import app.revanced.tiktok.utils.ReVancedUtils;
import com.bytedance.ies.ugc.aweme.commercialize.compliance.personalization.AdPersonalizationActivity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class SettingsMenu {
    public static Object createSettingsEntry(String entryClazzName, String entryInfoClazzName) {
        try {
            Class<?> entryClazz = Class.forName(entryClazzName);
            Class<?> entryInfoClazz = Class.forName(entryInfoClazzName);
            Constructor<?> entryConstructor = entryClazz.getConstructor(entryInfoClazz);
            Constructor<?> entryInfoConstructor = entryInfoClazz.getDeclaredConstructors()[0];
            Object buttonInfo = entryInfoConstructor.newInstance("Revanced settings", null, (View.OnClickListener) view -> startSettingsActivity());
            return entryConstructor.newInstance(buttonInfo);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * Initialize the settings menu.
     * @param base The activity to initialize the settings menu on.
     * @return Whether the settings menu should be initialized.
     */
    public static boolean initializeSettings(AdPersonalizationActivity base) {
        if (!base.getIntent().getExtras().getBoolean("revanced", false)) return false;

        SettingsStatus.load();

        LinearLayout linearLayout = new LinearLayout(base);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setFitsSystemWindows(true);
        linearLayout.setTransitionGroup(true);

        FrameLayout fragment = new FrameLayout(base);
        fragment.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        int fragmentId = View.generateViewId();
        fragment.setId(fragmentId);

        linearLayout.addView(fragment);
        base.setContentView(linearLayout);

        PreferenceFragment preferenceFragment = new ReVancedSettingsFragment();
        base.getFragmentManager().beginTransaction().replace(fragmentId, preferenceFragment).commit();

        return true;
    }

    private static void startSettingsActivity() {
        Context appContext = ReVancedUtils.getAppContext();
        if (appContext != null) {
            Intent intent = new Intent(appContext, AdPersonalizationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("revanced", true);
            appContext.startActivity(intent);
        } else {
            LogHelper.debug(SettingsMenu.class, "ReVancedUtils.getAppContext() return null");
        }
    }
}
