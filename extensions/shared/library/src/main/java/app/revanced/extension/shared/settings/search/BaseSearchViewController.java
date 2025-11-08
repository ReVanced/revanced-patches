package app.revanced.extension.shared.settings.search;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toolbar;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.settings.preference.CustomDialogListPreference;
import app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory;
import app.revanced.extension.shared.ui.Dim;

/**
 * Abstract controller for managing the overlay search view in ReVanced settings.
 * Subclasses must implement app-specific preference handling.
 */
@SuppressWarnings("deprecation")
public abstract class BaseSearchViewController {
    protected SearchView searchView;
    protected FrameLayout searchContainer;
    protected FrameLayout overlayContainer;
    protected final Toolbar toolbar;
    protected final Activity activity;
    protected final BasePreferenceFragment fragment;
    protected final CharSequence originalTitle;
    protected BaseSearchResultsAdapter searchResultsAdapter;
    protected final List<BaseSearchResultItem> allSearchItems;
    protected final List<BaseSearchResultItem> filteredSearchItems;
    protected final Map<String, BaseSearchResultItem> keyToSearchItem;
    protected final InputMethodManager inputMethodManager;
    protected SearchHistoryManager searchHistoryManager;
    protected boolean isSearchActive;
    protected boolean isShowingSearchHistory;

    protected static final int MAX_SEARCH_RESULTS = 50; // Maximum number of search results displayed.

    protected static final int ID_REVANCED_SEARCH_VIEW = getResourceIdentifierOrThrow("revanced_search_view", "id");
    protected static final int ID_REVANCED_SEARCH_VIEW_CONTAINER = getResourceIdentifierOrThrow("revanced_search_view_container", "id");
    protected static final int ID_ACTION_SEARCH = getResourceIdentifierOrThrow("action_search", "id");
    protected static final int ID_REVANCED_SETTINGS_FRAGMENTS = getResourceIdentifierOrThrow("revanced_settings_fragments", "id");
    public static final int DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON =
            getResourceIdentifierOrThrow("revanced_settings_search_icon", "drawable");
    protected static final int MENU_REVANCED_SEARCH_MENU =
            getResourceIdentifierOrThrow("revanced_search_menu", "menu");

    /**
     * Constructs a new BaseSearchViewController instance.
     *
     * @param activity The activity hosting the search view.
     * @param toolbar  The toolbar containing the search action.
     * @param fragment The preference fragment to manage search preferences.
     */
    protected BaseSearchViewController(Activity activity, Toolbar toolbar, BasePreferenceFragment fragment) {
        this.activity = activity;
        this.toolbar = toolbar;
        this.fragment = fragment;
        this.originalTitle = toolbar.getTitle();
        this.allSearchItems = new ArrayList<>();
        this.filteredSearchItems = new ArrayList<>();
        this.keyToSearchItem = new HashMap<>();
        this.inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        this.isShowingSearchHistory = false;

        // Initialize components
        initializeSearchView();
        initializeOverlayContainer();
        initializeSearchHistoryManager();
        setupToolbarMenu();
        setupListeners();
    }

