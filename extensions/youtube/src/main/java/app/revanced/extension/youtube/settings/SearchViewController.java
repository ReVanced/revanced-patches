package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.youtube.ThemeHelper;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

/**
 * Controller for managing the search view in ReVanced settings.
 */
@SuppressWarnings({"deprecated", "DiscouragedApi"})
public class SearchViewController {
    private static final String PREFS_NAME = "revanced_search_history";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final int MAX_HISTORY_SIZE = 5;

    private final SearchView searchView;
    private final FrameLayout searchContainer;
    private final Toolbar toolbar;
    private final Activity activity;
    private boolean isSearchActive;
    private final CharSequence originalTitle;
    private final SharedPreferences searchHistoryPrefs;
    private final Set<String> searchHistory;
    private final AutoCompleteTextView autoCompleteTextView;
    private final boolean showSettingsSearchHistory;

    /**
     * Creates a background drawable for the SearchView with rounded corners.
     */
    private static GradientDrawable createBackgroundDrawable(Context context) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(28 * context.getResources().getDisplayMetrics().density); // 28dp corner radius.
        int baseColor = ThemeHelper.getBackgroundColor();
        int adjustedColor = ThemeHelper.isDarkTheme()
                ? ThemeHelper.adjustColorBrightness(baseColor, 1.11f)  // Lighten for dark theme.
                : ThemeHelper.adjustColorBrightness(baseColor, 0.95f); // Darken for light theme.
        background.setColor(adjustedColor);
        return background;
    }

    /**
     * Creates a background drawable for suggestion items with rounded corners.
     */
    private static GradientDrawable createSuggestionBackgroundDrawable(Context context) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(8 * context.getResources().getDisplayMetrics().density); // 8dp corner radius.
        return background;
    }

    /**
     * Adds search view components to the activity.
     */
    public static void addSearchViewComponents(Activity activity, Toolbar toolbar, ReVancedPreferenceFragment fragment) {
        new SearchViewController(activity, toolbar, fragment);
    }

    private SearchViewController(Activity activity, Toolbar toolbar, ReVancedPreferenceFragment fragment) {
        this.activity = activity;
        this.toolbar = toolbar;
        this.originalTitle = toolbar.getTitle();
        this.searchHistoryPrefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.searchHistory = new LinkedHashSet<>(searchHistoryPrefs.getStringSet(
                KEY_SEARCH_HISTORY, Collections.emptySet()));
        this.showSettingsSearchHistory = Settings.SETTINGS_SEARCH_HISTORY.get();

        // Retrieve SearchView and container from XML.
        searchView = activity.findViewById(getResourceIdentifier(
                "revanced_search_view", "id"));
        searchContainer = activity.findViewById(getResourceIdentifier(
                "revanced_search_view_container", "id"));

        // Initialize AutoCompleteTextView.
        autoCompleteTextView = searchView.findViewById(
                searchView.getContext().getResources().getIdentifier(
                        "android:id/search_src_text", null, null));

        // Set background and query hint.
        searchView.setBackground(createBackgroundDrawable(toolbar.getContext()));
        searchView.setQueryHint(str("revanced_search_settings"));

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
                    if (showSettingsSearchHistory && autoCompleteTextView != null) {
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
                    fragment.filterPreferences(newText);
                    // Prevent suggestions from showing during text input.
                    if (showSettingsSearchHistory && autoCompleteTextView != null) {
                        if (!newText.isEmpty()) {
                            autoCompleteTextView.dismissDropDown();
                            autoCompleteTextView.setThreshold(Integer.MAX_VALUE); // Disable autocomplete suggestions.
                        } else {
                            autoCompleteTextView.setThreshold(1); // Re-enable for empty input.
                        }
                    }
                } catch (Exception ex) {
                    Logger.printException(() -> "onQueryTextChange failure", ex);
                }
                return true;
            }
        });

        // Set menu and search icon.
        final int actionSearchId = getResourceIdentifier("action_search", "id");
        toolbar.inflateMenu(getResourceIdentifier("revanced_search_menu", "menu"));
        MenuItem searchItem = toolbar.getMenu().findItem(actionSearchId);
        searchItem.setIcon(getResourceIdentifier(ThemeHelper.isDarkTheme()
                                ? "yt_outline_search_white_24"
                                : "yt_outline_search_black_24",
                        "drawable")).setTooltipText(null);

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
                    activity.onBackPressed();
                }
            } catch (Exception ex) {
                Logger.printException(() -> "navigation click failure", ex);
            }
        });
    }

    /**
     * Sets up the search history suggestions for the SearchView with custom adapter.
     */
    private void setupSearchHistory() {
        if (autoCompleteTextView != null) {
            SearchHistoryAdapter adapter = new SearchHistoryAdapter(activity, searchHistory);
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
        searchHistory.add(query); // Add to the end (most recent).

        // Keep only the last few entries.
        while (searchHistory.size() > MAX_HISTORY_SIZE) {
            Iterator<String> iterator = searchHistory.iterator();
            String next = iterator.next();
            Logger.printDebug(() -> "Removing search history query: " + next);
            iterator.remove();
        }

        saveSearchHistoryToPreferences();

        updateSearchHistoryAdapter();
    }

    /**
     * Removes a search query from the search history.
     * @param query The search query to remove.
     */
    private void removeSearchQuery(String query) {
        if (!!showSettingsSearchHistory) {
            return;
        }
        searchHistory.remove(query);

        saveSearchHistoryToPreferences();

        updateSearchHistoryAdapter();
    }

    /**
     * Save the search history to the shared preferences.
     */
    private void saveSearchHistoryToPreferences() {
        if (!showSettingsSearchHistory) {
            return;
        }
        Logger.printDebug(() -> "Saving search history: " + searchHistory);

        searchHistoryPrefs.edit()
                .putStringSet(KEY_SEARCH_HISTORY, searchHistory)
                .apply();
    }

    /**
     * Updates the search history adapter with the latest history.
     */
    private void updateSearchHistoryAdapter() {
        if (!showSettingsSearchHistory || autoCompleteTextView == null) {
            return;
        }
        SearchHistoryAdapter adapter = (SearchHistoryAdapter) autoCompleteTextView.getAdapter();
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(searchHistory);
            adapter.notifyDataSetChanged();
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
    private void closeSearch() {
        isSearchActive = false;
        toolbar.getMenu().findItem(getResourceIdentifier(
                        "action_search", "id"))
                .setIcon(getResourceIdentifier(ThemeHelper.isDarkTheme()
                                ? "yt_outline_search_white_24"
                                : "yt_outline_search_black_24",
                        "drawable")
                ).setVisible(true);
        toolbar.setTitle(originalTitle);
        searchContainer.setVisibility(View.GONE);
        searchView.setQuery("", false);

        // Hide keyboard.
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    /**
     * Custom ArrayAdapter for search history.
     */
    private class SearchHistoryAdapter extends ArrayAdapter<String> {
        public SearchHistoryAdapter(Context context, Set<String> history) {
            super(context, 0, new ArrayList<>(history));
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = LinearLayout.inflate(getContext(), getResourceIdentifier(
                        "revanced_search_suggestion_item", "layout"), null);
            }

            // Apply rounded corners programmatically.
            convertView.setBackground(createSuggestionBackgroundDrawable(getContext()));
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
                new AlertDialog.Builder(activity)
                        .setTitle(str("revanced_search_settings_user_dialog_title"))
                        .setMessage(str("revanced_search_settings_user_dialog_message"))
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> removeSearchQuery(query))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            });

            return convertView;
        }
    }
}
