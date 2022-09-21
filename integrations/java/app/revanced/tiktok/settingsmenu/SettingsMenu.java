package app.revanced.tiktok.settingsmenu;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.bytedance.ies.ugc.aweme.commercialize.compliance.personalization.AdPersonalizationActivity;

import app.revanced.tiktok.utils.LogHelper;
import app.revanced.tiktok.utils.ReVancedUtils;


public class SettingsMenu {
    public static void initializeSettings(AdPersonalizationActivity base) {
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
    }

    public static void startSettingsActivity() {
        Context appContext = ReVancedUtils.getAppContext();
        if (appContext != null) {
            Intent intent = new Intent(appContext, AdPersonalizationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appContext.startActivity(intent);
        } else {
            LogHelper.debug(SettingsMenu.class, "ReVancedUtils.getAppContext() return null");
        }
    }
}
