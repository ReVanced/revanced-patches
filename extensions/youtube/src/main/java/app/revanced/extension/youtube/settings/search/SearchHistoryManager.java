package app.revanced.extension.youtube.settings.search;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.youtube.settings.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import android.util.Pair;
import android.app.Dialog;

/**
 * Manages search history for the ReVanced settings search functionality.
 */
public class SearchHistoryManager {
    private final Deque<String> searchHistory;
    private final Activity activity;
    private final SearchHistoryAdapter searchHistoryAdapter;
    private final boolean showSettingsSearchHistory;

    private static final int MAX_HISTORY_SIZE = 5; // Maximum history items stored.

    // Resource ID constants.
    private static final int ID_SEARCH_HISTORY_LIST = getResourceIdentifier("search_history_list", "id");
    private static final int ID_CLEAR_HISTORY_BUTTON = getResourceIdentifier("clear_history_button", "id");
    private static final int ID_HISTORY_TEXT = getResourceIdentifier("history_text", "id");
    private static final int ID_DELETE_ICON = getResourceIdentifier("delete_icon", "id");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_HISTORY_ITEM =
            getResourceIdentifier("revanced_preference_search_history_item", "layout");

    /**
     * Constructor for SearchHistoryManager.
     *
     * @param activity                  The parent activity.
     * @param searchHistoryContainer    The FrameLayout container for the search history UI.
     * @param onClearHistoryAction      Callback for when history is cleared.
     * @param onSelectHistoryItemAction Callback for when a history item is selected.
     */
    public SearchHistoryManager(Activity activity, FrameLayout searchHistoryContainer,
                                Runnable onClearHistoryAction, OnSelectHistoryItemListener onSelectHistoryItemAction) {
        this.activity = activity;
        this.showSettingsSearchHistory = Settings.SETTINGS_SEARCH_HISTORY.get();
        this.searchHistory = new LinkedList<>();

        // Initialize search history from settings.
        StringSetting searchEntries = Settings.SETTINGS_SEARCH_ENTRIES;
        if (showSettingsSearchHistory) {
            String entries = searchEntries.get();
            if (!entries.isBlank()) {
                searchHistory.addAll(Arrays.asList(entries.split("\n")));
            }
        } else {
            // Clear old saved history if the feature is disabled.
            searchEntries.resetToDefault();
        }

        // Find the LinearLayout for the history list within the container.
        LinearLayout searchHistoryListView = searchHistoryContainer.findViewById(ID_SEARCH_HISTORY_LIST);
        if (searchHistoryListView == null) {
            throw new IllegalStateException("Search history list view not found in container");
        }

        // Set up history adapter.
        this.searchHistoryAdapter = new SearchHistoryAdapter(activity, searchHistoryListView, new ArrayList<>(searchHistory),
                onSelectHistoryItemAction);

        // Set up clear history button.
        TextView clearHistoryButton = searchHistoryContainer.findViewById(ID_CLEAR_HISTORY_BUTTON);
        clearHistoryButton.setOnClickListener(v -> showClearHistoryDialog(onClearHistoryAction));
    }

    /**
     * Interface for handling history item selection.
     */
    public interface OnSelectHistoryItemListener {
        void onSelectHistoryItem(String query);
    }

    /**
     * Saves a search query to the history, maintaining the size limit.
     */
    public void saveSearchQuery(String query) {
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
     * Removes a search query from the history.
     */
    public void removeSearchQuery(String query) {
        searchHistory.remove(query);
        saveSearchHistory();
    }

    /**
     * Saves the search history to shared preferences.
     */
    private void saveSearchHistory() {
        Logger.printDebug(() -> "Saving search history: " + searchHistory);
        Settings.SETTINGS_SEARCH_ENTRIES.save(String.join("\n", searchHistory));
    }

    /**
     * Shows the search history UI and updates the adapter.
     */
    public void showSearchHistory() {
        if (!showSettingsSearchHistory || searchHistory.isEmpty()) {
            return;
        }

        // Update the adapter.
        searchHistoryAdapter.clear();
        searchHistoryAdapter.addAll(searchHistory);
        searchHistoryAdapter.notifyDataSetChanged();
    }

    /**
     * Clears all search history.
     *
     * @param onClearHistoryAction Callback to run after clearing history.
     */
    public void clearAllSearchHistory(Runnable onClearHistoryAction) {
        searchHistory.clear();
        saveSearchHistory();
        searchHistoryAdapter.clear();
        searchHistoryAdapter.notifyDataSetChanged();
        onClearHistoryAction.run();
    }

    /**
     * Shows a confirmation dialog for clearing search history.
     *
     * @param onClearHistoryAction Callback to run if history is cleared.
     */
    private void showClearHistoryDialog(Runnable onClearHistoryAction) {
        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                activity,
                str("revanced_settings_search_clear_history"),
                str("revanced_settings_search_clear_history_message"),
                null,
                null,
                () -> clearAllSearchHistory(onClearHistoryAction),
                () -> {},
                null,
                null,
                false
        );

        Dialog dialog = dialogPair.first;
        dialog.setCancelable(true);
        dialog.show();
    }

    /**
     * Checks if the search history is not empty.
     */
    public boolean isSearchHistoryNotEmpty() {
        return !searchHistory.isEmpty();
    }

    /**
     * Custom adapter for search history items.
     */
    private class SearchHistoryAdapter {
        private final List<String> history;
        private final LayoutInflater inflater;
        private final LinearLayout container;
        private final OnSelectHistoryItemListener onSelectHistoryItemListener;

        public SearchHistoryAdapter(Context context, LinearLayout container, List<String> history,
                                    OnSelectHistoryItemListener listener) {
            this.history = history;
            this.inflater = LayoutInflater.from(context);
            this.container = container;
            this.onSelectHistoryItemListener = listener;
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
                view.setOnClickListener(v -> onSelectHistoryItemListener.onSelectHistoryItem(query));

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
                            },
                            () -> {},
                            null,
                            null,
                            false
                    );

                    Dialog dialog = dialogPair.first;
                    dialog.setCancelable(true);
                    dialog.show();
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
}
