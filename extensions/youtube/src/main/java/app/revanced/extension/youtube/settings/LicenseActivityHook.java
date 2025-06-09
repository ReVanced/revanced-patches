package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceFragment;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.youtube.ThemeHelper;
import app.revanced.extension.youtube.patches.VersionCheckPatch;
import app.revanced.extension.youtube.patches.spoof.SpoofAppVersionPatch;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

/**
 * Hooks LicenseActivity.
 * <p>
 * This class is responsible for injecting our own fragment by replacing the LicenseActivity.
 */
@SuppressWarnings("unused")
public class LicenseActivityHook {

    private static ViewGroup.LayoutParams toolbarLayoutParams;

    public static void setToolbarLayoutParams(Toolbar toolbar) {
        if (toolbarLayoutParams != null) {
            toolbar.setLayoutParams(toolbarLayoutParams);
        }
    }

    /**
     * Injection point.
     * Overrides the ReVanced settings language.
     */
    public static Context getAttachBaseContext(Context original) {
        AppLanguage language = BaseSettings.REVANCED_LANGUAGE.get();
        if (language == AppLanguage.DEFAULT) {
            return original;
        }

        return Utils.getContext();
    }

    /**
     * Injection point.
     */
    public static boolean useCairoSettingsFragment(boolean original) {
        // Early targets have layout issues and it's better to always force off.
        if (!VersionCheckPatch.IS_19_34_OR_GREATER) {
            return false;
        }
        if (Settings.RESTORE_OLD_SETTINGS_MENUS.get()) {
            return false;
        }
        // Spoofing can cause half broken settings menus of old and new settings.
        if (SpoofAppVersionPatch.isSpoofingToLessThan("19.35.36")) {
            return false;
        }

        // On the first launch of a clean install, forcing the cairo menu can give a
        // half broken appearance because all the preference icons may not be available yet.
        // 19.34+ cairo settings are always on, so it doesn't need to be forced anyway.
        // Cairo setting will show on the next launch of the app.
        return original;
    }

    /**
     * Injection point.
     * <p>
     * Hooks LicenseActivity#onCreate in order to inject our own fragment.
     */
    public static void initialize(Activity licenseActivity) {
        try {
            ThemeHelper.setActivityTheme(licenseActivity);
            ThemeHelper.setNavigationBarColor(licenseActivity.getWindow());
            licenseActivity.setContentView(getResourceIdentifier(
                    "revanced_settings_with_toolbar", "layout"));

            // Sanity check.
            String dataString = licenseActivity.getIntent().getDataString();
            if (!"revanced_settings_intent".equals(dataString)) {
                Logger.printException(() -> "Unknown intent: " + dataString);
                return;
            }

            PreferenceFragment fragment = new ReVancedPreferenceFragment();
            createToolbar(licenseActivity, fragment);

            //noinspection deprecation
            licenseActivity.getFragmentManager()
                    .beginTransaction()
                    .replace(getResourceIdentifier("revanced_settings_fragments", "id"), fragment)
                    .commit();
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void createToolbar(Activity activity, PreferenceFragment fragment) {
        // Replace dummy placeholder toolbar.
        // This is required to fix submenu title alignment issue with Android ASOP 15+
        ViewGroup toolBarParent = activity.findViewById(
                getResourceIdentifier("revanced_toolbar_parent", "id"));
        ViewGroup dummyToolbar = Utils.getChildViewByResourceName(toolBarParent, "revanced_toolbar");
        toolbarLayoutParams = dummyToolbar.getLayoutParams();
        toolBarParent.removeView(dummyToolbar);

        Toolbar toolbar = new Toolbar(toolBarParent.getContext());
        toolbar.setBackgroundColor(ThemeHelper.getToolbarBackgroundColor());
        toolbar.setNavigationIcon(ReVancedPreferenceFragment.getBackButtonDrawable());
        toolbar.setTitle(getResourceIdentifier("revanced_settings_title", "string"));

        final int margin = Utils.dipToPixels(16);
        toolbar.setTitleMarginStart(margin);
        toolbar.setTitleMarginEnd(margin);
        TextView toolbarTextView = Utils.getChildView(toolbar, false,
                view -> view instanceof TextView);
        if (toolbarTextView != null) {
            toolbarTextView.setTextColor(Utils.getAppForegroundColor());
        }
        setToolbarLayoutParams(toolbar);

        // Add Search Icon and EditText for ReVancedPreferenceFragment only.
        if (fragment instanceof ReVancedPreferenceFragment) {
            SearchViewController.addSearchViewComponents(activity, toolbar, (ReVancedPreferenceFragment) fragment);
        }

        toolBarParent.addView(toolbar, 0);
    }
}
