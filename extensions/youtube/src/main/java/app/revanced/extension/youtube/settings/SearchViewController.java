package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
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
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory;
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;
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
    private final InputMethodManager inputMethodManager;

    private boolean isSearchActive;
    private int currentOrientation;

    private static final int MAX_HISTORY_SIZE = 5;
    private static final int SEARCH_DROPDOWN_DELAY_MS = 100;
    private static final float DISABLED_ALPHA = 0.5f;

    // Resource ID constants.
    private static final int ID_REVANCED_SEARCH_VIEW = getResourceIdentifier("revanced_search_view", "id");
    private static final int ID_REVANCED_SEARCH_VIEW_CONTAINER = getResourceIdentifier("revanced_search_view_container", "id");
    private static final int ID_REVANCED_SETTINGS_FRAGMENTS = getResourceIdentifier("revanced_settings_fragments", "id");
    private static final int ID_ACTION_SEARCH = getResourceIdentifier("action_search", "id");
    private static final int ID_PREFERENCE_TITLE = getResourceIdentifier("preference_title", "id");
    private static final int ID_PREFERENCE_SUMMARY = getResourceIdentifier("preference_summary", "id");
    private static final int ID_PREFERENCE_PATH = getResourceIdentifier("preference_path", "id");
    private static final int ID_PREFERENCE_SWITCH = getResourceIdentifier("preference_switch", "id");
    private static final int ID_SUGGESTION_TEXT = getResourceIdentifier("suggestion_text", "id");

    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_PREFERENCE = getResourceIdentifier("revanced_preference_search_result_preference", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_SWITCH_PREFERENCE = getResourceIdentifier("revanced_preference_search_result_switch_preference", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_LIST_PREFERENCE = getResourceIdentifier("revanced_preference_search_result_list_preference", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_NO_RESULT_PREFERENCE = getResourceIdentifier("revanced_preference_search_no_result_preference", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_WITH_ICON_NO_SEARCH_RESULT = getResourceIdentifier("revanced_preference_with_icon_no_search_result", "layout");
    private static final int LAYOUT_REVANCED_SEARCH_SUGGESTION_ITEM = getResourceIdentifier("revanced_search_suggestion_item", "layout");

    private static final int DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON = getResourceIdentifier("revanced_settings_search_icon", "drawable");
    private static final int MENU_REVANCED_SEARCH_MENU = getResourceIdentifier("revanced_search_menu", "menu");

    // Layout resource mapping.
    private static final Map<String, String> LAYOUT_RESOURCE_MAP = createLayoutResourceMap();

    private static Map<String, String> createLayoutResourceMap() {
        return Map.of(
                "regular", "revanced_preference_search_result_preference",
                "switch", "revanced_preference_search_result_switch_preference",
                "list", "revanced_preference_search_result_list_preference",
                "no_results", "revanced_preference_search_no_result_preference");
    }

    /**
     * Data class for search result items.
     */
    @SuppressWarnings("deprecation")
    private static class SearchResultItem {
        static final int TYPE_REGULAR = 0;
        static final int TYPE_SWITCH = 1;
        static final int TYPE_LIST = 2;
        static final int TYPE_NO_RESULTS = 3;

        final Preference preference;
        final String navigationPath;
        final String searchableText;
        final int preferenceType;

        @Nullable private final CharSequence originalTitle;
        @Nullable private final CharSequence originalSummary;
        @Nullable private final CharSequence originalSummaryOn;
        @Nullable private final CharSequence originalSummaryOff;
        @Nullable private final CharSequence[] originalEntries;

        CharSequence title;
        CharSequence summary;
        private boolean highlightingApplied;

        SearchResultItem(Preference pref, String navPath) {
            this.preference = pref;
            this.navigationPath = navPath;
            this.originalTitle = pref.getTitle();
            this.title = originalTitle != null ? originalTitle : "";
            this.originalSummary = pref.getSummary();
            this.summary = originalSummary != null ? originalSummary : "";

            // Determine preference type and cache original values.
            if (pref instanceof SwitchPreference switchPref) {
                this.preferenceType = TYPE_SWITCH;
                this.originalSummaryOn = switchPref.getSummaryOn();
                this.originalSummaryOff = switchPref.getSummaryOff();
                this.originalEntries = null;
            } else if (pref instanceof ListPreference listPref) {
                this.preferenceType = TYPE_LIST;
                this.originalSummaryOn = null;
                this.originalSummaryOff = null;
                this.originalEntries = listPref.getEntries();
            } else if ("no_results_placeholder".equals(pref.getKey())) {
                this.preferenceType = TYPE_NO_RESULTS;
                this.originalSummaryOn = null;
                this.originalSummaryOff = null;
                this.originalEntries = null;
            } else {
                this.preferenceType = TYPE_REGULAR;
                this.originalSummaryOn = null;
                this.originalSummaryOff = null;
                this.originalEntries = null;
            }

            // Create searchable text combining all relevant fields.
            StringBuilder searchBuilder = new StringBuilder();
            appendText(searchBuilder, pref.getKey());
            appendText(searchBuilder, title);
            appendText(searchBuilder, summary);

            // Add type-specific searchable content.
            if (pref instanceof ListPreference listPref) {
                CharSequence[] entries = listPref.getEntries();
                if (entries != null) {
                    for (CharSequence entry : entries) {
                        appendText(searchBuilder, entry);
                    }
                }
            } else if (pref instanceof SwitchPreference switchPref) {
                appendText(searchBuilder, switchPref.getSummaryOn());
                appendText(searchBuilder, switchPref.getSummaryOff());
            }

            this.searchableText = searchBuilder.toString();
        }

        private void appendText(StringBuilder builder, CharSequence text) {
            if (!TextUtils.isEmpty(text)) {
                if (builder.length() > 0) builder.append(" ");
                builder.append(Utils.removePunctuationToLowercase(text));
            }
        }

        /**
         * Checks if this search result item matches the provided query.
         * Uses case-insensitive matching against the searchable text.
         */
        boolean matchesQuery(String query) {
            return searchableText.contains(Utils.removePunctuationToLowercase(query));
        }

        /**
         * Highlights the search query in the given text by applying a background color span.
         */
        private static CharSequence highlightSearchQuery(CharSequence text, Pattern queryPattern) {
            if (TextUtils.isEmpty(text)) return text;

            final int adjustedColor = Utils.adjustColorBrightness(
                    Utils.getAppBackgroundColor(), 0.95f, 1.20f);
            BackgroundColorSpan highlightSpan = new BackgroundColorSpan(adjustedColor);
            SpannableStringBuilder spannable = new SpannableStringBuilder(text);

            Matcher matcher = queryPattern.matcher(text);
            while (matcher.find()) {
                spannable.setSpan(highlightSpan, matcher.start(), matcher.end(),
                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return spannable;
        }

        /**
         * Applies search query highlighting to the preference's title, summary, and type-specific content.
         * Only applies highlighting if not already applied to avoid redundant operations.
         */
        void applyHighlighting(Pattern queryPattern) {
            if (highlightingApplied) return;

            CharSequence highlightedTitle = highlightSearchQuery(originalTitle, queryPattern);
            preference.setTitle(highlightedTitle);
            title = highlightedTitle;

            CharSequence highlightedSummary = highlightSearchQuery(originalSummary, queryPattern);
            preference.setSummary(highlightedSummary);
            summary = highlightedSummary;

            // Apply type-specific highlighting.
            if (preference instanceof SwitchPreference switchPref) {
                switchPref.setSummaryOn(highlightSearchQuery(originalSummaryOn, queryPattern));
                switchPref.setSummaryOff(highlightSearchQuery(originalSummaryOff, queryPattern));
            } else if (preference instanceof ListPreference listPref && originalEntries != null) {
                CharSequence[] highlightedEntries = new CharSequence[originalEntries.length];
                for (int i = 0; i < originalEntries.length; i++) {
                    highlightedEntries[i] = highlightSearchQuery(originalEntries[i], queryPattern);
                }
                listPref.setEntries(highlightedEntries);
            }

            highlightingApplied = true;
        }

        /**
         * Clears all search query highlighting from the preference's content.
         * Restores original text for title, summary, and type-specific content.
         */
        void clearHighlighting() {
            if (!highlightingApplied) return;

            preference.setTitle(originalTitle);
            title = originalTitle;
            preference.setSummary(originalSummary);
            summary = originalSummary;

            // Clear type-specific highlighting.
            if (preference instanceof SwitchPreference switchPref) {
                switchPref.setSummaryOn(originalSummaryOn);
                switchPref.setSummaryOff(originalSummaryOff);
            } else if (preference instanceof ListPreference listPref && originalEntries != null) {
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
            this.inflater = LayoutInflater.from(context);
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

            // Create or reuse preference view based on type.
            View view = createPreferenceView(item, convertView, viewType);

            if (!"no_results".equals(viewType)) {
                TextView pathView = view.findViewById(ID_PREFERENCE_PATH);
                boolean showPath = true;

                // Only show the path if it's the first occurrence in the group.
                if (position > 0) {
                    SearchResultItem previousItem = getItem(position - 1);
                    if (previousItem != null && item.navigationPath.equals(previousItem.navigationPath)) {
                        showPath = false; // Hide path for subsequent elements.
                    }
                }

                pathView.setVisibility(showPath ? View.VISIBLE : View.GONE);
            }

            return view;
        }

        /**
         * Creates a view for a preference based on its type.
         */
        @SuppressWarnings("deprecation")
        private View createPreferenceView(SearchResultItem item, View convertView, String viewType) {
            // Inflate or reuse view.
            View view = convertView;
            String layoutResource = LAYOUT_RESOURCE_MAP.get(viewType);
            if (layoutResource == null) {
                Logger.printException(() -> "Invalid viewType: " + viewType + ", cannot inflate view.");
                return new View(getContext()); // Fallback to empty view.
            }
            int layoutId = switch (layoutResource) {
                case "revanced_preference_search_result_preference" -> LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_PREFERENCE;
                case "revanced_preference_search_result_switch_preference" -> LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_SWITCH_PREFERENCE;
                case "revanced_preference_search_result_list_preference" -> LAYOUT_REVANCED_PREFERENCE_SEARCH_RESULT_LIST_PREFERENCE;
                case "revanced_preference_search_no_result_preference" -> LAYOUT_REVANCED_PREFERENCE_SEARCH_NO_RESULT_PREFERENCE;
                default -> throw new IllegalStateException("Unknown layout resource: " + layoutResource);
            };
            if (view == null || !viewType.equals(view.getTag())) {
                view = inflater.inflate(layoutId, null);
                view.setTag(viewType);
            }

            // Initialize common views.
            TextView titleView = view.findViewById(ID_PREFERENCE_TITLE);
            TextView summaryView = view.findViewById(ID_PREFERENCE_SUMMARY);
            TextView pathView = view.findViewById("no_results".equals(viewType) ?
                    android.R.id.summary : ID_PREFERENCE_PATH);

            // Set common view properties.
            titleView.setText(item.title);
            if (!"no_results".equals(viewType)) {
                pathView.setText(item.navigationPath);
            }

            // Handle specific view types.
            switch (viewType) {
                case "regular", "list" -> {
                    summaryView.setText(item.summary);
                    summaryView.setVisibility(TextUtils.isEmpty(item.summary) ? View.GONE : View.VISIBLE);
                    setupPreferenceView(view, titleView, summaryView, pathView, item.preference,
                            () -> handlePreferenceClick(item.preference));
                }

                case "switch" -> {
                    SwitchPreference switchPref = (SwitchPreference) item.preference;
                    Switch switchWidget = view.findViewById(ID_PREFERENCE_SWITCH);

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
                }

                case "no_results" -> {
                    summaryView.setText(item.summary);
                    summaryView.setVisibility(TextUtils.isEmpty(item.summary) ? View.GONE : View.VISIBLE);
                    ImageView iconView = view.findViewById(android.R.id.icon);
                    iconView.setImageResource(DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON);
                }
            }

            return view;
        }

        /**
         * Sets up common properties for preference views.
         */
        @SuppressWarnings("deprecation")
        private void setupPreferenceView(View view, TextView titleView, TextView summaryView,
                                         TextView pathView, Preference preference, Runnable onClickAction) {
            boolean enabled = preference.isEnabled();

            view.setEnabled(enabled);
            titleView.setEnabled(enabled);
            if (summaryView != null) summaryView.setEnabled(enabled);
            if (pathView != null) pathView.setEnabled(enabled);

            titleView.setAlpha(enabled ? 1.0f : DISABLED_ALPHA);
            view.setOnClickListener(enabled ? v -> onClickAction.run() : null);
        }
    }

    /**
     * Gets the background color for search view components based on current theme.
     */
    @ColorInt
    public static int getSearchViewBackground() {
        return Utils.isDarkModeEnabled()
                ? Utils.adjustColorBrightness(Utils.getDialogBackgroundColor(), 1.11f)
                : Utils.adjustColorBrightness(Utils.getThemeLightColor(), 0.95f);
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
     * @param toolbar The toolbar where search functionality will be integrated.
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
        searchResultsAdapter = new SearchResultsAdapter(activity, filteredSearchItems);
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
     * Recursively collects all searchable preferences from a preference group.
     * Builds navigation paths for each preference and filters out non-searchable items.
     *
     * @param group The preference group to search within.
     * @param parentPath The navigation path to this group.
     * @param includeDepth The minimum depth at which to include preferences.
     * @param currentDepth The current recursion depth.
     */
    @SuppressWarnings("deprecation")
    private void collectSearchablePreferences(PreferenceGroup group, String parentPath,
                                              int includeDepth, int currentDepth) {
        if (group == null) return;

        for (int i = 0, count = group.getPreferenceCount(); i < count; i++) {
            Preference preference = group.getPreference(i);

            // Add to search results only if it is a "real" preference and not a category or SponsorBlockPreferenceGroup.
            if (includeDepth <= currentDepth
                    && !(preference instanceof PreferenceCategory)
                    && !(preference instanceof SponsorBlockPreferenceGroup)) {

                allSearchItems.add(new SearchResultItem(preference, parentPath));
            }

            // If the preference is a group, recurse into it.
            if (preference instanceof PreferenceGroup subGroup) {
                String newPath = parentPath;

                // Append the group title to the path only if it is not a SponsorBlockPreferenceGroup or NoTitlePreferenceCategory.
                if (!(preference instanceof SponsorBlockPreferenceGroup)
                        && !(preference instanceof NoTitlePreferenceCategory)) {
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
     * Filters all search items based on the provided query and displays results in the overlay.
     * Applies highlighting to matching text and shows a "no results" message if nothing matches.
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
            Preference noResultsPreference = createNoResultsPreference(query);
            filteredSearchItems.add(new SearchResultItem(noResultsPreference, ""));
        }

        searchResultsAdapter.notifyDataSetChanged();

        overlayContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Creates a Preference object for the "No results found" message.
     */
    @SuppressWarnings("deprecation")
    private Preference createNoResultsPreference(String query) {
        Preference noResultsPreference = new Preference(activity);
        noResultsPreference.setKey("no_results_placeholder");
        noResultsPreference.setTitle(str("revanced_settings_search_no_results_title", query));
        noResultsPreference.setSummary(str("revanced_settings_search_no_results_summary"));
        noResultsPreference.setSelectable(false);
        noResultsPreference.setLayoutResource(LAYOUT_REVANCED_PREFERENCE_WITH_ICON_NO_SEARCH_RESULT);
        noResultsPreference.setIcon(DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON);
        return noResultsPreference;
    }

    /**
     * Handles preference click actions by invoking the preference's performClick method via reflection.
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
                convertView = LinearLayout.inflate(getContext(), LAYOUT_REVANCED_SEARCH_SUGGESTION_ITEM, null);
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
