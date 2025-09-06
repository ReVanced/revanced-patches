package app.revanced.extension.youtube.settings.search;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.*;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.settings.preference.CustomDialogListPreference;
import app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory;
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.youtube.settings.LicenseActivityHook;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategoryListPreference;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup;

/**
 * Controller for managing the overlay search view in ReVanced settings.
 */
@SuppressWarnings({"deprecated", "DiscouragedApi", "NewApi"})
public class SearchViewController {
    private final SearchView searchView;
    private final FrameLayout searchContainer;
    private final FrameLayout overlayContainer;
    private final Toolbar toolbar;
    private final Activity activity;
    private final ReVancedPreferenceFragment fragment;
    private final CharSequence originalTitle;
    private final Deque<String> searchHistory;
    private final AutoCompleteTextView autoCompleteTextView;
    private final boolean showSettingsSearchHistory;
    private final SearchResultsAdapter searchResultsAdapter;
    private final List<SearchResultItem> allSearchItems;
    private final List<SearchResultItem> filteredSearchItems;
    private final Map<String, SearchResultItem> keyToSearchItem;
    private final InputMethodManager inputMethodManager;

    private boolean isSearchActive;
    private int currentOrientation;

    private static final int MAX_HISTORY_SIZE = 5; // Maximum history items stored in settings, previous ones are erased.
    private static final int MAX_SEARCH_RESULTS = 50; // Maximum number of search results displayed.
    private static final int SEARCH_DROPDOWN_DELAY_MS = 100; // Delay for showing search history suggestions.

    // Resource ID constants.
    private static final int ID_REVANCED_SEARCH_VIEW = getResourceIdentifier("revanced_search_view", "id");
    private static final int ID_REVANCED_SEARCH_VIEW_CONTAINER = getResourceIdentifier("revanced_search_view_container", "id");
    private static final int ID_ACTION_SEARCH = getResourceIdentifier("action_search", "id");
    private static final int ID_REVANCED_SETTINGS_FRAGMENTS = getResourceIdentifier("revanced_settings_fragments", "id");
    private static final int ID_SUGGESTION_TEXT = getResourceIdentifier("suggestion_text", "id");

    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_SUGGESTION_ITEM =
            getResourceIdentifier("revanced_preference_search_suggestion_item", "layout");
    private static final int DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON =
            getResourceIdentifier("revanced_settings_search_icon", "drawable");
    private static final int DRAWABLE_REVANCED_SETTINGS_INFO =
            getResourceIdentifier("revanced_settings_screen_00_about", "drawable");
    private static final int MENU_REVANCED_SEARCH_MENU =
            getResourceIdentifier("revanced_search_menu", "menu");

    /**
     * Gets the background color for search view components based on current theme.
     */
    @ColorInt
    public static int getSearchViewBackground() {
        return Utils.adjustColorBrightness(Utils.getDialogBackgroundColor(), Utils.isDarkModeEnabled() ? 1.11f : 0.95f);
    }

    /**
     * Creates a rounded background drawable for the main search view.
     */
    private static GradientDrawable createBackgroundDrawable() {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(Utils.dipToPixels(28)); // 28dp corner radius.
        background.setColor(getSearchViewBackground());
        return background;
    }

    /**
     * Creates a background drawable for search suggestion items.
     */
    private static GradientDrawable createSuggestionBackgroundDrawable() {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(getSearchViewBackground());
        return background;
    }

    /**
     * Factory method to create and initialize a SearchViewController instance.
     *
     * @param activity The activity containing the search components.
     * @param toolbar  The toolbar where search functionality will be integrated.
     * @param fragment The preference fragment to search within.
     * @return Configured SearchViewController instance.
     */
    public static SearchViewController addSearchViewComponents(Activity activity, Toolbar toolbar,
                                                               ReVancedPreferenceFragment fragment) {
        return new SearchViewController(activity, toolbar, fragment);
    }

