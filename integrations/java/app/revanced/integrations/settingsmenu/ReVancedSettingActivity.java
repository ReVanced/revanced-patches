package app.revanced.integrations.settingsmenu;

import android.content.Context;
import android.preference.PreferenceFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.libraries.social.licenses.LicenseActivity;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.ThemeHelper;

public class ReVancedSettingActivity {

    public static void setTheme(LicenseActivity base) {
        final var whiteTheme = "Theme.YouTube.Settings";
        final var darkTheme = "Theme.YouTube.Settings.Dark";

        final var theme = ThemeHelper.isDarkTheme() ? darkTheme : whiteTheme;

        LogHelper.debug(ReVancedSettingActivity.class, "Using theme: " + theme);
        base.setTheme(getIdentifier(theme, "style"));
    }

    public static void initializeSettings(LicenseActivity base) {
        base.setContentView(getIdentifier("revanced_settings_with_toolbar", "layout"));

        PreferenceFragment preferenceFragment;
        String preferenceIdentifier;

        String dataString = base.getIntent().getDataString();
        if (dataString.equalsIgnoreCase("sponsorblock_settings")) {
            preferenceIdentifier = "sb_settings";
            preferenceFragment = new SponsorBlockSettingsFragment();
        } else if (dataString.equalsIgnoreCase("ryd_settings")) {
            preferenceIdentifier = "revanced_ryd_settings_title";
            preferenceFragment = new ReturnYouTubeDislikeSettingsFragment();
        } else {
            preferenceIdentifier = "revanced_settings";
            preferenceFragment = new ReVancedSettingsFragment();
        }

        try {
            getTextView((ViewGroup) base.findViewById(getIdentifier("toolbar", "id"))).setText(preferenceIdentifier);
        } catch (Exception e) {
            LogHelper.printException(ReVancedSettingActivity.class, "Couldn't set Toolbar title", e);
        }

        base.getFragmentManager().beginTransaction().replace(getIdentifier("revanced_settings_fragments", "id"), preferenceFragment).commit();
    }


    public static <T extends View> T getView(Class<T> typeClass, ViewGroup viewGroup) {
        if (viewGroup == null) {
            return null;
        }
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt.getClass() == typeClass) {
                return (T) childAt;
            }
        }
        return null;
    }

    public static ImageButton getImageButton(ViewGroup viewGroup) {
        return getView(ImageButton.class, viewGroup);
    }

    public static TextView getTextView(ViewGroup viewGroup) {
        return getView(TextView.class, viewGroup);
    }

    private static int getIdentifier(String name, String defType) {
        Context appContext = ReVancedUtils.getContext();
        assert appContext != null;
        return appContext.getResources().getIdentifier(name, defType, appContext.getPackageName());
    }
}
