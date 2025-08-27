package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.preference.*;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup;

/**
 * Controller for managing the overlay search view in ReVanced settings.
 */
@SuppressWarnings({"deprecated", "DiscouragedApi"})
public class SearchViewController {
    private static final int MAX_HISTORY_SIZE = 5;

    private final SearchView searchView;
    private final FrameLayout searchContainer;
    private final FrameLayout overlayContainer;
    private final Toolbar toolbar;
    private final Activity activity;
    private final ReVancedPreferenceFragment fragment;
    private boolean isSearchActive;
    private final CharSequence originalTitle;
    private final Deque<String> searchHistory;
    private final AutoCompleteTextView autoCompleteTextView;
    private final boolean showSettingsSearchHistory;
    private int currentOrientation;
    private final SearchResultsAdapter searchResultsAdapter;
    private final List<SearchResultItem> allSearchItems;
    private final List<SearchResultItem> filteredSearchItems;

    /**
     * Data class for search result items.
     */
    @SuppressWarnings("deprecation")
    private static class SearchResultItem {
        final Preference preference;
        CharSequence title;
        CharSequence summary;
        final String navigationPath;
        final String searchableText;
        final int preferenceType;

        @Nullable
        private final CharSequence originalTitle;
        @Nullable
        private final CharSequence originalSummary;
        @Nullable
        private CharSequence originalSummaryOn;
        @Nullable
        private CharSequence originalSummaryOff;
        @Nullable
        private CharSequence[] originalEntries;
        private boolean highlightingApplied;

        // Preference types.
        static final int TYPE_REGULAR = 0;
        static final int TYPE_SWITCH = 1;
        static final int TYPE_LIST = 2;
        static final int TYPE_NO_RESULTS = 3;

        SearchResultItem(Preference pref, String navPath) {
            preference = pref;
            navigationPath = navPath;
            originalTitle = pref.getTitle();
            title = originalTitle != null ? originalTitle : "";
            originalSummary = pref.getSummary();
            summary = originalSummary != null ? originalSummary : "";

            // Determine preference type.
            if (pref instanceof SwitchPreference switchPref) {
                preferenceType = TYPE_SWITCH;
                originalSummaryOn = switchPref.getSummaryOn();
                originalSummaryOff = switchPref.getSummaryOff();
            } else if (pref instanceof ListPreference listPref) {
                preferenceType = TYPE_LIST;
                originalEntries = listPref.getEntries();
            } else if (pref.getKey() != null && pref.getKey().equals("no_results_placeholder")) {
                preferenceType = TYPE_NO_RESULTS;
            } else {
                preferenceType = TYPE_REGULAR;
            }

            // Create searchable text combining all relevant fields.
            StringBuilder searchBuilder = new StringBuilder();
            searchBuilder.append(Utils.removePunctuationToLowercase(pref.getKey()));
            searchBuilder.append(" ").append(Utils.removePunctuationToLowercase(title));
            if (!TextUtils.isEmpty(summary)) {
                searchBuilder.append(" ").append(Utils.removePunctuationToLowercase(summary));
            }

            // Add list entries if it's a ListPreference.
            if (pref instanceof ListPreference listPref) {
                CharSequence[] entries = listPref.getEntries();
                if (entries != null) {
                    for (CharSequence entry : entries) {
                        searchBuilder.append(" ").append(Utils.removePunctuationToLowercase(entry));
                    }
                }
            }

            // Add switch summaries if it's a SwitchPreference.
            if (pref instanceof SwitchPreference switchPref) {
                if (switchPref.getSummaryOn() != null) {
                    searchBuilder.append(" ").append(Utils.removePunctuationToLowercase(switchPref.getSummaryOn()));
                }
                if (switchPref.getSummaryOff() != null) {
                    searchBuilder.append(" ").append(Utils.removePunctuationToLowercase(switchPref.getSummaryOff()));
                }
            }

            searchableText = searchBuilder.toString();
        }

        /**
         * Checks if this item matches the search query.
         */
        boolean matchesQuery(String query) {
            return searchableText.contains(Utils.removePunctuationToLowercase(query));
        }

