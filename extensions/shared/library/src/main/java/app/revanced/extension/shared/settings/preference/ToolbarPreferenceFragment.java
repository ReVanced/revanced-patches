package app.revanced.extension.shared.settings.preference;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Insets;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseActivityHook;

@SuppressWarnings({"deprecation", "NewApi"})
public class ToolbarPreferenceFragment extends AbstractPreferenceFragment {

    /**
     * Removes the list of preferences from this fragment, if they exist.
     * @param keys Preference keys.
     */
    protected void removePreferences(String ... keys) {
        for (String key : keys) {
            Preference pref = findPreference(key);
            if (pref != null) {
                PreferenceGroup parent = pref.getParent();
                if (parent != null) {
                    Logger.printDebug(() -> "Removing preference: " + key);
                    parent.removePreference(pref);
                }
            }
        }
    }

    /**
     * Sets toolbar for all nested preference screens.
     */
    protected void setPreferenceScreenToolbar(PreferenceScreen parentScreen) {
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

                            // Allow package-specific background customization.
                            customizeDialogBackground(rootView);

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
                            toolbar.setNavigationIcon(getBackButtonDrawable());
                            toolbar.setNavigationOnClickListener(view -> preferenceScreenDialog.dismiss());

                            final int margin = Utils.dipToPixels(16);
                            toolbar.setTitleMargin(margin, 0, margin, 0);

                            TextView toolbarTextView = Utils.getChildView(toolbar,
                                    true, TextView.class::isInstance);
                            if (toolbarTextView != null) {
                                toolbarTextView.setTextColor(Utils.getAppForegroundColor());
                                toolbarTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            }

                            // Allow package-specific toolbar customization.
                            customizeToolbar(toolbar);

                            // Allow package-specific post-toolbar setup.
                            onPostToolbarSetup(toolbar, preferenceScreenDialog);

                            rootView.addView(toolbar, 0);
                            return false;
                        }
                );
            }
        }
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
     * Returns the drawable for the back button.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getBackButtonDrawable() {
        final int backButtonResource = Utils.getResourceIdentifierOrThrow(
                "revanced_settings_toolbar_arrow_left", "drawable");
        Drawable drawable = Utils.getContext().getResources().getDrawable(backButtonResource);
        customizeBackButtonDrawable(drawable);
        return drawable;
    }

    /**
     * Customizes the back button drawable.
     */
    protected static void customizeBackButtonDrawable(Drawable drawable) {
        drawable.setTint(Utils.getAppForegroundColor());
    }

    /**
     * Allows subclasses to customize the dialog's root view background.
     */
    protected void customizeDialogBackground(ViewGroup rootView) {
        rootView.setBackgroundColor(Utils.getAppBackgroundColor());
    }

    /**
     * Allows subclasses to customize the toolbar.
     */
    protected void customizeToolbar(Toolbar toolbar) {
        BaseActivityHook.setToolbarLayoutParams(toolbar);
    }

    /**
     * Allows subclasses to perform actions after toolbar setup.
     */
    protected void onPostToolbarSetup(Toolbar toolbar, Dialog preferenceScreenDialog) {}
}
