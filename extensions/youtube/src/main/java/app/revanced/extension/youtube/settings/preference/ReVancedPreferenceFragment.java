package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Insets;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory;
import app.revanced.extension.youtube.settings.LicenseActivityHook;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup;

/**
 * Preference fragment for ReVanced settings.
 */
@SuppressWarnings({"deprecation", "NewApi"})
public class ReVancedPreferenceFragment extends ToolbarPreferenceFragment {

    /**
     * The main PreferenceScreen used to display the current set of preferences.
     * This screen is manipulated during initialization and filtering to show or hide preferences.
     */
    private PreferenceScreen preferenceScreen;

    /**
     * A copy of the original PreferenceScreen created during initialization.
     * Used to restore the preference structure to its initial state after filtering or other modifications.
     */
    private PreferenceScreen originalPreferenceScreen;

    /**
     * Used for searching preferences. A Collection of all preferences including nested preferences.
     * Root preferences are excluded (no need to search what's on the root screen),
     * but their sub preferences are included.
     */
    private final List<AbstractPreferenceSearchData<?>> allPreferences = new ArrayList<>();

    /**
     * Initializes the preference fragment.
     */
    @Override
    protected void initialize() {
        super.initialize();

        try {
            preferenceScreen = getPreferenceScreen();
            Utils.sortPreferenceGroups(preferenceScreen);
            setPreferenceScreenToolbar(preferenceScreen);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Called when the fragment starts.
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            // Initialize search controller if needed.
            if (searchViewController != null) {
                // Trigger search data collection after fragment is ready.
                searchViewController.initializeSearchData();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onStart failure", ex);
        }
    }

    /**
     * Sets toolbar for all nested preference screens.
     */
    @Override
    protected void customizeToolbar(Toolbar toolbar) {
        LicenseActivityHook.setToolbarLayoutParams(toolbar);
    }

    /**
     * Perform actions after toolbar setup.
     */
    @Override
    protected void onPostToolbarSetup(Toolbar toolbar, Dialog preferenceScreenDialog) {
        if (LicenseActivityHook.searchViewController != null
                && LicenseActivityHook.searchViewController.isSearchActive()) {
            toolbar.post(() -> LicenseActivityHook.searchViewController.closeSearch());
        }
    }

    /**
     * Returns the preference screen for external access by SearchViewController.
     */
    public PreferenceScreen getPreferenceScreenForSearch() {
        return preferenceScreen;
    }
}
