package app.revanced.extension.youtube.settings.preference;

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
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.EnumSetting;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.extension.youtube.ThemeHelper;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.LicenseActivityHook;
import app.revanced.extension.youtube.settings.Settings;

/**
 * Preference fragment for ReVanced settings.
 */
@SuppressWarnings("deprecation")
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {

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
     * A list of top-level preferences directly attached to the main PreferenceScreen.
     * Stored to maintain a reference to the primary preferences for easier manipulation or restoration.
     */
    private List<Preference> topLevelPreferences;

    /**
     * A comprehensive list of all preferences, including nested ones, collected from the PreferenceScreen.
     * Used for filtering and searching through all available preferences.
     */
    private List<Preference> allPreferences;

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getBackButtonDrawable() {
        final int backButtonResource = getResourceIdentifier(ThemeHelper.isDarkTheme()
                        ? "yt_outline_arrow_left_white_24"
                        : "yt_outline_arrow_left_black_24",
                "drawable");
        return Utils.getContext().getResources().getDrawable(backButtonResource);
    }

    /**
     * Sorts a preference list by menu entries, but preserves the first value as the first entry.
     *
     * @noinspection SameParameterValue
     */
    private static void sortListPreferenceByValues(ListPreference listPreference, int firstEntriesToPreserve) {
        CharSequence[] entries = listPreference.getEntries();
        CharSequence[] entryValues = listPreference.getEntryValues();
        final int entrySize = entries.length;

        if (entrySize != entryValues.length) {
            // Xml array declaration has a missing/extra entry.
            throw new IllegalStateException();
        }

        List<Pair<String, String>> firstPairs = new ArrayList<>(firstEntriesToPreserve);
        List<Pair<String, String>> pairsToSort = new ArrayList<>(entrySize);

        for (int i = 0; i < entrySize; i++) {
            Pair<String, String> pair = new Pair<>(entries[i].toString(), entryValues[i].toString());
            if (i < firstEntriesToPreserve) {
                firstPairs.add(pair);
            } else {
                pairsToSort.add(pair);
            }
        }

        pairsToSort.sort((pair1, pair2)
                -> pair1.first.compareToIgnoreCase(pair2.first));

        CharSequence[] sortedEntries = new CharSequence[entrySize];
        CharSequence[] sortedEntryValues = new CharSequence[entrySize];

        int i = 0;
        for (Pair<String, String> pair : firstPairs) {
            sortedEntries[i] = pair.first;
            sortedEntryValues[i] = pair.second;
            i++;
        }

        for (Pair<String, String> pair : pairsToSort) {
            sortedEntries[i] = pair.first;
            sortedEntryValues[i] = pair.second;
            i++;
        }

        listPreference.setEntries(sortedEntries);
        listPreference.setEntryValues(sortedEntryValues);
    }

    /**
     * Initializes the preference fragment, copying the original screen to allow full restoration.
     */
    @Override
    protected void initialize() {
        super.initialize();

        try {
            preferenceScreen = getPreferenceScreen();
            if (preferenceScreen == null) {
                Logger.printDebug(() -> "PreferenceScreen is null during initialization");
                throw new IllegalStateException("PreferenceScreen is null");
            }

            // Store the original structure for restoration after filtering
            originalPreferenceScreen = getPreferenceManager().createPreferenceScreen(getContext());
            for (int i = 0, count = preferenceScreen.getPreferenceCount(); i < count; i++) {
                originalPreferenceScreen.addPreference(preferenceScreen.getPreference(i));
            }

            topLevelPreferences = new ArrayList<>();
            allPreferences = new ArrayList<>();

            for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
                topLevelPreferences.add(preferenceScreen.getPreference(i));
            }

            collectPreferences(preferenceScreen, allPreferences);
            setPreferenceScreenToolbar(preferenceScreen);

            // If the preference was included, then initialize it based on the available playback speed.
            Preference preference = findPreference(Settings.PLAYBACK_SPEED_DEFAULT.key);
            if (preference instanceof ListPreference playbackPreference) {
                CustomPlaybackSpeedPatch.initializeListPreference(playbackPreference);
            }

            sortPreferenceListMenu(Settings.CHANGE_START_PAGE);
            sortPreferenceListMenu(Settings.SPOOF_VIDEO_STREAMS_LANGUAGE);
            sortPreferenceListMenu(BaseSettings.REVANCED_LANGUAGE);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Recursively collects all preferences from the screen.
     */
    private void collectPreferences(PreferenceScreen screen, List<Preference> preferences) {
        for (int i = 0, count = screen.getPreferenceCount(); i < count; i++) {
            Preference preference = screen.getPreference(i);
            preferences.add(preference);
            if (preference instanceof PreferenceScreen) {
                collectPreferences((PreferenceScreen) preference, preferences);
            }
        }
    }

    /**
     * Filters the preferences using the given query string.
     */
    public void filterPreferences(String query) {
        if (preferenceScreen == null || topLevelPreferences == null
                || allPreferences == null || originalPreferenceScreen == null) {
            return;
        }

        preferenceScreen.removeAll();
        if (TextUtils.isEmpty(query)) {
            restoreOriginalPreferences(preferenceScreen, originalPreferenceScreen);
        } else {
            String queryLower = query.toLowerCase();
            Set<Preference> addedPreferences = new HashSet<>();
            for (Preference preference : allPreferences) {
                String title = preference.getTitle() != null ? preference.getTitle().toString().toLowerCase() : "";
                String summary = preference.getSummary() != null ? preference.getSummary().toString().toLowerCase() : "";
                if (title.contains(queryLower) || summary.contains(queryLower)) {
                    addPreferenceWithParent(preference, preferenceScreen, addedPreferences);
                }
            }
        }
    }

    /**
     * Adds a preference to the target screen, preserving parent category if applicable.
     */
    private void addPreferenceWithParent(Preference preference, PreferenceScreen targetScreen,
                                         Set<Preference> addedPreferences) {
        if (addedPreferences.contains(preference)) return;

        PreferenceGroup parent = preference.getParent();
        if (parent instanceof PreferenceCategory && !addedPreferences.contains(parent)) {
            PreferenceCategory newCategory = new PreferenceCategory(targetScreen.getContext());
            newCategory.setKey(parent.getKey());
            newCategory.setTitle(parent.getTitle());
            newCategory.setSummary(parent.getSummary());
            newCategory.setIcon(parent.getIcon());
            targetScreen.addPreference(newCategory);
            addedPreferences.add(parent);
            newCategory.addPreference(preference);
        } else {
            targetScreen.addPreference(preference);
        }

        addedPreferences.add(preference);
    }

    /**
     * Restores preferences to the original state before filtering.
     */
    private void restoreOriginalPreferences(PreferenceScreen targetScreen, PreferenceScreen sourceScreen) {
        targetScreen.removeAll();
        for (int i = 0, count = sourceScreen.getPreferenceCount(); i < count; i++) {
            targetScreen.addPreference(sourceScreen.getPreference(i));
        }
        setPreferenceScreenToolbar(targetScreen);
        Utils.sortPreferenceGroups(targetScreen);
    }

    /**
     * Sorts a specific list preference by its entries, but retain the first entry as the first item.
     */
    private void sortPreferenceListMenu(EnumSetting<?> setting) {
        Preference preference = findPreference(setting.key);
        if (preference instanceof ListPreference languagePreference) {
            sortListPreferenceByValues(languagePreference, 1);
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

                            // Fix edge-to-edge screen with Android 15 and YT 19.45+
                            // https://developer.android.com/develop/ui/views/layout/edge-to-edge#system-bars-insets
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                                    Insets statusInsets = insets.getInsets(WindowInsets.Type.statusBars());
                                    Insets navInsets = insets.getInsets(WindowInsets.Type.navigationBars());
                                    v.setPadding(0, statusInsets.top, 0, navInsets.bottom);
                                    return insets;
                                });
                            }

                            Toolbar toolbar = new Toolbar(childScreen.getContext());
                            toolbar.setTitle(childScreen.getTitle());
                            toolbar.setNavigationIcon(getBackButtonDrawable());
                            toolbar.setNavigationOnClickListener(view -> preferenceScreenDialog.dismiss());

                            final int margin = (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
                            );
                            toolbar.setTitleMargin(margin, 0, margin, 0);

                            TextView toolbarTextView = Utils.getChildView(toolbar,
                                    true, TextView.class::isInstance);
                            if (toolbarTextView != null) {
                                toolbarTextView.setTextColor(ThemeHelper.getForegroundColor());
                            }

                            LicenseActivityHook.setToolbarLayoutParams(toolbar);

                            rootView.addView(toolbar, 0);
                            return false;
                        }
                );
            }
        }
    }
}