    /**
     * Initializes the SearchViewController with all necessary components and listeners.
     * Sets up the search view, overlay container, toolbar menu, and search history functionality.
     *
     * @param activity The parent activity.
     * @param toolbar  The toolbar for search integration.
     * @param fragment The preference fragment to search within.
     */
    private SearchViewController(Activity activity, Toolbar toolbar, ReVancedPreferenceFragment fragment) {
        this.activity = activity;
        this.toolbar = toolbar;
        this.fragment = fragment;
        this.originalTitle = toolbar.getTitle();
        this.showSettingsSearchHistory = Settings.SETTINGS_SEARCH_HISTORY.get();
        this.searchHistory = new LinkedList<>();
        this.currentOrientation = activity.getResources().getConfiguration().orientation;
        this.allSearchItems = new ArrayList<>();
        this.filteredSearchItems = new ArrayList<>();
        this.keyToSearchItem = new HashMap<>();
        this.inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // Initialize search history.
        StringSetting searchEntries = Settings.SETTINGS_SEARCH_ENTRIES;
        if (showSettingsSearchHistory) {
            String entries = searchEntries.get();
            if (!entries.isBlank()) {
                searchHistory.addAll(Arrays.asList(entries.split("\n")));
            }
        } else {
            // Clear old saved history if the user turns off the feature.
            searchEntries.resetToDefault();
        }

        // Retrieve SearchView and container from XML.
        searchView = activity.findViewById(ID_REVANCED_SEARCH_VIEW);
        searchContainer = activity.findViewById(ID_REVANCED_SEARCH_VIEW_CONTAINER);

        // Create overlay container for search results.
        overlayContainer = new FrameLayout(activity);
        overlayContainer.setVisibility(View.GONE);
        overlayContainer.setBackgroundColor(Utils.getAppBackgroundColor());
        overlayContainer.setElevation(Utils.dipToPixels(8));

        // Create search results ListView.
        ListView searchResultsListView = new ListView(activity);
        searchResultsListView.setDivider(null);
        searchResultsListView.setDividerHeight(0);
        searchResultsAdapter = new SearchResultsAdapter(activity, filteredSearchItems, fragment);
        searchResultsListView.setAdapter(searchResultsAdapter);

        // Add ListView to overlay container.
        overlayContainer.addView(searchResultsListView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Add overlay to the main content container.
        FrameLayout mainContainer = activity.findViewById(ID_REVANCED_SETTINGS_FRAGMENTS);
        if (mainContainer != null) {
            FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            overlayParams.gravity = Gravity.TOP;
            mainContainer.addView(overlayContainer, overlayParams);
        }

        // Initialize AutoCompleteTextView.
        autoCompleteTextView = searchView.findViewById(
                searchView.getContext().getResources().getIdentifier(
                        "android:id/search_src_text", null, null));

        // Disable fullscreen keyboard mode.
        autoCompleteTextView.setImeOptions(autoCompleteTextView.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Set background and query hint.
        searchView.setBackground(createBackgroundDrawable());
        searchView.setQueryHint(str("revanced_settings_search_hint"));

        // Configure RTL support based on app language.
        AppLanguage appLanguage = BaseSettings.REVANCED_LANGUAGE.get();
        if (Utils.isRightToLeftLocale(appLanguage.getLocale())) {
            searchView.setTextDirection(View.TEXT_DIRECTION_RTL);
            searchView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }

        // Set up search history suggestions.
        if (showSettingsSearchHistory) {
            setupSearchHistory();
        }

        // Set up query text listener.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    String queryTrimmed = query.trim();
                    if (!queryTrimmed.isEmpty()) {
                        saveSearchQuery(queryTrimmed);
                    }
                    // Hide suggestions on submit.
                    if (showSettingsSearchHistory) {
                        autoCompleteTextView.dismissDropDown();
                    }
                } catch (Exception ex) {
                    Logger.printException(() -> "onQueryTextSubmit failure", ex);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    Logger.printDebug(() -> "Search query: " + newText);

                    String trimmedText = newText.trim();
                    if (trimmedText.isEmpty()) { // Consider spaces as an empty query.
                        overlayContainer.setVisibility(View.GONE);
                        filteredSearchItems.clear();
                        searchResultsAdapter.notifyDataSetChanged();

                        // Clear highlighting when query becomes empty.
                        for (SearchResultItem item : allSearchItems) {
                            item.clearHighlighting();
                        }

                        // Re-enable suggestions for empty input.
                        if (showSettingsSearchHistory) {
                            autoCompleteTextView.setThreshold(1);
                        }
                    } else {
                        filterAndShowResults(newText);

                        // Disable suggestions during text input.
                        if (showSettingsSearchHistory) {
                            autoCompleteTextView.dismissDropDown();
                            autoCompleteTextView.setThreshold(Integer.MAX_VALUE); // Disable autocomplete suggestions.
                        }
                    }
                } catch (Exception ex) {
                    Logger.printException(() -> "onQueryTextChange failure", ex);
                }
                return true;
            }
        });

        // Set up menu to toolbar.
        toolbar.inflateMenu(MENU_REVANCED_SEARCH_MENU);

        // Set menu item click listener.
        toolbar.setOnMenuItemClickListener(item -> {
            try {
                if (item.getItemId() == ID_ACTION_SEARCH && !isSearchActive) {
                    openSearch();
                    return true;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "menu click failure", ex);
            }
            return false;
        });

        // Set navigation click listener.
        toolbar.setNavigationOnClickListener(view -> {
            try {
                if (isSearchActive) {
                    closeSearch();
                } else {
                    activity.finish();
                }
            } catch (Exception ex) {
                Logger.printException(() -> "navigation click failure", ex);
            }
        });
    }

    /**
     * Initializes search data by collecting all searchable preferences from the fragment.
     * This method should be called after the preference fragment is fully loaded.
     * Runs on the UI thread to ensure proper access to preference components.
     */
    @SuppressWarnings("deprecation")
    public void initializeSearchData() {
        allSearchItems.clear();
        keyToSearchItem.clear();

        // Wait until fragment is properly initialized.
        activity.runOnUiThread(() -> {
            try {
                PreferenceScreen screen = fragment.getPreferenceScreenForSearch();
                if (screen != null) {
                    collectSearchablePreferences(screen);
                    for (SearchResultItem item : allSearchItems) {
                        // Only PreferenceSearchItem instances have keys.
                        if (item instanceof SearchResultItem.PreferenceSearchItem prefItem) {
                            String key = prefItem.preference.getKey();
                            if (key != null) {
                                keyToSearchItem.put(key, item);
                            }
                        }
                    }
                    // Set up listeners.
                    setupPreferenceListeners();
                    Logger.printDebug(() -> "Collected " + allSearchItems.size() + " searchable preferences");
                }
            } catch (Exception ex) {
                Logger.printException(() -> "Failed to initialize search data", ex);
            }
        });
    }

    /**
     * Sets up listeners for preferences (e.g., ColorPickerPreference, CustomDialogListPreference)
     * to keep search results in sync when preference values change.
     */
    @SuppressWarnings("deprecation")
    private void setupPreferenceListeners() {
        for (SearchResultItem item : allSearchItems) {
            // Skip non-preference items.
            if (!(item instanceof SearchResultItem.PreferenceSearchItem prefItem)) continue;
            Preference pref = prefItem.preference;

            if (pref instanceof ColorPickerPreference colorPref) {
                colorPref.setOnColorChangeListener((prefKey, newColor) -> {
                    SearchResultItem.PreferenceSearchItem searchItem = (SearchResultItem.PreferenceSearchItem) keyToSearchItem.get(prefKey);
                    if (searchItem != null) {
                        searchItem.setColor(newColor | 0xFF000000);
                        refreshSearchResults();
                    }
                });
            } else if (pref instanceof SegmentCategoryListPreference segmentPref) {
                segmentPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    SearchResultItem.PreferenceSearchItem searchItem = (SearchResultItem.PreferenceSearchItem) keyToSearchItem.get(preference.getKey());
                    if (searchItem != null) {
                        searchItem.setColor(segmentPref.getColorWithOpacity());
                        refreshSearchResults();
                    }
                    return true;
                });
            } else if (pref instanceof CustomDialogListPreference listPref) {
                listPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    SearchResultItem.PreferenceSearchItem searchItem = (SearchResultItem.PreferenceSearchItem) keyToSearchItem.get(preference.getKey());
                    if (searchItem == null) return true;

                    int index = listPref.findIndexOfValue(newValue.toString());
                    if (index >= 0) {
                        // Check if a static summary is set.
                        boolean isStaticSummary = listPref.getStaticSummary() != null;

                        if (!isStaticSummary) {
                            // Only update summary if it is not static.
                            CharSequence newSummary = listPref.getEntries()[index];
                            searchItem.updateOriginalSummary(newSummary);
                            listPref.setSummary(newSummary);
                        }

                        searchItem.clearHighlighting();
                    }

                    refreshSearchResults();
                    return true;
                });
            }
        }
    }

    /**
     * Refreshes search results if search is active.
     */
    private void refreshSearchResults() {
        if (isSearchActive) {
            searchResultsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Collect all searchable preferences with key-based navigation support.
     */
    private void collectSearchablePreferences(PreferenceGroup group) {
        collectSearchablePreferencesWithKeys(group, "", new ArrayList<>(), 1, 0);
    }

    /**
     * Recursively collects all searchable preferences from a preference group with navigation keys.
     * Builds navigation paths for each preference and filters out non-searchable items.
     *
     * @param group        The preference group to search within.
     * @param parentPath   The navigation path to this group.
     * @param parentKeys   The list of navigation keys to this group.
     * @param includeDepth The minimum depth at which to include preferences.
     * @param currentDepth The current recursion depth.
     */
    @SuppressWarnings("deprecation")
    private void collectSearchablePreferencesWithKeys(PreferenceGroup group, String parentPath,
                                                      List<String> parentKeys, int includeDepth, int currentDepth) {
        if (group == null) return;

        for (int i = 0, count = group.getPreferenceCount(); i < count; i++) {
            Preference preference = group.getPreference(i);

            // Add to search results only if it is not a category, special group, or PreferenceScreen.
            if (includeDepth <= currentDepth
                    && !(preference instanceof PreferenceCategory)
                    && !(preference instanceof SponsorBlockPreferenceGroup)
                    && !(preference instanceof PreferenceScreen)) {
                allSearchItems.add(new SearchResultItem.PreferenceSearchItem(preference, parentPath, parentKeys));
            }

            // If the preference is a group, recurse into it.
            if (preference instanceof PreferenceGroup subGroup) {
                String newPath = parentPath;
                List<String> newKeys = new ArrayList<>(parentKeys);

                // Append the group title to the path and save key for navigation.
                if (!(preference instanceof SponsorBlockPreferenceGroup)
                        && !(preference instanceof NoTitlePreferenceCategory)) {
                    CharSequence title = preference.getTitle();
                    if (!TextUtils.isEmpty(title)) {
                        newPath = TextUtils.isEmpty(parentPath)
                                ? title.toString()
                                : parentPath + " > " + title;
                    }

                    // Add key for navigation if this is a PreferenceScreen or group with navigation capability.
                    String key = preference.getKey();
                    if (!TextUtils.isEmpty(key) && (preference instanceof PreferenceScreen
                            || searchResultsAdapter.hasNavigationCapability(preference))) {
                        newKeys.add(key);
                    }
                }

                collectSearchablePreferencesWithKeys(subGroup, newPath, newKeys, includeDepth, currentDepth + 1);
            }
        }
    }

    /**
     * Helper method to get all keys from current screen (for debugging).
     */
    @SuppressWarnings("deprecation")
    private void logAllPreferenceKeys(PreferenceGroup group, String prefix) {
        if (group == null) return;

        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference pref = group.getPreference(i);
            String key = pref.getKey();
            CharSequence title = pref.getTitle();

            Logger.printDebug(() -> prefix + "Key: '" + key + "', Title: '" + title +
                    "', Type: " + pref.getClass().getSimpleName());

            if (pref instanceof PreferenceGroup) {
                logAllPreferenceKeys((PreferenceGroup) pref, prefix + "  ");
            }
        }
    }

    /**
     * Filters all search items based on the provided query and displays results in the overlay.
     * Applies highlighting to matching text and shows a "no results" message if nothing matches.
     */
    @SuppressWarnings("deprecation")
    private void filterAndShowResults(String query) {
        // Keep track of the previously displayed items to clear their highlights.
        List<SearchResultItem> previouslyDisplayedItems = new ArrayList<>(filteredSearchItems);

        filteredSearchItems.clear();

        String queryLower = Utils.removePunctuationToLowercase(query);
        Pattern queryPattern = Pattern.compile(Pattern.quote(queryLower), Pattern.CASE_INSENSITIVE);

        // Clear highlighting only for items that were previously visible.
        // This avoids iterating through all items on every keystroke during filtering.
        for (SearchResultItem item : previouslyDisplayedItems) {
            item.clearHighlighting();
        }

        // Collect matched items first.
        List<SearchResultItem> matched = new ArrayList<>();
        int matchCount = 0;
        for (SearchResultItem item : allSearchItems) {
            if (matchCount >= MAX_SEARCH_RESULTS) break; // Stop after collecting max results.
            if (item.matchesQuery(queryLower)) {
                item.applyHighlighting(queryPattern);
                matched.add(item);
                matchCount++;
            }
        }

        // Build filteredSearchItems, inserting parent enablers for disabled dependents.
        Set<String> addedParentKeys = new HashSet<>();
        for (SearchResultItem item : matched) {
            if (item instanceof SearchResultItem.PreferenceSearchItem prefItem) {
                String key = prefItem.preference.getKey();
                Setting<?> setting = (key != null) ? Setting.getSettingFromPath(key) : null;
                if (setting != null && !setting.isAvailable()) {
                    List<Setting<?>> parentSettings = setting.getParentSettings();
                    for (Setting<?> parentSetting : parentSettings) {
                        SearchResultItem parentItem = keyToSearchItem.get(parentSetting.key);
                        if (parentItem != null && !addedParentKeys.contains(parentSetting.key)) {
                            if (!parentItem.matchesQuery(queryLower)) {
                                filteredSearchItems.add(parentItem);
                            }
                            addedParentKeys.add(parentSetting.key);
                        }
                    }
                }
                filteredSearchItems.add(item);
                if (key != null) {
                    addedParentKeys.add(key);
                }
            }
        }

        if (!filteredSearchItems.isEmpty()) {
            filteredSearchItems.sort(Comparator.comparing(o -> o.navigationPath));
            List<SearchResultItem> displayItems = new ArrayList<>();
            String currentPath = null;
            for (SearchResultItem item : filteredSearchItems) {
                if (!item.navigationPath.equals(currentPath)) {
                    SearchResultItem header = new SearchResultItem.GroupHeaderItem(item.navigationPath, item.navigationKeys);
                    displayItems.add(header);
                    currentPath = item.navigationPath;
                }
                displayItems.add(item);
            }
            filteredSearchItems.clear();
            filteredSearchItems.addAll(displayItems);
        }

        // Show 'No results found' and "Search Tips" if search results are empty.
        if (filteredSearchItems.isEmpty()) {
            for (Preference pref : createNoResultsPreferences(query)) {
                filteredSearchItems.add(new SearchResultItem.PreferenceSearchItem(pref, "", Collections.emptyList()));
            }
        }

        searchResultsAdapter.notifyDataSetChanged();

        overlayContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Creates Preferences for "No results" and "Search Tips".
     */
    @SuppressWarnings("deprecation")
    private List<Preference> createNoResultsPreferences(String query) {
        List<Preference> prefs = new ArrayList<>();

        // "No results" card.
        Preference noResultsPreference = new Preference(activity);
        noResultsPreference.setKey("no_results_placeholder");
        noResultsPreference.setTitle(str("revanced_settings_search_no_results_title", query));
        noResultsPreference.setSummary(str("revanced_settings_search_no_results_summary"));
        noResultsPreference.setSelectable(false);
        noResultsPreference.setIcon(DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON);
        prefs.add(noResultsPreference);

        // "Search Tips" card.
        Preference tipsPreference = new Preference(activity);
        tipsPreference.setKey("search_tips_placeholder");
        tipsPreference.setTitle(str("revanced_settings_search_tips_title"));
        tipsPreference.setSummary(str("revanced_settings_search_tips_summary"));
        tipsPreference.setSelectable(false);
        tipsPreference.setIcon(DRAWABLE_REVANCED_SETTINGS_INFO);
        prefs.add(tipsPreference);

        return prefs;
    }

    /**
     * Sets up the search history functionality with autocomplete suggestions.
     * Configures the AutoCompleteTextView with a custom adapter and focus listeners.
     */
    private void setupSearchHistory() {
        if (autoCompleteTextView == null) return;

        // Create adapter for search history.
        SearchHistoryAdapter adapter = new SearchHistoryAdapter(activity, new ArrayList<>(searchHistory));
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1); // Initial threshold for empty input.
        autoCompleteTextView.setLongClickable(true);

        // Show suggestions only when search bar is active and query is empty.
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isSearchActive && autoCompleteTextView.getText().length() == 0) {
                autoCompleteTextView.showDropDown();
            }
        });
    }

    /**
     * Saves a search query to the search history.
     * Manages the history size limit and updates the autocomplete adapter.
     */
    private void saveSearchQuery(String query) {
        if (!showSettingsSearchHistory) return;

        searchHistory.remove(query); // Remove if already exists to update position.
        searchHistory.addFirst(query); // Add to the most recent.

        // Remove extra old entries.
        while (searchHistory.size() > MAX_HISTORY_SIZE) {
            String last = searchHistory.removeLast();
            Logger.printDebug(() -> "Removing search history query: " + last);
        }

        saveSearchHistory();

        updateSearchHistoryAdapter();
    }

    /**
     * Removes a search query from the search history.
     */
    private void removeSearchQuery(String query) {
        searchHistory.remove(query);

        saveSearchHistory();

        updateSearchHistoryAdapter();
    }

    /**
     * Save the search history to the shared preferences.
     */
    private void saveSearchHistory() {
        Logger.printDebug(() -> "Saving search history: " + searchHistory);
        Settings.SETTINGS_SEARCH_ENTRIES.save(String.join("\n", searchHistory));
    }

    /**
     * Updates the search history autocomplete adapter with the latest history data.
     * Refreshes the dropdown suggestions to reflect recent changes.
     */
    private void updateSearchHistoryAdapter() {
        if (autoCompleteTextView == null) return;

        SearchHistoryAdapter adapter = (SearchHistoryAdapter) autoCompleteTextView.getAdapter();
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(searchHistory);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Handles orientation changes by dismissing any open dropdown menus.
     */
    public void handleOrientationChange(int newOrientation) {
        if (newOrientation != currentOrientation) {
            currentOrientation = newOrientation;
            if (autoCompleteTextView != null) {
                autoCompleteTextView.dismissDropDown();
                Logger.printDebug(() -> "Orientation changed, search history dismissed");
            }
        }
    }

    /**
     * Opens the search interface by showing the search view and hiding the menu item.
     * Configures the UI for search mode, shows the keyboard, and displays search suggestions.
     */
    private void openSearch() {
        isSearchActive = true;
        toolbar.getMenu().findItem(ID_ACTION_SEARCH).setVisible(false);
        toolbar.setTitle("");
        searchContainer.setVisibility(View.VISIBLE);
        searchView.requestFocus();

        // Show keyboard.
        inputMethodManager.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);

        // Show suggestions with a slight delay.
        if (showSettingsSearchHistory && autoCompleteTextView != null && autoCompleteTextView.getText().length() == 0) {
            searchView.postDelayed(() -> {
                if (isSearchActive && autoCompleteTextView.getText().length() == 0) {
                    autoCompleteTextView.showDropDown();
                }
            }, SEARCH_DROPDOWN_DELAY_MS);
        }
    }

    /**
     * Closes the search interface and restores the normal UI state.
     * Hides the overlay, clears search results, dismisses the keyboard, and removes highlighting.
     */
    public void closeSearch() {
        isSearchActive = false;
        toolbar.getMenu().findItem(ID_ACTION_SEARCH).setVisible(true);
        toolbar.setTitle(originalTitle);
        searchContainer.setVisibility(View.GONE);
        overlayContainer.setVisibility(View.GONE);
        searchView.setQuery("", false);

        // Hide keyboard.
        inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

        // Clear search results.
        filteredSearchItems.clear();
        searchResultsAdapter.notifyDataSetChanged();

        // Clear highlighting for all search items.
        for (SearchResultItem item : allSearchItems) {
            item.clearHighlighting();
        }
    }

    /**
     * Injection point.
     */
    @SuppressWarnings("unused")
    public static boolean handleBackPress() {
        if (LicenseActivityHook.searchViewController != null
                && LicenseActivityHook.searchViewController.isSearchActive()) {
            LicenseActivityHook.searchViewController.closeSearch();
            return true;
        }
        return false;
    }

    /**
     * Return if a search is currently active.
     */
    public boolean isSearchActive() {
        return isSearchActive;
    }

    /**
     * Custom ArrayAdapter for search history.
     */
    private class SearchHistoryAdapter extends ArrayAdapter<String> {
        public SearchHistoryAdapter(Context context, List<String> history) {
            super(context, 0, history);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LinearLayout.inflate(getContext(), LAYOUT_REVANCED_PREFERENCE_SEARCH_SUGGESTION_ITEM, null);
            }

            // Set query text.
            String query = getItem(position);
            TextView textView = convertView.findViewById(ID_SUGGESTION_TEXT);
            if (textView != null) {
                textView.setText(query);
            }

            // Create ripple effect.
            int rippleColor = Utils.adjustColorBrightness(getSearchViewBackground(), Utils.isDarkModeEnabled() ? 1.25f : 0.90f);
            RippleDrawable rippleBackground = new RippleDrawable(
                    ColorStateList.valueOf(rippleColor),
                    createSuggestionBackgroundDrawable(),
                    null
            );
            convertView.setBackground(rippleBackground);

            // Set click listener for inserting query into SearchView.
            convertView.setOnClickListener(v -> searchView.setQuery(query, true));

            // Set long click listener for deletion confirmation.
            convertView.setOnLongClickListener(v -> {
                Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                        activity,
                        query,                                // Title.
                        str("revanced_settings_search_remove_message"), // Message.
                        null,                                 // No EditText.
                        null,                                 // OK button text.
                        () -> removeSearchQuery(query),       // OK button action.
                        () -> {},                             // Cancel button action (dismiss only).
                        null,                                 // No Neutral button text.
                        () -> {},                             // Neutral button action (dismiss only).
                        true                                  // Dismiss dialog when onNeutralClick.
                );

                Dialog dialog = dialogPair.first;
                dialog.setCancelable(true); // Allow dismissal via back button.
                dialog.show(); // Show the dialog.
                return true;
            });

            return convertView;
        }
    }
}
