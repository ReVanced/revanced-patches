package app.revanced.extension.shared.settings.search;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;
import static app.revanced.extension.shared.settings.BaseSettings.SETTINGS_SEARCH_ENTRIES;
import static app.revanced.extension.shared.settings.BaseSettings.SETTINGS_SEARCH_HISTORY;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.preference.BulletPointPreference;
import app.revanced.extension.shared.ui.CustomDialog;

/**
 * Manager for search history functionality.
 */
public class SearchHistoryManager {
    /**
     * Interface for handling history item selection.
     */
    private static final int MAX_HISTORY_SIZE = 5;  // Maximum history items stored.

    private static final int ID_CLEAR_HISTORY_BUTTON = getResourceIdentifierOrThrow(
            "clear_history_button", "id");
    private static final int ID_HISTORY_TEXT = getResourceIdentifierOrThrow(
            "history_text", "id");
    private static final int ID_DELETE_ICON = getResourceIdentifierOrThrow(
            "delete_icon", "id");
    private static final int ID_EMPTY_HISTORY_TITLE = getResourceIdentifierOrThrow(
            "empty_history_title", "id");
    private static final int ID_EMPTY_HISTORY_SUMMARY = getResourceIdentifierOrThrow(
            "empty_history_summary", "id");
    private static final int ID_SEARCH_HISTORY_HEADER = getResourceIdentifierOrThrow(
            "search_history_header", "id");
    private static final int ID_SEARCH_TIPS_SUMMARY = getResourceIdentifierOrThrow(
            "revanced_settings_search_tips_summary", "id");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_HISTORY_SCREEN = getResourceIdentifierOrThrow(
            "revanced_preference_search_history_screen", "layout");
    private static final int LAYOUT_REVANCED_PREFERENCE_SEARCH_HISTORY_ITEM = getResourceIdentifierOrThrow(
            "revanced_preference_search_history_item", "layout");
    private static final int ID_SEARCH_HISTORY_LIST = getResourceIdentifierOrThrow(
            "search_history_list", "id");

    private final Deque<String> searchHistory;
    private final Activity activity;
    private final SearchHistoryAdapter searchHistoryAdapter;
    private final boolean showSettingsSearchHistory;
    private final FrameLayout searchHistoryContainer;

    public interface OnSelectHistoryItemListener {
        void onSelectHistoryItem(String query);
    }

