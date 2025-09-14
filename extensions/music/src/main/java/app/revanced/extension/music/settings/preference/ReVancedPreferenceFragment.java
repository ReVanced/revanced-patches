package app.revanced.extension.music.settings.preference;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toolbar;
import androidx.annotation.Nullable;
import app.revanced.extension.music.settings.GoogleApiActivityHook;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;

import static app.revanced.extension.shared.Utils.getResourceIdentifier;

/**
 * Preference fragment for ReVanced settings.
 */
@SuppressWarnings("deprecation")
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {

    /**
     * Initializes the preference fragment.
     */
    @Override
    protected void initialize() {
        super.initialize();

        try {
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            Utils.sortPreferenceGroups(preferenceScreen);

            setPreferenceScreenToolbar(preferenceScreen);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getBackButtonDrawable() {
        return Utils.getContext().getResources().getDrawable(
                getResourceIdentifier("revanced_settings_toolbar_arrow_left", "drawable"));
    }

    /**
     * Sets the system navigation bar color for the activity.
     * Applies the background color obtained from {@link Utils#getAppBackgroundColor()} to the navigation bar.
     * For Android 10 (API 29) and above, enforces navigation bar contrast to ensure visibility.
     */
    public static void setNavigationBarColor(@Nullable Window window) {
        if (window == null) {
            Logger.printDebug(() -> "Cannot set navigation bar color, window is null");
            return;
        }

        window.setNavigationBarColor(Utils.getAppBackgroundColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(true);
        }
    }

    /**
     * Sets toolbar for all nested preference screens.
     */
    private void setPreferenceScreenToolbar(PreferenceScreen parentScreen) {
        for (int i = 0, count = parentScreen.getPreferenceCount(); i < count; i++) {
            Preference childPreference = parentScreen.getPreference(i);
            if (childPreference instanceof PreferenceScreen) {
                // Recursively set sub preferences.
                setPreferenceScreenToolbar((PreferenceScreen) childPreference);

                childPreference.setOnPreferenceClickListener(
                        childScreen -> {
                            Dialog preferenceScreenDialog = ((PreferenceScreen) childScreen).getDialog();
                            ViewGroup rootView = (ViewGroup) preferenceScreenDialog
                                    .findViewById(android.R.id.content)
                                    .getParent();

                            // Set black background for the subscreen to prevent overlap.
                            rootView.setBackgroundColor(Color.BLACK);

                            // Fix the system navigation bar color for submenus.
                            setNavigationBarColor(preferenceScreenDialog.getWindow());

                            // Fix edge-to-edge screen with Android 15 and YT 19.45+
                            // https://developer.android.com/develop/ui/views/layout/edge-to-edge#system-bars-insets
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                                    Insets statusInsets = insets.getInsets(WindowInsets.Type.statusBars());
                                    Insets navInsets = insets.getInsets(WindowInsets.Type.navigationBars());
                                    Insets cutoutInsets = insets.getInsets(WindowInsets.Type.displayCutout());

                                    // Apply padding for display cutout in landscape.
                                    int leftPadding = cutoutInsets.left;
                                    int rightPadding = cutoutInsets.right;
                                    int topPadding = statusInsets.top;
                                    int bottomPadding = navInsets.bottom;

                                    v.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
                                    return insets;
                                });
                            }

                            Toolbar toolbar = new Toolbar(childScreen.getContext());
                            toolbar.setTitle(childScreen.getTitle());

                            Drawable navigationIcon = getBackButtonDrawable();
                            navigationIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                            toolbar.setNavigationIcon(navigationIcon);
                            toolbar.setNavigationOnClickListener(view -> preferenceScreenDialog.dismiss());

                            final int margin = Utils.dipToPixels(16);
                            toolbar.setTitleMargin(margin, 0, margin, 0);

                            TextView toolbarTextView = Utils.getChildView(toolbar,
                                    true, TextView.class::isInstance);
                            if (toolbarTextView != null) {
                                toolbarTextView.setTextColor(Utils.getAppForegroundColor());
                                toolbarTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            }

                            GoogleApiActivityHook.setToolbarLayoutParams(toolbar);

                            rootView.addView(toolbar, 0);
                            return false;
                        }
                );
            }
        }
    }
}
