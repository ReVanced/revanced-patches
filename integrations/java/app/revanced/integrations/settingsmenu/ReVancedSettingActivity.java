package app.revanced.integrations.settingsmenu;

import android.preference.PreferenceFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.libraries.social.licenses.LicenseActivity;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.ThemeHelper;

public class ReVancedSettingActivity {

    /**
     * Injection point.
     */
    public static void setTheme(LicenseActivity base) {
        final var whiteTheme = "Theme.YouTube.Settings";
        final var darkTheme = "Theme.YouTube.Settings.Dark";

        final var theme = ThemeHelper.isDarkTheme() ? darkTheme : whiteTheme;

        LogHelper.printDebug(() -> "Using theme: " + theme);
        base.setTheme(ReVancedUtils.getResourceIdentifier(theme, "style"));
    }

    /**
     * Injection point.
     */
    public static void initializeSettings(LicenseActivity base) {
        base.setContentView(ReVancedUtils.getResourceIdentifier("revanced_settings_with_toolbar", "layout"));

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
            TextView toolbar = getTextView((ViewGroup) base.findViewById(ReVancedUtils.getResourceIdentifier("toolbar", "id")));
            if (toolbar == null) {
                // FIXME
                // https://github.com/revanced/revanced-patches/issues/1384
                LogHelper.printDebug(() -> "Could not find toolbar");
            } else {
                toolbar.setText(preferenceIdentifier);
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "Could not set Toolbar title", e);
        }

        base.getFragmentManager().beginTransaction().replace(ReVancedUtils.getResourceIdentifier("revanced_settings_fragments", "id"), preferenceFragment).commit();
    }


    @Nullable
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

    @Nullable
    public static ImageButton getImageButton(ViewGroup viewGroup) {
        return getView(ImageButton.class, viewGroup);
    }

    @Nullable
    public static TextView getTextView(ViewGroup viewGroup) {
        return getView(TextView.class, viewGroup);
    }
}