    /**
     * Constructor for SearchHistoryManager.
     *
     * @param activity                  The parent activity.
     * @param overlayContainer          The overlay container to hold the search history container.
     * @param onSelectHistoryItemAction Callback for when a history item is selected.
     */
    SearchHistoryManager(Activity activity, FrameLayout overlayContainer,
                         OnSelectHistoryItemListener onSelectHistoryItemAction) {
        this.activity = activity;
        this.showSettingsSearchHistory = SETTINGS_SEARCH_HISTORY.get();
        this.searchHistory = new LinkedList<>();

        // Initialize search history from settings.
        if (showSettingsSearchHistory) {
            String entries = SETTINGS_SEARCH_ENTRIES.get();
            if (!entries.isBlank()) {
                searchHistory.addAll(Arrays.asList(entries.split("\n")));
            }
        } else {
            // Clear old saved history if the feature is disabled.
            SETTINGS_SEARCH_ENTRIES.resetToDefault();
        }

        // Create search history container.
        this.searchHistoryContainer = new FrameLayout(activity);
        searchHistoryContainer.setVisibility(View.GONE);

        // Inflate search history layout.
        LayoutInflater inflater = LayoutInflater.from(activity);
        View historyView = inflater.inflate(LAYOUT_REVANCED_PREFERENCE_SEARCH_HISTORY_SCREEN, searchHistoryContainer, false);
        searchHistoryContainer.addView(historyView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Add history container to overlay.
        FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        overlayParams.gravity = Gravity.TOP;
        overlayContainer.addView(searchHistoryContainer, overlayParams);

        // Find the LinearLayout for the history list within the container.
        LinearLayout searchHistoryListView = searchHistoryContainer.findViewById(ID_SEARCH_HISTORY_LIST);
        if (searchHistoryListView == null) {
            throw new IllegalStateException("Search history list view not found in container");
        }

        // Set up history adapter. Use a copy of the search history.
        this.searchHistoryAdapter = new SearchHistoryAdapter(activity, searchHistoryListView,
                new ArrayList<>(searchHistory), onSelectHistoryItemAction);

        // Set up clear history button.
        TextView clearHistoryButton = searchHistoryContainer.findViewById(ID_CLEAR_HISTORY_BUTTON);
        clearHistoryButton.setOnClickListener(v -> createAndShowDialog(
                str("revanced_settings_search_clear_history"),
                str("revanced_settings_search_clear_history_message"),
                this::clearAllSearchHistory
        ));

        // Set up search tips summary.
        CharSequence text = BulletPointPreference.formatIntoBulletPoints(
                str("revanced_settings_search_tips_summary"));
        TextView tipsSummary = historyView.findViewById(ID_SEARCH_TIPS_SUMMARY);
        tipsSummary.setText(text);
    }

    /**
     * Shows search history screen - either with history items or empty history message.
     */
    public void showSearchHistory() {
        if (!showSettingsSearchHistory) {
            return;
        }

        // Find all view elements.
        TextView emptyHistoryTitle = searchHistoryContainer.findViewById(ID_EMPTY_HISTORY_TITLE);
        TextView emptyHistorySummary = searchHistoryContainer.findViewById(ID_EMPTY_HISTORY_SUMMARY);
        TextView historyHeader = searchHistoryContainer.findViewById(ID_SEARCH_HISTORY_HEADER);
        LinearLayout historyList = searchHistoryContainer.findViewById(ID_SEARCH_HISTORY_LIST);
        TextView clearHistoryButton = searchHistoryContainer.findViewById(ID_CLEAR_HISTORY_BUTTON);

        if (searchHistory.isEmpty()) {
            // Show empty history state.
            showEmptyHistoryViews(emptyHistoryTitle, emptyHistorySummary);
            hideHistoryViews(historyHeader, historyList, clearHistoryButton);
        } else {
            // Show history list state.
            hideEmptyHistoryViews(emptyHistoryTitle, emptyHistorySummary);
            showHistoryViews(historyHeader, historyList, clearHistoryButton);

            // Update adapter with current history.
            searchHistoryAdapter.clear();
            searchHistoryAdapter.addAll(searchHistory);
            searchHistoryAdapter.notifyDataSetChanged();
        }

        // Show the search history container.
        showSearchHistoryContainer();
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
     * Saves the search history to shared preferences.
     */
    protected void saveSearchHistory() {
        Logger.printDebug(() -> "Saving search history: " + searchHistory);
        SETTINGS_SEARCH_ENTRIES.save(String.join("\n", searchHistory));
    }

    /**
     * Removes a search query from the history.
     */
    public void removeSearchQuery(String query) {
        searchHistory.remove(query);
        saveSearchHistory();
    }

    /**
     * Clears all search history.
     */
    public void clearAllSearchHistory() {
        searchHistory.clear();
        saveSearchHistory();
        searchHistoryAdapter.clear();
        searchHistoryAdapter.notifyDataSetChanged();
        showSearchHistory();
    }

    /**
     * Checks if search history feature is enabled.
     */
    public boolean isSearchHistoryEnabled() {
        return showSettingsSearchHistory;
    }

    /**
     * Shows the search history container and overlay.
     */
    public void showSearchHistoryContainer() {
        searchHistoryContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the search history container.
     */
    public void hideSearchHistoryContainer() {
        searchHistoryContainer.setVisibility(View.GONE);
    }

    /**
     * Helper method to show empty history views.
     */
    protected void showEmptyHistoryViews(TextView emptyTitle, TextView emptySummary) {
        emptyTitle.setVisibility(View.VISIBLE);
        emptyTitle.setText(str("revanced_settings_search_empty_history_title"));
        emptySummary.setVisibility(View.VISIBLE);
        emptySummary.setText(str("revanced_settings_search_empty_history_summary"));
    }

    /**
     * Helper method to hide empty history views.
     */
    protected void hideEmptyHistoryViews(TextView emptyTitle, TextView emptySummary) {
        emptyTitle.setVisibility(View.GONE);
        emptySummary.setVisibility(View.GONE);
    }

    /**
     * Helper method to show history list views.
     */
    protected void showHistoryViews(TextView header, LinearLayout list, TextView clearButton) {
        header.setVisibility(View.VISIBLE);
        list.setVisibility(View.VISIBLE);
        clearButton.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to hide history list views.
     */
    protected void hideHistoryViews(TextView header, LinearLayout list, TextView clearButton) {
        header.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        clearButton.setVisibility(View.GONE);
    }

    /**
     * Creates and shows a dialog with the specified title, message, and confirmation action.
     *
     * @param title            The title of the dialog.
     * @param message          The message to display in the dialog.
     * @param confirmAction    The action to perform when the dialog is confirmed.
     */
    protected void createAndShowDialog(String title, String message, Runnable confirmAction) {
        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                activity,
                title,
                message,
                null,
                null,
                confirmAction,
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
     * Custom adapter for search history items.
     */
    protected class SearchHistoryAdapter {
        protected final Collection<String> history;
        protected final LayoutInflater inflater;
        protected final LinearLayout container;
        protected final OnSelectHistoryItemListener onSelectHistoryItemListener;

        public SearchHistoryAdapter(Context context, LinearLayout container, Collection<String> history,
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
                deleteIcon.setOnClickListener(v -> createAndShowDialog(
                        query,
                        str("revanced_settings_search_remove_message"),
                        () -> {
                            removeSearchQuery(query);
                            remove(query);
                            notifyDataSetChanged();
                        }
                ));

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
            if (history.isEmpty()) {
                // If history is now empty, show the empty history state.
                showSearchHistory();
            } else {
                notifyDataSetChanged();
            }
        }
    }
}
