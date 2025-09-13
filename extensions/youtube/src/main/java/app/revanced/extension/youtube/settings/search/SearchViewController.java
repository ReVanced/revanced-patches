package app.revanced.extension.youtube.settings.search;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.preference.*;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.ColorInt;

import java.util.*;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.settings.preference.CustomDialogListPreference;
import app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory;
import app.revanced.extension.youtube.settings.LicenseActivityHook;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategoryListPreference;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

/**
 * Controller for managing the overlay search view in ReVanced settings.
 */
@SuppressWarnings({"deprecation", "DiscouragedApi", "NewApi"})
public class SearchViewController {
    private final SearchView searchView;
    private final FrameLayout searchContainer;
    private final FrameLayout overlayContainer;
    private final Toolbar toolbar;
    private final Activity activity;
    private final ReVancedPreferenceFragment fragment;
    private final CharSequence originalTitle;
    private final SearchResultsAdapter searchResultsAdapter;
    private final List<SearchResultItem> allSearchItems;
    private final List<SearchResultItem> filteredSearchItems;
    private final Map<String, SearchResultItem> keyToSearchItem;
    private final InputMethodManager inputMethodManager;
    private final SearchHistoryManager searchHistoryManager;
    private boolean isSearchActive;
    private boolean isShowingSearchHistory;

    private static final int MAX_SEARCH_RESULTS = 50; // Maximum number of search results displayed.

    private static final int ID_REVANCED_SEARCH_VIEW = getResourceIdentifier("revanced_search_view", "id");
    private static final int ID_REVANCED_SEARCH_VIEW_CONTAINER = getResourceIdentifier("revanced_search_view_container", "id");
    private static final int ID_ACTION_SEARCH = getResourceIdentifier("action_search", "id");
    private static final int ID_REVANCED_SETTINGS_FRAGMENTS = getResourceIdentifier("revanced_settings_fragments", "id");
    public static final int DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON =
            getResourceIdentifier("revanced_settings_search_icon", "drawable");
    private static final int MENU_REVANCED_SEARCH_MENU =
            getResourceIdentifier("revanced_search_menu", "menu");

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
        this.allSearchItems = new ArrayList<>();
        this.filteredSearchItems = new ArrayList<>();
        this.keyToSearchItem = new HashMap<>();
        this.inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        this.isShowingSearchHistory = false;

