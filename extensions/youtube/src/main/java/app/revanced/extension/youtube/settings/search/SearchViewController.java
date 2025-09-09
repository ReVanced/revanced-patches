package app.revanced.extension.youtube.settings.search;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.util.Pair;
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
@SuppressWarnings({"deprecation", "DiscouragedApi", "NewApi"})
public class SearchViewController {
    private final SearchView searchView;
    private final FrameLayout searchContainer;
    private final FrameLayout overlayContainer;
    private final Toolbar toolbar;
    private final Activity activity;
    private final ReVancedPreferenceFragment fragment;
    private final CharSequence originalTitle;
    private final Deque<String> searchHistory;
    private final boolean showSettingsSearchHistory;
    private final SearchResultsAdapter searchResultsAdapter;
    private final List<SearchResultItem> allSearchItems;
    private final List<SearchResultItem> filteredSearchItems;
    private final Map<String, SearchResultItem> keyToSearchItem;
    private final InputMethodManager inputMethodManager;
    private final FrameLayout searchHistoryContainer;
    private final SearchHistoryAdapter searchHistoryAdapter;

    private boolean isSearchActive;
    private boolean isShowingSearchHistory;

    private static final int MAX_HISTORY_SIZE = 5; // Maximum history items stored in settings, previous ones are erased.
    private static final int MAX_SEARCH_RESULTS = 50; // Maximum number of search results displayed.

