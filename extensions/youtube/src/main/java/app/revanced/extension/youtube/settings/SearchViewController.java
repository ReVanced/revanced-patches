package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

/**
 * Controller for managing the search view in ReVanced settings.
 */
@SuppressWarnings({"deprecated", "DiscouragedApi"})
public class SearchViewController {
    private static final int MAX_HISTORY_SIZE = 5;

    private final SearchView searchView;
    private final FrameLayout searchContainer;
    private final Toolbar toolbar;
    private final Activity activity;
    private boolean isSearchActive;
    private final CharSequence originalTitle;
    private final Deque<String> searchHistory;
    private final AutoCompleteTextView autoCompleteTextView;
    private final boolean showSettingsSearchHistory;
    private int currentOrientation;

    /**
     * Creates a background drawable for the SearchView with rounded corners.
     */
    private static GradientDrawable createBackgroundDrawable(Context context) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(28 * context.getResources().getDisplayMetrics().density); // 28dp corner radius.
        background.setColor(getSearchViewBackground());
        return background;
    }

    /**
     * Creates a background drawable for suggestion items with rounded corners.
     */
    private static GradientDrawable createSuggestionBackgroundDrawable(Context context) {
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
        this.originalTitle = toolbar.getTitle();
        this.showSettingsSearchHistory = Settings.SETTINGS_SEARCH_HISTORY.get();
        this.searchHistory = new LinkedList<>();
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

        // Initialize AutoCompleteTextView.
        autoCompleteTextView = searchView.findViewById(
                searchView.getContext().getResources().getIdentifier(
                        "android:id/search_src_text", null, null));

        // Disable fullscreen keyboard mode.
        autoCompleteTextView.setImeOptions(autoCompleteTextView.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Set background and query hint.
        searchView.setBackground(createBackgroundDrawable(toolbar.getContext()));
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

        monitorOrientationChanges();
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

    private void monitorOrientationChanges() {
        currentOrientation = activity.getResources().getConfiguration().orientation;

        searchView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int newOrientation = activity.getResources().getConfiguration().orientation;
            if (newOrientation != currentOrientation) {
                currentOrientation = newOrientation;
                if (autoCompleteTextView != null) {
                    autoCompleteTextView.dismissDropDown();
                    Logger.printDebug(() -> "Orientation changed, search history dismissed");
                }
            }
        });
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
        searchView.setQuery("", false);

        // Hide keyboard.
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    public static boolean handleBackPress() {
        if (LicenseActivityHook.searchViewController != null
                && LicenseActivityHook.searchViewController.isSearchExpanded()) {
            LicenseActivityHook.searchViewController.closeSearch();
            return true;
        }
        return false;
    }

    public boolean isSearchExpanded() {
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
                Pair<Dialog, LinearLayout> dialogPair = Utils.createCustomDialog(
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
