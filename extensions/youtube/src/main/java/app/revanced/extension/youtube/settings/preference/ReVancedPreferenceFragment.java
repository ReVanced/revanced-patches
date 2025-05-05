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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.EnumSetting;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory;
import app.revanced.extension.youtube.ThemeHelper;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.LicenseActivityHook;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup;

/**
 * Preference fragment for ReVanced settings.
 */
@SuppressWarnings("deprecation")
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {

    private static class PreferenceSearchData {
        final String key;
        final String title;
        final String summary;
        final String navigationPath;

        PreferenceSearchData(Preference preference) {
            key = Utils.removePunctuationToLowercase(preference.getKey());
            title = Utils.removePunctuationToLowercase(preference.getTitle());
            summary = Utils.removePunctuationToLowercase(preference.getSummary());
            navigationPath = getPreferenceNavigationString(preference);
        }

        boolean matchesSearchQuery(String query) {
            return title.contains(query) || summary.contains(query) || key.contains(query);
        }

        /**
         * @return The navigation path for the given preference, such as "Player > Action buttons".
         */
        private static String getPreferenceNavigationString(Preference preference) {
            StringBuilder path = new StringBuilder();

            while (true) {
                preference = preference.getParent();

                if (preference == null) {
                    if (path.length() > 0) {
                        path.insert(0, Utils.getTextDirectionString());
                    }
                    return path.toString();
                }

                if (!(preference instanceof NoTitlePreferenceCategory)
                        && !(preference instanceof SponsorBlockPreferenceGroup)) {
                    CharSequence title = preference.getTitle();
                    if (title != null && title.length() > 0) {
                        if (path.length() > 0) {
                            path.insert(0, " > ");
                        }

                        path.insert(0, title);
                    }
                }
            }
        }
    }

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
     * Used for searching preferences.  A Collection of all preferences including nested preferences.
     * Root preferences are excluded (no need to search what's on the root screen),
     * but their sub preferences are included.
     */
    private final Map<Preference, PreferenceSearchData> allPreferences = new LinkedHashMap<>();

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
            sortPreferenceListMenu(Settings.CHANGE_START_PAGE);
            sortPreferenceListMenu(Settings.SPOOF_VIDEO_STREAMS_LANGUAGE);
            sortPreferenceListMenu(BaseSettings.REVANCED_LANGUAGE);

            // If the preference was included, then initialize it based on the available playback speed.
            Preference preference = findPreference(Settings.PLAYBACK_SPEED_DEFAULT.key);
            if (preference instanceof ListPreference playbackPreference) {
                CustomPlaybackSpeedPatch.initializeListPreference(playbackPreference);
            }

            preferenceScreen = getPreferenceScreen();
            Utils.sortPreferenceGroups(preferenceScreen);

            // Store the original structure for restoration after filtering.
            originalPreferenceScreen = getPreferenceManager().createPreferenceScreen(getContext());
            for (int i = 0, count = preferenceScreen.getPreferenceCount(); i < count; i++) {
                originalPreferenceScreen.addPreference(preferenceScreen.getPreference(i));
            }

            setPreferenceScreenToolbar(preferenceScreen);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Called when the fragment starts, ensuring all preferences are collected after initialization.
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            if (allPreferences.isEmpty()) {
                // Must collect preferences on start and not in initialize since
                // legacy SB settings are not loaded yet.
                Logger.printDebug(() -> "Collecting preferences to search");

                // Do not show root menu preferences in search results.
                // Instead search for everything that's not shown when search is not active.
                collectPreferences(preferenceScreen, 1, 0);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onStart failure", ex);
        }
    }

    /**
     * Recursively collects all preferences from the screen or group.
     * @param includeDepth Menu depth to start including preferences.
     *                     A value of 0 adds all preferences.
     */
    private void collectPreferences(PreferenceGroup group, int includeDepth, int currentDepth) {
        for (int i = 0, count = group.getPreferenceCount(); i < count; i++) {
            Preference preference = group.getPreference(i);
            if (includeDepth <= currentDepth && !(preference instanceof PreferenceCategory)) {
                allPreferences.put(preference, new PreferenceSearchData(preference));
            }

            if (preference instanceof PreferenceGroup subGroup) {
                collectPreferences(subGroup, includeDepth, currentDepth + 1);
            }
        }
    }

    /**
     * Filters the preferences using the given query string.
     */
    public void filterPreferences(String query) {
        preferenceScreen.removeAll();

        if (TextUtils.isEmpty(query)) {
            for (int i = 0, count = originalPreferenceScreen.getPreferenceCount(); i < count; i++) {
                preferenceScreen.addPreference(originalPreferenceScreen.getPreference(i));
            }
            return;
        }

        // Navigation path -> Category
        Map<String, PreferenceCategory> categoryMap = new HashMap<>(50);
        String queryLower = Utils.removePunctuationToLowercase(query);

        for (Map.Entry<Preference, PreferenceSearchData> entry : allPreferences.entrySet()) {
            PreferenceSearchData data = entry.getValue();

            if (data.matchesSearchQuery(queryLower)) {
                String navigationPath = data.navigationPath;
                PreferenceCategory group = categoryMap.computeIfAbsent(navigationPath, key -> {
                    PreferenceCategory newGroup = new PreferenceCategory(preferenceScreen.getContext());
                    newGroup.setTitle(navigationPath);
                    preferenceScreen.addPreference(newGroup);
                    return newGroup;
                });
                group.addPreference(entry.getKey());
            }
        }
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

                            // Fix the system navigation bar color for submenus.
                            ThemeHelper.setNavigationBarColor(preferenceScreenDialog.getWindow());

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
