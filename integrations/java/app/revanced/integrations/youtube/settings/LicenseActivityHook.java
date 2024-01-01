package app.revanced.integrations.youtube.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.preference.PreferenceFragment;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.ThemeHelper;
import app.revanced.integrations.youtube.settings.preference.ReVancedPreferenceFragment;
import app.revanced.integrations.youtube.settings.preference.ReturnYouTubeDislikePreferenceFragment;
import app.revanced.integrations.youtube.settings.preference.SponsorBlockPreferenceFragment;

import java.util.Objects;

import static app.revanced.integrations.shared.Utils.getChildView;
import static app.revanced.integrations.shared.Utils.getResourceIdentifier;

/**
 * Hooks LicenseActivity.
 * <p>
 * This class is responsible for injecting our own fragment by replacing the LicenseActivity.
 */
@SuppressWarnings("unused")
public class LicenseActivityHook {

    /**
     * Injection point.
     * <p>
     * Hooks LicenseActivity#onCreate in order to inject our own fragment.
     */
    public static void initialize(Activity licenseActivity) {
        try {
            ThemeHelper.setActivityTheme(licenseActivity);
            licenseActivity.setContentView(
                    getResourceIdentifier("revanced_settings_with_toolbar", "layout"));
            setBackButton(licenseActivity);

            PreferenceFragment fragment;
            String toolbarTitleResourceName;
            String dataString = licenseActivity.getIntent().getDataString();
            switch (dataString) {
                case "sponsorblock_settings":
                    toolbarTitleResourceName = "revanced_sponsorblock_settings_title";
                    fragment = new SponsorBlockPreferenceFragment();
                    break;
                case "ryd_settings":
                    toolbarTitleResourceName = "revanced_ryd_settings_title";
                    fragment = new ReturnYouTubeDislikePreferenceFragment();
                    break;
                case "revanced_settings":
                    toolbarTitleResourceName = "revanced_settings_title";
                    fragment = new ReVancedPreferenceFragment();
                    break;
                default:
                    Logger.printException(() -> "Unknown setting: " + dataString);
                    return;
            }

            setToolbarTitle(licenseActivity, toolbarTitleResourceName);
            licenseActivity.getFragmentManager()
                    .beginTransaction()
                    .replace(getResourceIdentifier("revanced_settings_fragments", "id"), fragment)
                    .commit();
        } catch (Exception ex) {
            Logger.printException(() -> "onCreate failure", ex);
        }
    }

    private static void setToolbarTitle(Activity activity, String toolbarTitleResourceName) {
        ViewGroup toolbar = activity.findViewById(getToolbarResourceId());
        TextView toolbarTextView = Objects.requireNonNull(getChildView(toolbar, view -> view instanceof TextView));
        toolbarTextView.setText(getResourceIdentifier(toolbarTitleResourceName, "string"));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setBackButton(Activity activity) {
        ViewGroup toolbar = activity.findViewById(getToolbarResourceId());
        ImageButton imageButton = Objects.requireNonNull(getChildView(toolbar, view -> view instanceof ImageButton));
        final int backButtonResource = getResourceIdentifier(ThemeHelper.isDarkTheme()
                        ? "yt_outline_arrow_left_white_24"
                        : "yt_outline_arrow_left_black_24",
                "drawable");
        imageButton.setImageDrawable(activity.getResources().getDrawable(backButtonResource));
        imageButton.setOnClickListener(view -> activity.onBackPressed());
    }

    private static int getToolbarResourceId() {
        final int toolbarResourceId = getResourceIdentifier("revanced_toolbar", "id");
        if (toolbarResourceId == 0) {
            throw new IllegalStateException("Could not find back button resource");
        }
        return toolbarResourceId;
    }

}