        /**
         * Highlights the search query in the given text by applying a background color span.
         */
        private static CharSequence highlightSearchQuery(CharSequence text, Pattern queryPattern) {
            if (TextUtils.isEmpty(text)) {
                return text;
            }
            final int adjustedColor = Utils.adjustColorBrightness(Utils.getAppBackgroundColor(), 0.95f, 1.20f);
            BackgroundColorSpan highlightSpan = new BackgroundColorSpan(adjustedColor);
            SpannableStringBuilder spannable = new SpannableStringBuilder(text);
            Matcher matcher = queryPattern.matcher(text);
            while (matcher.find()) {
                spannable.setSpan(highlightSpan, matcher.start(), matcher.end(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannable;
        }

        /**
         * Applies highlighting to title, summary, summaryOn, summaryOff, and entries, and updates local fields.
         */
        void applyHighlighting(Pattern queryPattern) {
            CharSequence highlightedTitle = highlightSearchQuery(originalTitle, queryPattern);
            preference.setTitle(highlightedTitle);
            title = highlightedTitle;

            CharSequence highlightedSummary = highlightSearchQuery(originalSummary, queryPattern);
            preference.setSummary(highlightedSummary);
            summary = highlightedSummary;

            if (preference instanceof SwitchPreference switchPref) {
                CharSequence highlightedSummaryOn = highlightSearchQuery(originalSummaryOn, queryPattern);
                switchPref.setSummaryOn(highlightedSummaryOn);
                CharSequence highlightedSummaryOff = highlightSearchQuery(originalSummaryOff, queryPattern);
                switchPref.setSummaryOff(highlightedSummaryOff);
            }
            if (preference instanceof ListPreference listPref && originalEntries != null) {
                CharSequence[] highlightedEntries = new CharSequence[originalEntries.length];
                for (int i = 0; i < originalEntries.length; i++) {
                    highlightedEntries[i] = highlightSearchQuery(originalEntries[i], queryPattern);
                }
                listPref.setEntries(highlightedEntries);
            }
            highlightingApplied = true;
        }

        /**
         * Clears highlighting from title, summary, summaryOn, summaryOff, and entries, and updates local fields.
         */
        void clearHighlighting() {
            if (!highlightingApplied) {
                return;
            }
            preference.setTitle(originalTitle);
            title = originalTitle;

            preference.setSummary(originalSummary);
            summary = originalSummary;

            if (preference instanceof SwitchPreference switchPref) {
                switchPref.setSummaryOn(originalSummaryOn);
                switchPref.setSummaryOff(originalSummaryOff);
            }
            if (preference instanceof ListPreference listPref && originalEntries != null) {
                listPref.setEntries(originalEntries);
            }
            highlightingApplied = false;
        }
    }

    /**
     * Adapter for displaying search results in overlay ListView.
     */
    private class SearchResultsAdapter extends ArrayAdapter<SearchResultItem> {
        private final LayoutInflater inflater;

        SearchResultsAdapter(Context context, List<SearchResultItem> items) {
            super(context, 0, items);
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            SearchResultItem item = getItem(position);
            if (item == null) return new View(getContext());

            // Map preference type to view type string.
            String viewType = switch (item.preferenceType) {
                case SearchResultItem.TYPE_SWITCH -> "switch";
                case SearchResultItem.TYPE_LIST -> "list";
                case SearchResultItem.TYPE_NO_RESULTS -> "no_results";
                default -> "regular";
            };

            return createPreferenceView(item, convertView, viewType);
        }

        /**
         * Creates a view for a preference based on its type.
         */
        @SuppressWarnings("deprecation")
        private View createPreferenceView(SearchResultItem item, View convertView, String viewType) {
            // Map view types to layout resources.
            final Map<String, Integer> layoutMap = new HashMap<>();
            layoutMap.put("regular", getResourceIdentifier("revanced_preference_search_result_preference", "layout"));
            layoutMap.put("switch", getResourceIdentifier("revanced_preference_search_result_switch_preference", "layout"));
            layoutMap.put("list", getResourceIdentifier("revanced_preference_search_result_list_preference", "layout"));
            layoutMap.put("no_results", getResourceIdentifier("revanced_preference_search_no_result_preference", "layout"));

            // Inflate or reuse view.
            View view = convertView;
            Integer layoutResId = layoutMap.get(viewType);
            if (layoutResId == null) {
                Logger.printException(() -> "Invalid viewType: " + viewType + ", cannot inflate view.");
                return new View(getContext()); // Fallback to empty view.
            }
            if (view == null || !viewType.equals(view.getTag())) {
                view = inflater.inflate(layoutResId, null);
                view.setTag(viewType);
            }

            // Initialize common views.
            TextView titleView = view.findViewById(getResourceIdentifier("preference_title", "id"));
            TextView summaryView = view.findViewById(getResourceIdentifier("preference_summary", "id"));
            TextView pathView = view.findViewById(viewType.equals("no_results") ? android.R.id.summary : getResourceIdentifier("preference_path", "id"));

            // Set common view properties.
            titleView.setText(item.title);
            if (!viewType.equals("no_results")) {
                pathView.setText(item.navigationPath);
            }

            // Handle specific view types.
            switch (viewType) {
                case "regular":
                case "list":
                    summaryView.setText(item.summary);
                    summaryView.setVisibility(TextUtils.isEmpty(item.summary) ? View.GONE : View.VISIBLE);
                    setupPreferenceView(view, titleView, summaryView, pathView, item.preference, () -> handlePreferenceClick(item.preference));
                    break;

                case "switch":
                    SwitchPreference switchPref = (SwitchPreference) item.preference;
                    Switch switchWidget = view.findViewById(getResourceIdentifier("preference_switch", "id"));

                    // Remove ripple/highlight.
                    switchWidget.setBackground(null);

                    // Set switch state without animation.
                    boolean currentState = switchPref.isChecked();
                    if (switchWidget.isChecked() != currentState) {
                        switchWidget.setChecked(currentState);
                        switchWidget.jumpDrawablesToCurrentState();
                    }

                    // Update summary based on switch state.
                    CharSequence summaryText = currentState
                            ? (switchPref.getSummaryOn() != null ? switchPref.getSummaryOn() :
                            switchPref.getSummary() != null ? switchPref.getSummary() : "")
                            : (switchPref.getSummaryOff() != null ? switchPref.getSummaryOff() :
                            switchPref.getSummary() != null ? switchPref.getSummary() : "");
                    summaryView.setText(summaryText);
                    summaryView.setVisibility(TextUtils.isEmpty(summaryText) ? View.GONE : View.VISIBLE);

                    // Set up click listeners for switch.
                    final View finalView = view; // Store view in a final variable.
                    setupPreferenceView(view, titleView, summaryView, pathView, switchPref, () -> {
                        boolean newState = !switchPref.isChecked();
                        switchPref.setChecked(newState);
                        switchWidget.setChecked(newState);

                        // Update summary.
                        CharSequence newSummary = newState
                                ? (switchPref.getSummaryOn() != null ? switchPref.getSummaryOn() :
                                switchPref.getSummary() != null ? switchPref.getSummary() : "")
                                : (switchPref.getSummaryOff() != null ? switchPref.getSummaryOff() :
                                switchPref.getSummary() != null ? switchPref.getSummary() : "");
                        summaryView.setText(newSummary);
                        summaryView.setVisibility(TextUtils.isEmpty(newSummary) ? View.GONE : View.VISIBLE);

                        // Notify preference change.
                        if (switchPref.getOnPreferenceChangeListener() != null) {
                            switchPref.getOnPreferenceChangeListener().onPreferenceChange(switchPref, newState);
                        }

                        searchResultsAdapter.notifyDataSetChanged();
                    });
                    switchWidget.setEnabled(switchPref.isEnabled());
                    if (switchPref.isEnabled()) {
                        switchWidget.setOnClickListener(v -> finalView.performClick());
                    } else {
                        switchWidget.setOnClickListener(null);
                    }
                    break;

                case "no_results":
                    summaryView.setText(item.summary);
                    summaryView.setVisibility(TextUtils.isEmpty(item.summary) ? View.GONE : View.VISIBLE);
                    ImageView iconView = view.findViewById(android.R.id.icon);
                    iconView.setImageResource(getResourceIdentifier("revanced_settings_search_icon", "drawable"));
                    break;
            }

            return view;
        }

        /**
         * Sets up common properties for preference views.
         */
        @SuppressWarnings("deprecation")
        private void setupPreferenceView(View view, TextView titleView, TextView summaryView, TextView pathView,
                                         Preference preference, Runnable onClickAction) {
            boolean enabled = preference.isEnabled();
            view.setEnabled(enabled);
            titleView.setEnabled(enabled);
            summaryView.setEnabled(enabled);
            pathView.setEnabled(enabled);
            titleView.setAlpha(enabled ? 1.0f : 0.5f);
            view.setOnClickListener(enabled ? v -> onClickAction.run() : null);
        }
    }

    /**
     * Creates a background drawable for the SearchView with rounded corners.
     */
    private static GradientDrawable createBackgroundDrawable() {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(Utils.dipToPixels(28)); // 28dp corner radius.
        background.setColor(getSearchViewBackground());
        return background;
    }

    /**
     * Creates a background drawable for suggestion items with rounded corners.
     */
    private static GradientDrawable createSuggestionBackgroundDrawable() {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(getSearchViewBackground());
        return background;
    }

    @ColorInt
    public static int getSearchViewBackground() {
        return Utils.isDarkModeEnabled()
                ? Utils.adjustColorBrightness(Utils.getDialogBackgroundColor(), 1.11f)
                : Utils.adjustColorBrightness(Utils.getThemeLightColor(), 0.95f);
    }

    /**
     * Adds search view components to the activity.
     */
    public static SearchViewController addSearchViewComponents(Activity activity, Toolbar toolbar, ReVancedPreferenceFragment fragment) {
        return new SearchViewController(activity, toolbar, fragment);
    }

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
        searchView = activity.findViewById(getResourceIdentifier(
                "revanced_search_view", "id"));
        searchContainer = activity.findViewById(getResourceIdentifier(
                "revanced_search_view_container", "id"));

        // Create overlay container for search results.
        overlayContainer = new FrameLayout(activity);
        overlayContainer.setVisibility(View.GONE);
        overlayContainer.setBackgroundColor(Utils.getAppBackgroundColor());
        overlayContainer.setElevation(8 * activity.getResources().getDisplayMetrics().density); // 8dp elevation.

        // Create search results ListView.
        ListView searchResultsListView = new ListView(activity);
        searchResultsListView.setDivider(null);
        searchResultsListView.setDividerHeight(0);
        searchResultsAdapter = new SearchResultsAdapter(activity, filteredSearchItems);
        searchResultsListView.setAdapter(searchResultsAdapter);

        // Add ListView to overlay container.
        overlayContainer.addView(searchResultsListView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Add overlay to the main content container.
        FrameLayout mainContainer = activity.findViewById(getResourceIdentifier(
                "revanced_settings_fragments", "id"));
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

                    if (TextUtils.isEmpty(newText)) {
                        overlayContainer.setVisibility(View.GONE);
                        filteredSearchItems.clear();
                        searchResultsAdapter.notifyDataSetChanged();

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

        // Set menu.
        final int actionSearchId = getResourceIdentifier("action_search", "id");
        toolbar.inflateMenu(getResourceIdentifier("revanced_search_menu", "menu"));

        // Set menu item click listener.
        toolbar.setOnMenuItemClickListener(item -> {
            try {
                if (item.getItemId() == actionSearchId) {
                    if (!isSearchActive) {
                        openSearch();
                    }
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
     * Initializes search data by collecting all preferences from the fragment.
     */
    @SuppressWarnings("deprecation")
    public void initializeSearchData() {
        allSearchItems.clear();

        // Wait until fragment is properly initialized.
        activity.runOnUiThread(() -> {
            try {
                PreferenceScreen screen = fragment.getPreferenceScreenForSearch();
                if (screen != null) {
                    collectSearchablePreferences(screen, "", 1, 0);
                    Logger.printDebug(() -> "Collected " + allSearchItems.size() + " searchable preferences");
                }
            } catch (Exception ex) {
                Logger.printException(() -> "Failed to initialize search data", ex);
            }
        });
    }

    /**
     * Recursively collect all searchable preferences inside a PreferenceGroup.
     */
    @SuppressWarnings("deprecation")
    private void collectSearchablePreferences(PreferenceGroup group, String parentPath,
                                              int includeDepth, int currentDepth) {
        if (group == null) return;

        for (int i = 0, count = group.getPreferenceCount(); i < count; i++) {
            Preference preference = group.getPreference(i);

            // Add to search results only if it is a "real" preference
            // (not a category or a custom container like SponsorBlockPreferenceGroup).
            if (includeDepth <= currentDepth
                    && !(preference instanceof PreferenceCategory)
                    && !(preference instanceof SponsorBlockPreferenceGroup)) {

                allSearchItems.add(new SearchResultItem(preference, parentPath));
            }

            // If the preference is a group, recurse into it.
            if (preference instanceof PreferenceGroup subGroup) {
                String newPath = parentPath;

                // Append the group title to the path only if it is not a SponsorBlock container.
                if (!(preference instanceof SponsorBlockPreferenceGroup)) {
                    CharSequence title = preference.getTitle();
                    if (!TextUtils.isEmpty(title)) {
                        newPath = TextUtils.isEmpty(parentPath)
                                ? title.toString()
                                : parentPath + " > " + title;
                    }
                }

                // Recurse deeper into subgroup.
                collectSearchablePreferences(subGroup, newPath, includeDepth, currentDepth + 1);
            }
        }
    }

    /**
     * Filters search items based on query and shows results in overlay.
     */
    @SuppressWarnings("deprecation")
    private void filterAndShowResults(String query) {
        filteredSearchItems.clear();

        String queryLower = Utils.removePunctuationToLowercase(query);
        Pattern queryPattern = Pattern.compile(Pattern.quote(queryLower), Pattern.CASE_INSENSITIVE);

        // Clear highlighting for all items to reset previous highlights.
        for (SearchResultItem item : allSearchItems) {
            item.clearHighlighting();
        }

        for (SearchResultItem item : allSearchItems) {
            if (item.matchesQuery(queryLower)) {
                item.applyHighlighting(queryPattern);
                filteredSearchItems.add(item);
            }
        }

        // Show 'No results found' if search results are empty.
        if (filteredSearchItems.isEmpty()) {
            Preference noResultsPreference = new Preference(activity);
            noResultsPreference.setKey("no_results_placeholder");
            noResultsPreference.setTitle(str("revanced_settings_search_no_results_title", query));
            noResultsPreference.setSummary(str("revanced_settings_search_no_results_summary"));
            noResultsPreference.setSelectable(false);
            noResultsPreference.setLayoutResource(getResourceIdentifier("revanced_preference_with_icon_no_search_result", "layout"));
            noResultsPreference.setIcon(getResourceIdentifier("revanced_settings_search_icon", "drawable"));
            filteredSearchItems.add(new SearchResultItem(noResultsPreference, ""));
        }

        searchResultsAdapter.notifyDataSetChanged();

        overlayContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Handles preference click actions.
     */
    @SuppressWarnings("all")
    private void handlePreferenceClick(Preference preference) {
        try {
            Method m = Preference.class.getDeclaredMethod("performClick", PreferenceScreen.class);
            m.setAccessible(true);
            m.invoke(preference, fragment.getPreferenceScreen());
        } catch (Exception e) {
            Logger.printException(() -> "Failed to invoke performClick()", e);
        }
    }

    /**
     * Sets up the search history suggestions for the SearchView with custom adapter.
     */
    private void setupSearchHistory() {
        if (autoCompleteTextView != null) {
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
    }

    /**
     * Saves a search query to the search history.
     * @param query The search query to save.
     */
    private void saveSearchQuery(String query) {
        if (!showSettingsSearchHistory) {
            return;
        }
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
     * @param query The search query to remove.
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

        Settings.SETTINGS_SEARCH_ENTRIES.save(
                String.join("\n", searchHistory)
        );
    }

    /**
     * Updates the search history adapter with the latest history.
     */
    private void updateSearchHistoryAdapter() {
        if (autoCompleteTextView == null) {
            return;
        }

        SearchHistoryAdapter adapter = (SearchHistoryAdapter) autoCompleteTextView.getAdapter();
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(searchHistory);
            adapter.notifyDataSetChanged();
        }
    }

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
     * Opens the search view and shows the keyboard.
     */
    private void openSearch() {
        isSearchActive = true;
        toolbar.getMenu().findItem(getResourceIdentifier(
                "action_search", "id")).setVisible(false);
        toolbar.setTitle("");
        searchContainer.setVisibility(View.VISIBLE);
        searchView.requestFocus();

        // Show keyboard.
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);

        // Show suggestions with a slight delay.
        if (showSettingsSearchHistory && autoCompleteTextView != null && autoCompleteTextView.getText().length() == 0) {
            searchView.postDelayed(() -> {
                if (isSearchActive && autoCompleteTextView.getText().length() == 0) {
                    autoCompleteTextView.showDropDown();
                }
            }, 100); // 100ms delay to ensure focus is stable.
        }
    }

    /**
     * Closes the search view and hides the keyboard.
     */
    public void closeSearch() {
        isSearchActive = false;
        toolbar.getMenu().findItem(getResourceIdentifier(
                "action_search", "id")).setVisible(true);
        toolbar.setTitle(originalTitle);
        searchContainer.setVisibility(View.GONE);
        overlayContainer.setVisibility(View.GONE);
        searchView.setQuery("", false);

        // Hide keyboard.
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

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
                convertView = LinearLayout.inflate(getContext(), getResourceIdentifier(
                        "revanced_search_suggestion_item", "layout"), null);
            }

            // Apply rounded corners programmatically.
            convertView.setBackground(createSuggestionBackgroundDrawable());
            String query = getItem(position);

            // Set query text.
            TextView textView = convertView.findViewById(getResourceIdentifier(
                    "suggestion_text", "id"));
            if (textView != null) {
                textView.setText(query);
            }

            // Set click listener for inserting query into SearchView.
            convertView.setOnClickListener(v -> {
                searchView.setQuery(query, true); // Insert selected query and submit.
            });

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
