package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toolbar;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.StringRef;
import app.revanced.extension.youtube.ThemeHelper;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

public class SearchViewController {
    private final SearchView searchView;
    private final FrameLayout searchContainer;
    private final Toolbar toolbar;
    private final Activity activity;
    private boolean isSearchActive;
    private final CharSequence originalTitle;

    private static GradientDrawable createBackgroundDrawable(Context context) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(28f * context.getResources().getDisplayMetrics().density); // 28dp corner radius
        int baseColor = ThemeHelper.getBackgroundColor();
        int adjustedColor = ThemeHelper.isDarkTheme()
                ? ThemeHelper.adjustColorBrightness(baseColor, 1.11f)  // Lighten for dark theme
                : ThemeHelper.adjustColorBrightness(baseColor, 0.95f); // Darken for light theme
        background.setColor(adjustedColor);
        return background;
    }

    public static void addSearchViewComponents(Activity activity, Toolbar toolbar, ReVancedPreferenceFragment fragment) {
        new SearchViewController(activity, toolbar, fragment);
    }

    private SearchViewController(Activity activity, Toolbar toolbar, ReVancedPreferenceFragment fragment) {
        this.activity = activity;
        this.toolbar = toolbar;
        this.originalTitle = toolbar.getTitle();

        // Retrieve SearchView and container from XML.
        searchView = activity.findViewById(getResourceIdentifier("revanced_search_view", "id"));
        searchContainer = activity.findViewById(getResourceIdentifier("revanced_search_view_container", "id"));

        // Set background.
        searchView.setBackground(createBackgroundDrawable(toolbar.getContext()));

        // Set query hint.
        searchView.setQueryHint(StringRef.str("revanced_search_settings"));

        // Configure RTL support based on app language.
        AppLanguage appLanguage = BaseSettings.REVANCED_LANGUAGE.get(); // Get language from ReVanced settings
        if (Utils.isRightToLeftLocale(appLanguage.getLocale())) {
            searchView.setTextDirection(View.TEXT_DIRECTION_RTL);
            searchView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }

        // Set up query text listener.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    Logger.printDebug(() -> "Search query: " + newText);
                    fragment.filterPreferences(newText);
                } catch (Exception ex) {
                    Logger.printException(() -> "onQueryTextChange failure", ex);
                }
                return true;
            }
        });

        // Set menu and search icon.
        toolbar.inflateMenu(getResourceIdentifier("revanced_search_menu", "menu"));
        MenuItem searchItem = toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id"));
        searchItem.setIcon(getResourceIdentifier(
                        ThemeHelper.isDarkTheme() ? "yt_outline_search_white_24" : "yt_outline_search_black_24",
                        "drawable"))
                .setTooltipText(null);

        // Set menu item click listener.
        toolbar.setOnMenuItemClickListener(item -> {
            try {
                if (item.getItemId() == getResourceIdentifier("action_search", "id")) {
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

    private void openSearch() {
        isSearchActive = true;
        toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id")).setVisible(false);
        toolbar.setTitle("");
        searchContainer.setVisibility(View.VISIBLE);
        searchView.requestFocus();

        // Show keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeSearch() {
        isSearchActive = false;
        toolbar.post(() -> {
            toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id"))
                    .setIcon(getResourceIdentifier(
                            ThemeHelper.isDarkTheme() ? "yt_outline_search_white_24" : "yt_outline_search_black_24",
                            "drawable"))
                    .setVisible(true);
        });
        toolbar.setTitle(originalTitle);
        searchContainer.setVisibility(View.GONE);
        searchView.setQuery("", false);

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }
}