    // Resource ID constants.
    private static final int ID_REVANCED_SEARCH_VIEW = getResourceIdentifier("revanced_search_view", "id");
    private static final int ID_REVANCED_SEARCH_VIEW_CONTAINER = getResourceIdentifier("revanced_search_view_container", "id");
    private static final int ID_ACTION_SEARCH = getResourceIdentifier("action_search", "id");
    private static final int ID_REVANCED_SETTINGS_FRAGMENTS = getResourceIdentifier("revanced_settings_fragments", "id");
    private static final int ID_SEARCH_HISTORY_LIST = getResourceIdentifier("search_history_list", "id");
    private static final int ID_SEARCH_TIPS_SUMMARY = getResourceIdentifier("revanced_settings_search_tips_summary", "id");
    private static final int ID_CLEAR_HISTORY_BUTTON = getResourceIdentifier("clear_history_button", "id");
    private static final int ID_HISTORY_TEXT = getResourceIdentifier("history_text", "id");
    private static final int ID_DELETE_ICON = getResourceIdentifier("delete_icon", "id");

    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_HISTORY_SCREEN =
            getResourceIdentifier("revanced_preference_search_history_screen", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_HISTORY_ITEM =
            getResourceIdentifier("revanced_preference_search_history_item", "layout");
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
        this.showSettingsSearchHistory = Settings.SETTINGS_SEARCH_HISTORY.get();
        this.searchHistory = new LinkedList<>();
        this.allSearchItems = new ArrayList<>();
        this.filteredSearchItems = new ArrayList<>();
        this.keyToSearchItem = new HashMap<>();
        this.inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        this.isShowingSearchHistory = false;

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
        searchResultsAdapter = new SearchResultsAdapter(activity, filteredSearchItems, fragment);
        searchResultsListView.setAdapter(searchResultsAdapter);

        // Add results list into container.
        searchResultsContainer.addView(searchResultsListView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Add results container into overlay.
        overlayContainer.addView(searchResultsContainer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Create search history container inside overlay.
        searchHistoryContainer = new FrameLayout(activity);
        searchHistoryContainer.setVisibility(View.GONE);

        // Inflate search history layout.
        LayoutInflater inflater = LayoutInflater.from(activity);
        View historyView = inflater.inflate(LAYOUT_REVANCED_PREFERENCE_SEARCH_HISTORY_SCREEN, searchHistoryContainer, false);

        // Set up history adapter.
        LinearLayout searchHistoryListView = historyView.findViewById(ID_SEARCH_HISTORY_LIST);
        searchHistoryAdapter = new SearchHistoryAdapter(activity, searchHistoryListView, new ArrayList<>(searchHistory));

        // Set up clear history button.
        TextView clearHistoryButton = historyView.findViewById(ID_CLEAR_HISTORY_BUTTON);
        clearHistoryButton.setOnClickListener(v -> showClearHistoryDialog());

        // Add inflated history layout to container.
        searchHistoryContainer.addView(historyView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Add history container to overlay.
        overlayContainer.addView(searchHistoryContainer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Finally add overlay to the main content container.
        FrameLayout mainContainer = activity.findViewById(ID_REVANCED_SETTINGS_FRAGMENTS);
        if (mainContainer != null) {
            FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            overlayParams.gravity = Gravity.TOP;
            mainContainer.addView(overlayContainer, overlayParams);
        }

        // Set background and query hint.
        searchView.setBackground(createBackgroundDrawable());
        searchView.setQueryHint(str("revanced_settings_search_hint"));

        // Configure RTL support based on app language.
        AppLanguage appLanguage = BaseSettings.REVANCED_LANGUAGE.get();
        if (Utils.isRightToLeftLocale(appLanguage.getLocale())) {
            searchView.setTextDirection(View.TEXT_DIRECTION_RTL);
            searchView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }

        // Add bullet points to search tips summary.
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (String item : str("revanced_settings_search_tips_summary").split("\\n\\s*\\n")) {
            final int start = builder.length();
            builder.append(item);
            builder.setSpan(new BulletSpan(20), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append("\n");
        }
        TextView tipsSummary = historyView.findViewById(ID_SEARCH_TIPS_SUMMARY);
        tipsSummary.setText(builder);

        // Set up query text listener.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    String queryTrimmed = query.trim();
                    if (!queryTrimmed.isEmpty()) {
                        saveSearchQuery(queryTrimmed);
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

                    if (trimmedText.isEmpty()) { // Consider spaces as an empty query.
                        filteredSearchItems.clear();
                        searchResultsAdapter.notifyDataSetChanged();

                        for (SearchResultItem item : allSearchItems) {
                            item.clearHighlighting();
                        }

                        if (showSettingsSearchHistory && !searchHistory.isEmpty()) {
                            showSearchHistory();
                        } else {
                            hideAllOverlays();
                        }
                    } else {
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
     * Shows the search history screen.
     */
    private void showSearchHistory() {
        if (!showSettingsSearchHistory || searchHistory.isEmpty()) {
            return;
        }

        isShowingSearchHistory = true;

        // Update the adapter.
        searchHistoryAdapter.clear();
        searchHistoryAdapter.addAll(searchHistory);
        searchHistoryAdapter.notifyDataSetChanged();

        // Show containers.
        searchHistoryContainer.setVisibility(View.VISIBLE);
        overlayContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the search history screen.
     */
    private void hideSearchHistory() {
        searchHistoryContainer.setVisibility(View.GONE);
        isShowingSearchHistory = false;
    }

    /**
     * Hides all overlay containers.
     */
    private void hideAllOverlays() {
        searchHistoryContainer.setVisibility(View.GONE);
        isShowingSearchHistory = false;

        overlayContainer.setVisibility(View.GONE);

        filteredSearchItems.clear();
        searchResultsAdapter.notifyDataSetChanged();
    }

    /**
     * Shows confirmation dialog for clearing search history.
     */
    private void showClearHistoryDialog() {
        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                activity,
                str("revanced_settings_search_clear_history"),
                str("revanced_settings_search_clear_history_message"),
                null,
                null,
                this::clearAllSearchHistory,
                () -> {
                },
                null,
                null,
                false
        );

        Dialog dialog = dialogPair.first;
        dialog.setCancelable(true);
        dialog.show();
    }

    /**
     * Clears all search history.
     */
    private void clearAllSearchHistory() {
        searchHistory.clear();
        saveSearchHistory();
        searchHistoryAdapter.clear();
        searchHistoryAdapter.notifyDataSetChanged();

        // If currently showing history and it's now empty, hide it.
        if (isShowingSearchHistory) {
            hideAllOverlays();
        }
    }

    /**
     * Custom adapter for search history items.
     */
    private class SearchHistoryAdapter {
        private final List<String> history;
        private final LayoutInflater inflater;
        private final LinearLayout container;

        public SearchHistoryAdapter(Context context, LinearLayout container, List<String> history) {
            this.history = history;
            this.inflater = LayoutInflater.from(context);
            this.container = container;
        }

        /**
         * Updates the container with current history items.
         */
        public void notifyDataSetChanged() {
            container.removeAllViews();
            for (String query : history) {
                View view = inflater.inflate(LAYOUT_REVANCED_PREFERENCE_SEARCH_HISTORY_ITEM, container, false);

                TextView historyText = view.findViewById(ID_HISTORY_TEXT);
                ImageView deleteIcon = view.findViewById(ID_DELETE_ICON);

                historyText.setText(query);

                // Set click listener for main item (select query).
                view.setOnClickListener(v -> {
                    searchView.setQuery(query, true);
                    hideSearchHistory();
                });

                // Set click listener for delete icon.
                deleteIcon.setOnClickListener(v -> {
                    Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                            activity,
                            query,
                            str("revanced_settings_search_remove_message"),
                            null,
                            null,
                            () -> {
                                removeSearchQuery(query);
                                remove(query);
                                notifyDataSetChanged();

                                // If history is now empty, hide the screen.
                                if (history.isEmpty()) {
                                    hideAllOverlays();
                                }
                            }, // OK button action.
                            () -> {}, // Cancel button action (dismiss only).
                            null,
                            null,
                            false
                    );

                    Dialog dialog = dialogPair.first;
                    dialog.setCancelable(true); // Allow dismissal via back button.
                    dialog.show(); // Show the dialog.
                });

                container.addView(view);
            }
        }

        /**
         * Clears all views from the container and history list.
         */
        public void clear() {
            history.clear();
            container.removeAllViews();
        }

        /**
         * Adds all provided history items to the container.
         */
        public void addAll(Collection<String> items) {
            history.addAll(items);
            notifyDataSetChanged();
        }

        /**
         * Removes a query from the history and updates the container.
         */
        public void remove(String query) {
            history.remove(query);
            notifyDataSetChanged();
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
     * Sets up listeners for preferences (e.g., ColorPickerPreference, CustomDialogListPreference)
     * to keep search results in sync when preference values change.
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
     * Helper method to get all keys from current screen (for debugging).
     */
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
     * Saves a search query to the search history. Manages the history size limit.
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
    }

    /**
     * Removes a search query from the search history.
     */
    private void removeSearchQuery(String query) {
        searchHistory.remove(query);

        saveSearchHistory();
    }

    /**
     * Save the search history to the shared preferences.
     */
    private void saveSearchHistory() {
        Logger.printDebug(() -> "Saving search history: " + searchHistory);
        Settings.SETTINGS_SEARCH_ENTRIES.save(String.join("\n", searchHistory));
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

        // Show search history if enabled and has history.
        if (showSettingsSearchHistory && !searchHistory.isEmpty()) {
            showSearchHistory();
        }
    }

    /**
     * Closes the search interface and restores the normal UI state.
     * Hides the overlay, clears search results, dismisses the keyboard, and removes highlighting.
     */
    public void closeSearch() {
        isSearchActive = false;
        isShowingSearchHistory = false;

        searchHistoryContainer.setVisibility(View.GONE);
        overlayContainer.setVisibility(View.GONE);

        filteredSearchItems.clear();

        searchContainer.setVisibility(View.GONE);
        toolbar.getMenu().findItem(ID_ACTION_SEARCH).setVisible(true);
        toolbar.setTitle(originalTitle);
        searchView.setQuery("", false);

        // Hide keyboard and reset soft input mode.
        inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Clear highlighting for all search items and force restore original entries.
        for (SearchResultItem item : allSearchItems) {
            item.clearHighlighting();

            // Additional explicit restoration to handle cached entries.
            if (item instanceof SearchResultItem.PreferenceSearchItem prefItem
                    && prefItem.preference instanceof CustomDialogListPreference listPref) {
                listPref.restoreOriginalEntries();
            }
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
