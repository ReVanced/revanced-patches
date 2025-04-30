package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceFragment;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.youtube.ThemeHelper;
import app.revanced.extension.youtube.patches.VersionCheckPatch;
import app.revanced.extension.youtube.patches.spoof.SpoofAppVersionPatch;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;
import app.revanced.extension.youtube.settings.preference.ReturnYouTubeDislikePreferenceFragment;
import app.revanced.extension.youtube.settings.preference.SponsorBlockPreferenceFragment;

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
            licenseActivity.setContentView(getResourceIdentifier(
                    "revanced_settings_with_toolbar", "layout"));

            PreferenceFragment fragment;
            String toolbarTitleResourceName;
            String dataString = Objects.requireNonNull(licenseActivity.getIntent().getDataString());
            switch (dataString) {
                case "revanced_sb_settings_intent":
                    toolbarTitleResourceName = "revanced_sb_settings_title";
                    fragment = new SponsorBlockPreferenceFragment();
                    break;
                case "revanced_ryd_settings_intent":
                    toolbarTitleResourceName = "revanced_ryd_settings_title";
                    fragment = new ReturnYouTubeDislikePreferenceFragment();
                    break;
                case "revanced_settings_intent":
                    toolbarTitleResourceName = "revanced_settings_title";
                    fragment = new ReVancedPreferenceFragment();
                    break;
                default:
                    Logger.printException(() -> "Unknown setting: " + dataString);
                    return;
            }

            createToolbar(licenseActivity, toolbarTitleResourceName, fragment);

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
    private static void createToolbar(Activity activity, String toolbarTitleResourceName, PreferenceFragment fragment) {
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
        toolbar.setTitle(getResourceIdentifier(toolbarTitleResourceName, "string"));

        final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                Utils.getContext().getResources().getDisplayMetrics());
        toolbar.setTitleMarginStart(margin);
        toolbar.setTitleMarginEnd(margin);
        TextView toolbarTextView = Utils.getChildView(toolbar, false,
                view -> view instanceof TextView);
        if (toolbarTextView != null) {
            toolbarTextView.setTextColor(ThemeHelper.getForegroundColor());
        }
        setToolbarLayoutParams(toolbar);

        // Add Search Icon and EditText for ReVancedPreferenceFragment only.
        if (fragment instanceof ReVancedPreferenceFragment) {
            // Create EditText but keep it hidden initially.
            EditText searchEditText = new EditText(toolbar.getContext());
            searchEditText.setId(getResourceIdentifier("search_view", "id"));
            searchEditText.setHint(str("revanced_search_settings"));
            searchEditText.setBackground(null);
            searchEditText.setTextColor(ThemeHelper.getForegroundColor());
            searchEditText.setHintTextColor(ThemeHelper.getForegroundColor() & 0x80FFFFFF); // 50% opacity
            searchEditText.setVisibility(View.GONE);

            // Set layout params for EditText.
            ViewGroup.MarginLayoutParams searchEditTextParams = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                    Utils.getContext().getResources().getDisplayMetrics());
            searchEditTextParams.setMargins(0, 0, rightMargin, 0);
            searchEditText.setLayoutParams(searchEditTextParams);

            // Store original toolbar state.
            final int[] originalTitleMargin = {toolbar.getTitleMarginStart(), toolbar.getTitleMarginEnd()};
            final CharSequence originalTitle = toolbar.getTitle();
            final boolean[] isSearchActive = {false};

            // Update search button icon and visibility based on text.
            searchEditText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean hasText = s != null && s.length() > 0;
                    toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id"))
                            .setIcon(getResourceIdentifier(
                                    ThemeHelper.isDarkTheme() ? "quantum_ic_close_white_24" : "quantum_ic_close_black_24",
                                    "drawable"))
                            .setVisible(hasText);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    Logger.printDebug(() -> "Search query: " + s);
                    ((ReVancedPreferenceFragment) fragment).filterPreferences(s.toString());
                }
            });

            // Close EditText and restore toolbar.
            Runnable closeSearchEditText = () -> {
                isSearchActive[0] = false;
                toolbar.post(() -> {
                    toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id"))
                            .setIcon(getResourceIdentifier(
                                    ThemeHelper.isDarkTheme() ? "yt_outline_search_white_24" : "yt_outline_search_black_24",
                                    "drawable"))
                            .setVisible(true);
                });
                toolbar.setTitle(originalTitle);
                toolbar.setTitleMarginStart(originalTitleMargin[0]);
                toolbar.setTitleMarginEnd(originalTitleMargin[1]);
                searchEditText.setVisibility(View.GONE);
                searchEditText.setText("");
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            };

            // Update navigation click listener to handle EditText closing.
            toolbar.setNavigationOnClickListener(view -> {
                if (isSearchActive[0]) {
                    closeSearchEditText.run();
                } else {
                    activity.onBackPressed();
                }
            });

            // Add search menu to toolbar.
            toolbar.inflateMenu(getResourceIdentifier("revanced_search_menu", "menu"));
            toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id"))
                    .setIcon(getResourceIdentifier(
                            ThemeHelper.isDarkTheme() ? "yt_outline_search_white_24" : "yt_outline_search_black_24",
                            "drawable"))
                    .setTooltipText(null); // Disable tooltip for action_search.

            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == getResourceIdentifier("action_search", "id")) {
                    if (!isSearchActive[0]) {
                        // Open EditText and hide search button.
                        isSearchActive[0] = true;
                        item.setVisible(false);
                        toolbar.setTitle("");
                        toolbar.setTitleMarginStart(0);
                        toolbar.setTitleMarginEnd(0);
                        searchEditText.setVisibility(View.VISIBLE);
                        searchEditText.requestFocus();
                        // Show keyboard.
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
                    } else {
                        // Clear text and hide button.
                        searchEditText.setText("");
                        item.setVisible(false);
                    }
                    return true;
                }
                return false;
            });

            toolbar.addView(searchEditText);
        }

        toolBarParent.addView(toolbar, 0);
    }
}