package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.Utils.clamp;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.youtube.ThemeHelper;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

public class SearchViewManager {
    private final SearchView searchView;
    private final FrameLayout searchContainer;
    private final Toolbar toolbar;
    private final ReVancedPreferenceFragment fragment;
    private final Activity activity;
    private boolean isSearchActive = false;
    private final CharSequence originalTitle;

    public SearchViewManager(@NonNull Activity activity, @NonNull Toolbar toolbar, @NonNull ReVancedPreferenceFragment fragment) {
        this.activity = activity;
        this.toolbar = toolbar;
        this.fragment = fragment;
        this.originalTitle = toolbar.getTitle();

        // Retrieve SearchView and container from XML.
        this.searchView = activity.findViewById(getResourceIdentifier("revanced_search_view", "id"));
        this.searchContainer = activity.findViewById(getResourceIdentifier("revanced_search_view_container", "id"));
        setupSearchView();
        setupSearchMenu();
    }

    private void setupSearchView() {
        Context context = toolbar.getContext();

        // Set background
        searchView.setBackground(createBackgroundDrawable(context));

        // Configure RTL support based on app language
        AppLanguage appLanguage = BaseSettings.REVANCED_LANGUAGE.get(); // Get language from ReVanced settings
        if (Utils.isRightToLeftLocale(appLanguage.getLocale())) {
            searchView.setTextDirection(View.TEXT_DIRECTION_RTL);
            searchView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }

        // Set up query text listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Logger.printDebug(() -> "Search query: " + newText);
                fragment.filterPreferences(newText);
                return true;
            }
        });
    }

    private GradientDrawable createBackgroundDrawable(Context context) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(28f * context.getResources().getDisplayMetrics().density); // 28dp corner radius
        int baseColor = ThemeHelper.getBackgroundColor();
        int adjustedColor = ThemeHelper.isDarkTheme()
                ? changerColor(baseColor, 1.11f)  // Lighten for dark theme
                : changerColor(baseColor, 0.95f); // Darken for light theme
        background.setColor(adjustedColor);
        return background;
    }

    private void setupSearchMenu() {
        // Set menu and search icon.
        toolbar.inflateMenu(getResourceIdentifier("revanced_search_menu", "menu"));
        MenuItem searchItem = toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id"));
        searchItem.setIcon(getResourceIdentifier(
                        ThemeHelper.isDarkTheme() ? "yt_outline_search_white_24" : "yt_outline_search_black_24",
                        "drawable"))
                .setTooltipText(null);

        // Set menu item click listener.
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == getResourceIdentifier("action_search", "id")) {
                if (!isSearchActive) {
                    openSearch();
                }
                return true;
            }
            return false;
        });

        // Set navigation click listener.
        toolbar.setNavigationOnClickListener(view -> {
            if (isSearchActive) {
                closeSearch();
            } else {
                activity.onBackPressed();
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

    /**
     * Adjusts the brightness of a color by lightening or darkening it based on the given factor.
     * <p>
     * If the factor is greater than 1, the color is lightened by interpolating toward white (#FFFFFF).
     * If the factor is less than or equal to 1, the color is darkened by scaling its RGB components toward black (#000000).
     * The alpha channel remains unchanged.
     *
     * @param color  The input color to adjust, in ARGB format.
     * @param factor The adjustment factor. Use values > 1.0f to lighten (e.g., 1.1111f for slight lightening)
     *               or values <= 1.0f to darken (e.g., 0.95f for slight darkening).
     * @return The adjusted color in ARGB format.
     */
    public static int changerColor(int color, float factor) {
        final int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        if (factor > 1.0f) {
            // Lighten: Interpolate toward white (255)
            final float t = 1.0f - (1.0f / factor); // Interpolation parameter
            red = Math.round(red + (255 - red) * t);
            green = Math.round(green + (255 - green) * t);
            blue = Math.round(blue + (255 - blue) * t);
        } else {
            // Darken or no change: Scale toward black
            red = (int) (red * factor);
            green = (int) (green * factor);
            blue = (int) (blue * factor);
        }

        // Ensure values are within [0, 255]
        red = clamp(red, 0, 255);
        green = clamp(green, 0, 255);
        blue = clamp(blue, 0, 255);

        return Color.argb(alpha, red, green, blue);
    }
}