        // Retrieve SearchView and container from XML.
        searchView = activity.findViewById(ID_REVANCED_SEARCH_VIEW);
        EditText searchEditText = searchView.findViewById(
                searchView.getContext().getResources().getIdentifier(
                        "android:id/search_src_text", null, null));
        // Disable fullscreen keyboard mode.
        searchEditText.setImeOptions(searchEditText.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        searchContainer = activity.findViewById(ID_REVANCED_SEARCH_VIEW_CONTAINER);

        // Create overlay container for search results and history.
        overlayContainer = new FrameLayout(activity);
        overlayContainer.setVisibility(View.GONE);
        overlayContainer.setBackgroundColor(Utils.getAppBackgroundColor());
        overlayContainer.setElevation(Utils.dipToPixels(8));

        // Container for search results.
        FrameLayout searchResultsContainer = new FrameLayout(activity);
        searchResultsContainer.setVisibility(View.VISIBLE);

        // Create a ListView for the results.
        ListView searchResultsListView = new ListView(activity);
        searchResultsListView.setDivider(null);
        searchResultsListView.setDividerHeight(0);
        searchResultsAdapter = new SearchResultsAdapter(activity, filteredSearchItems, fragment, this);
        searchResultsListView.setAdapter(searchResultsAdapter);

        // Add results list into container.
        searchResultsContainer.addView(searchResultsListView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Add results container into overlay.
        overlayContainer.addView(searchResultsContainer, new FrameLayout.LayoutParams(
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

        // Initialize search history manager.
        searchHistoryManager = new SearchHistoryManager(activity, overlayContainer, query -> {
            searchView.setQuery(query, true);
            hideSearchHistory();
        });

        // Set background and query hint.
        searchView.setBackground(createBackgroundDrawable());
        searchView.setQueryHint(str("revanced_settings_search_hint"));

        // Configure RTL support based on app language.
        AppLanguage appLanguage = BaseSettings.REVANCED_LANGUAGE.get();
        if (Utils.isRightToLeftLocale(appLanguage.getLocale())) {
            searchView.setTextDirection(View.TEXT_DIRECTION_RTL);
            searchView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }

        // Set up query text listener.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    String queryTrimmed = query.trim();
                    if (!queryTrimmed.isEmpty()) {
                        searchHistoryManager.saveSearchQuery(queryTrimmed);
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
                    if (!isSearchActive) {
                        Logger.printDebug(() -> "Search is not active, skipping query processing");
                        return true;
                    }

                    if (trimmedText.isEmpty()) {
                        // If empty query: show history.
                        hideSearchResults();
                        showSearchHistory();
                    } else {
                        // If has search text: hide history and show search results.
                        hideSearchHistory();
                        filterAndShowResults(newText);
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
            if (item.getItemId() == ID_ACTION_SEARCH && !isSearchActive) {
                openSearch();
                return true;
            }
            return false;
        });

        // Set navigation click listener.
        toolbar.setNavigationOnClickListener(view -> {
            if (isSearchActive) {
                closeSearch();
            } else {
                activity.finish();
            }
        });
    }

    /**
     * Shows the search history screen.
     */
    private void showSearchHistory() {
        if (searchHistoryManager.isSearchHistoryEnabled()) {
            overlayContainer.setVisibility(View.VISIBLE);
            searchHistoryManager.showSearchHistory();
            isShowingSearchHistory = true;
        } else {
            hideAllOverlays();
        }
    }

    /**
     * Hides the search history screen.
     */
    private void hideSearchHistory() {
        searchHistoryManager.hideSearchHistoryContainer();
        isShowingSearchHistory = false;
    }

    /**
     * Hides all overlay containers.
     */
    private void hideAllOverlays() {
        hideSearchHistory();
        hideSearchResults();
    }

    /**
     * Hides search results overlay only.
     */
    private void hideSearchResults() {
        overlayContainer.setVisibility(View.GONE);

        filteredSearchItems.clear();
        searchResultsAdapter.notifyDataSetChanged();

        // Clear highlighting for all items.
        for (SearchResultItem item : allSearchItems) {
            item.clearHighlighting();
        }
    }

    /**
     * Initializes search data by collecting all searchable preferences from the fragment.
     * This method should be called after the preference fragment is fully loaded.
     * Runs on the UI thread to ensure proper access to preference components.
     */
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
     * Sets up listeners for preferences to keep search results in sync when preference values change.
     */
    private void setupPreferenceListeners() {
        for (SearchResultItem item : allSearchItems) {
            // Skip non-preference items.
            if (!(item instanceof SearchResultItem.PreferenceSearchItem prefItem)) continue;
            Preference pref = prefItem.preference;

            if (pref instanceof ColorPickerPreference colorPref) {
                colorPref.setOnColorChangeListener((prefKey, newColor) -> {
                    SearchResultItem.PreferenceSearchItem searchItem =
                            (SearchResultItem.PreferenceSearchItem) keyToSearchItem.get(prefKey);
                    if (searchItem != null) {
                        searchItem.setColor(newColor | 0xFF000000);
                        refreshSearchResults();
                    }
                });
            } else if (pref instanceof SegmentCategoryListPreference segmentPref) {
                segmentPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    SearchResultItem.PreferenceSearchItem searchItem =
                            (SearchResultItem.PreferenceSearchItem) keyToSearchItem.get(preference.getKey());
                    if (searchItem != null) {
                        searchItem.setColor(segmentPref.getColorWithOpacity());
                        refreshSearchResults();
                    }
                    return true;
                });
            } else if (pref instanceof CustomDialogListPreference listPref) {
                listPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    SearchResultItem.PreferenceSearchItem searchItem =
                            (SearchResultItem.PreferenceSearchItem) keyToSearchItem.get(preference.getKey());
                    if (searchItem == null) return true;

                    int index = listPref.findIndexOfValue(newValue.toString());
                    if (index >= 0) {
                        // Check if a static summary is set.
                        boolean isStaticSummary = listPref.getStaticSummary() != null;

                        if (!isStaticSummary) {
                            // Only update summary if it is not static.
                            CharSequence newSummary = listPref.getEntries()[index];
                            listPref.setSummary(newSummary);
                        }
                    }

                    listPref.clearHighlightedEntriesForDialog();
                    searchItem.refreshHighlighting();
                    refreshSearchResults();
                    return true;
                });
            }
        }
    }

    /**
     * Finds a SearchResultItem by its Preference in all search items.
     */
    public SearchResultItem.PreferenceSearchItem findSearchItemByPreference(Preference preference) {
        // First, search in filtered results.
        for (SearchResultItem item : filteredSearchItems) {
            if (item instanceof SearchResultItem.PreferenceSearchItem prefItem) {
                if (prefItem.preference == preference) {
                    return prefItem;
                }
            }
        }

        // If not found, search in all items.
        for (SearchResultItem item : allSearchItems) {
            if (item instanceof SearchResultItem.PreferenceSearchItem prefItem) {
                if (prefItem.preference == preference) {
                    return prefItem;
                }
            }
        }

        return null;
    }

    /**
     * Refreshes search results if search is active.
     */
    private void refreshSearchResults() {
        if (isSearchActive && !isShowingSearchHistory) {
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
     * Filters all search items based on the provided query and displays results in the overlay.
     * Applies highlighting to matching text and shows a "no results" message if nothing matches.
     */
    private void filterAndShowResults(String query) {
        hideSearchHistory();
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
        Set<String> addedParentKeys = new HashSet<>(2 * matched.size());
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
                                // Apply highlighting to parent items even if they don't match the query.
                                // This ensures they get their current effective summary calculated.
                                parentItem.applyHighlighting(queryPattern);
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

        // Show "No results found" if search results are empty.
        if (filteredSearchItems.isEmpty()) {
            Preference noResultsPreference = new Preference(activity);
            noResultsPreference.setKey("no_results_placeholder");
            noResultsPreference.setTitle(str("revanced_settings_search_no_results_title", query));
            noResultsPreference.setSummary(str("revanced_settings_search_no_results_summary"));
            noResultsPreference.setSelectable(false);
            noResultsPreference.setIcon(DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON);
            filteredSearchItems.add(new SearchResultItem.PreferenceSearchItem(noResultsPreference, "", Collections.emptyList()));
        }

        searchResultsAdapter.notifyDataSetChanged();

        overlayContainer.setVisibility(View.VISIBLE);
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

        // Configure soft input mode to adjust layout and show keyboard.
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        inputMethodManager.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);

        // Always show search history when opening search.
        showSearchHistory();
    }

    /**
     * Closes the search interface and restores the normal UI state.
     * Hides the overlay, clears search results, dismisses the keyboard, and removes highlighting.
     */
    public void closeSearch() {
        isSearchActive = false;
        isShowingSearchHistory = false;

        searchHistoryManager.hideSearchHistoryContainer();
        overlayContainer.setVisibility(View.GONE);

        filteredSearchItems.clear();

        searchContainer.setVisibility(View.GONE);
        toolbar.getMenu().findItem(ID_ACTION_SEARCH).setVisible(true);
        toolbar.setTitle(originalTitle);
        searchView.setQuery("", false);

        // Hide keyboard and reset soft input mode.
        inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Clear highlighting for all search items.
        for (SearchResultItem item : allSearchItems) {
            item.clearHighlighting();
        }

        searchResultsAdapter.notifyDataSetChanged();
    }

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
}