    /**
     * Initializes the search view with proper configurations, such as background, query hint, and RTL support.
     */
    private void initializeSearchView() {
        // Retrieve SearchView and container from XML.
        searchView = activity.findViewById(ID_REVANCED_SEARCH_VIEW);
        EditText searchEditText = searchView.findViewById(Utils.getResourceIdentifierOrThrow(
                "android:id/search_src_text", null));
        // Disable fullscreen keyboard mode.
        searchEditText.setImeOptions(searchEditText.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        searchContainer = activity.findViewById(ID_REVANCED_SEARCH_VIEW_CONTAINER);

        // Set background and query hint.
        searchView.setBackground(createBackgroundDrawable());
        searchView.setQueryHint(str("revanced_settings_search_hint"));

        // Set text size.
        searchEditText.setTextSize(16);

        // Set cursor color.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setCursorColor(searchEditText);
        }

        // Configure RTL support based on app language.
        AppLanguage appLanguage = BaseSettings.REVANCED_LANGUAGE.get();
        if (Utils.isRightToLeftLocale(appLanguage.getLocale())) {
            searchView.setTextDirection(View.TEXT_DIRECTION_RTL);
            searchView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
    }

    /**
     * Sets the cursor color (for Android 10+ devices).
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setCursorColor(EditText editText) {
        // Get the cursor color based on the current theme.
        final int cursorColor = Utils.isDarkModeEnabled() ? Color.WHITE : Color.BLACK;

        // Create cursor drawable.
        GradientDrawable cursorDrawable = new GradientDrawable();
        cursorDrawable.setShape(GradientDrawable.RECTANGLE);
        cursorDrawable.setSize(Dim.dp2, -1); // Width: 2dp, Height: match text height.
        cursorDrawable.setColor(cursorColor);

        // Set cursor drawable.
        editText.setTextCursorDrawable(cursorDrawable);
    }

    /**
     * Initializes the overlay container for displaying search results and history.
     */
    private void initializeOverlayContainer() {
        // Create overlay container for search results and history.
        overlayContainer = new FrameLayout(activity);
        overlayContainer.setVisibility(View.GONE);
        overlayContainer.setBackgroundColor(Utils.getAppBackgroundColor());
        overlayContainer.setElevation(Dim.dp8);

        // Container for search results.
        FrameLayout searchResultsContainer = new FrameLayout(activity);
        searchResultsContainer.setVisibility(View.VISIBLE);

        // Create a ListView for the results.
        ListView searchResultsListView = new ListView(activity);
        searchResultsListView.setDivider(null);
        searchResultsListView.setDividerHeight(0);
        searchResultsAdapter = createSearchResultsAdapter();
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
    }

    /**
     * Initializes the search history manager with the specified overlay container and listener.
     */
    private void initializeSearchHistoryManager() {
        searchHistoryManager = new SearchHistoryManager(activity, overlayContainer, query -> {
            searchView.setQuery(query, true);
            hideSearchHistory();
        });
    }

    // Abstract methods that subclasses must implement.
    protected abstract BaseSearchResultsAdapter createSearchResultsAdapter();
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected abstract boolean isSpecialPreferenceGroup(Preference preference);
    protected abstract void setupSpecialPreferenceListeners(BaseSearchResultItem item);

    // Abstract interface for preference fragments.
    public interface BasePreferenceFragment {
        PreferenceScreen getPreferenceScreenForSearch();
        android.view.View getView();
        Activity getActivity();
    }

    /**
     * Determines whether a preference should be included in the search index.
     *
     * @param preference   The preference to evaluate.
     * @param currentDepth The current depth in the preference hierarchy.
     * @param includeDepth The maximum depth to include in the search index.
     * @return True if the preference should be included, false otherwise.
     */
    protected boolean shouldIncludePreference(Preference preference, int currentDepth, int includeDepth) {
        return includeDepth <= currentDepth
                && !(preference instanceof PreferenceCategory)
                && !isSpecialPreferenceGroup(preference)
                && !(preference instanceof PreferenceScreen);
    }

    /**
     * Sets up the toolbar menu for the search action.
     */
    protected void setupToolbarMenu() {
        toolbar.inflateMenu(MENU_REVANCED_SEARCH_MENU);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == ID_ACTION_SEARCH && !isSearchActive) {
                openSearch();
                return true;
            }
            return false;
        });
    }

    /**
     * Configures listeners for the search view and toolbar navigation.
     */
    protected void setupListeners() {
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
                    for (BaseSearchResultItem item : allSearchItems) {
                        if (item instanceof BaseSearchResultItem.PreferenceSearchItem prefItem) {
                            String key = prefItem.preference.getKey();
                            if (key != null) {
                                keyToSearchItem.put(key, item);
                            }
                        }
                    }
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
    protected void setupPreferenceListeners() {
        for (BaseSearchResultItem item : allSearchItems) {
            // Skip non-preference items.
            if (!(item instanceof BaseSearchResultItem.PreferenceSearchItem prefItem)) continue;
            Preference pref = prefItem.preference;

            if (pref instanceof ColorPickerPreference colorPref) {
                colorPref.setOnColorChangeListener((prefKey, newColor) -> {
                    BaseSearchResultItem.PreferenceSearchItem searchItem =
                            (BaseSearchResultItem.PreferenceSearchItem) keyToSearchItem.get(prefKey);
                    if (searchItem != null) {
                        searchItem.setColor(newColor);
                        refreshSearchResults();
                    }
                });
            } else if (pref instanceof CustomDialogListPreference listPref) {
                listPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    BaseSearchResultItem.PreferenceSearchItem searchItem =
                            (BaseSearchResultItem.PreferenceSearchItem) keyToSearchItem.get(preference.getKey());
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

            // Let subclasses handle special preferences.
            setupSpecialPreferenceListeners(item);
        }
    }

    /**
     * Collects searchable preferences from a preference group.
     */
    protected void collectSearchablePreferences(PreferenceGroup group) {
        collectSearchablePreferencesWithKeys(group, "", new ArrayList<>(), 1, 0);
    }

    /**
     * Collects searchable preferences with their navigation paths and keys.
     *
     * @param group        The preference group to collect from.
     * @param parentPath   The navigation path of the parent group.
     * @param parentKeys   The keys of parent preferences.
     * @param includeDepth The maximum depth to include in the search index.
     * @param currentDepth The current depth in the preference hierarchy.
     */
    protected void collectSearchablePreferencesWithKeys(PreferenceGroup group, String parentPath,
                                                        List<String> parentKeys, int includeDepth, int currentDepth) {
        if (group == null) return;

        for (int i = 0, count = group.getPreferenceCount(); i < count; i++) {
            Preference preference = group.getPreference(i);

            // Add to search results only if it is not a category, special group, or PreferenceScreen.
            if (shouldIncludePreference(preference, currentDepth, includeDepth)) {
                allSearchItems.add(new BaseSearchResultItem.PreferenceSearchItem(
                        preference, parentPath, parentKeys));
            }

            // If the preference is a group, recurse into it.
            if (preference instanceof PreferenceGroup subGroup) {
                String newPath = parentPath;
                List<String> newKeys = new ArrayList<>(parentKeys);

                // Append the group title to the path and save key for navigation.
                if (!isSpecialPreferenceGroup(preference)
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
    protected void filterAndShowResults(String query) {
        hideSearchHistory();
        // Keep track of the previously displayed items to clear their highlights.
        List<BaseSearchResultItem> previouslyDisplayedItems = new ArrayList<>(filteredSearchItems);

        filteredSearchItems.clear();

        String queryLower = Utils.normalizeTextToLowercase(query);
        Pattern queryPattern = Pattern.compile(Pattern.quote(queryLower), Pattern.CASE_INSENSITIVE);

        // Clear highlighting only for items that were previously visible.
        // This avoids iterating through all items on every keystroke during filtering.
        for (BaseSearchResultItem item : previouslyDisplayedItems) {
            item.clearHighlighting();
        }

        // Collect matched items first.
        List<BaseSearchResultItem> matched = new ArrayList<>();
        int matchCount = 0;
        for (BaseSearchResultItem item : allSearchItems) {
            if (matchCount >= MAX_SEARCH_RESULTS) break; // Stop after collecting max results.
            if (item.matchesQuery(queryLower)) {
                item.applyHighlighting(queryPattern);
                matched.add(item);
                matchCount++;
            }
        }

        // Build filteredSearchItems, inserting parent enablers for disabled dependents.
        Set<String> addedParentKeys = new HashSet<>(2 * matched.size());
        for (BaseSearchResultItem item : matched) {
            if (item instanceof BaseSearchResultItem.PreferenceSearchItem prefItem) {
                String key = prefItem.preference.getKey();
                Setting<?> setting = (key != null) ? Setting.getSettingFromPath(key) : null;
                if (setting != null && !setting.isAvailable()) {
                    List<Setting<?>> parentSettings = setting.getParentSettings();
                    for (Setting<?> parentSetting : parentSettings) {
                        BaseSearchResultItem parentItem = keyToSearchItem.get(parentSetting.key);
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
            //noinspection ComparatorCombinators
            Collections.sort(filteredSearchItems, (o1, o2) ->
                    o1.navigationPath.compareTo(o2.navigationPath)
            );
            List<BaseSearchResultItem> displayItems = new ArrayList<>();
            String currentPath = null;
            for (BaseSearchResultItem item : filteredSearchItems) {
                if (!item.navigationPath.equals(currentPath)) {
                    BaseSearchResultItem header = new BaseSearchResultItem.GroupHeaderItem(item.navigationPath, item.navigationKeys);
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
            filteredSearchItems.add(new BaseSearchResultItem.PreferenceSearchItem(noResultsPreference, "", Collections.emptyList()));
        }

        searchResultsAdapter.notifyDataSetChanged();
        overlayContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Opens the search interface by showing the search view and hiding the menu item.
     * Configures the UI for search mode, shows the keyboard, and displays search suggestions.
     */
    protected void openSearch() {
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
        for (BaseSearchResultItem item : allSearchItems) {
            item.clearHighlighting();
        }

        searchResultsAdapter.notifyDataSetChanged();
    }

    /**
     * Shows the search history if enabled.
     */
    protected void showSearchHistory() {
        if (searchHistoryManager.isSearchHistoryEnabled()) {
            overlayContainer.setVisibility(View.VISIBLE);
            searchHistoryManager.showSearchHistory();
            isShowingSearchHistory = true;
        } else {
            hideAllOverlays();
        }
    }

    /**
     * Hides the search history container.
     */
    protected void hideSearchHistory() {
        searchHistoryManager.hideSearchHistoryContainer();
        isShowingSearchHistory = false;
    }

    /**
     * Hides all overlay containers, including search results and history.
     */
    protected void hideAllOverlays() {
        hideSearchHistory();
        hideSearchResults();
    }

    /**
     * Hides the search results overlay and clears the filtered results.
     */
    protected void hideSearchResults() {
        overlayContainer.setVisibility(View.GONE);
        filteredSearchItems.clear();
        searchResultsAdapter.notifyDataSetChanged();
        for (BaseSearchResultItem item : allSearchItems) {
            item.clearHighlighting();
        }
    }

    /**
     * Refreshes the search results display if the search is active and history is not shown.
     */
    protected void refreshSearchResults() {
        if (isSearchActive && !isShowingSearchHistory) {
            searchResultsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Finds a search item corresponding to the given preference.
     *
     * @param preference The preference to find a search item for.
     * @return The corresponding PreferenceSearchItem, or null if not found.
     */
    public BaseSearchResultItem.PreferenceSearchItem findSearchItemByPreference(Preference preference) {
        // First, search in filtered results.
        for (BaseSearchResultItem item : filteredSearchItems) {
            if (item instanceof BaseSearchResultItem.PreferenceSearchItem prefItem) {
                if (prefItem.preference == preference) {
                    return prefItem;
                }
            }
        }
        // If not found, search in all items.
        for (BaseSearchResultItem item : allSearchItems) {
            if (item instanceof BaseSearchResultItem.PreferenceSearchItem prefItem) {
                if (prefItem.preference == preference) {
                    return prefItem;
                }
            }
        }

        return null;
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
    protected static GradientDrawable createBackgroundDrawable() {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(Dim.dp28);
        background.setColor(getSearchViewBackground());
        return background;
    }

    /**
     * Return if a search is currently active.
     */
    public boolean isSearchActive() {
        return isSearchActive;
    }
}
