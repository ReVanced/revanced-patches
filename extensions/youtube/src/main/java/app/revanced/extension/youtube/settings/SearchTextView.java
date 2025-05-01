package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.StringRef;
import app.revanced.extension.youtube.ThemeHelper;

import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

public class SearchTextView {
    private final EditText searchEditText;
    private final FrameLayout searchContainer;
    private final Toolbar toolbar;
    private final ReVancedPreferenceFragment fragment;
    private final Activity activity;
    private boolean isSearchActive = false;
    private final int[] originalTitleMargin;
    private final CharSequence originalTitle;

    public SearchTextView(@NonNull Activity activity, @NonNull Toolbar toolbar, @NonNull ReVancedPreferenceFragment fragment) {
        this.activity = activity;
        this.toolbar = toolbar;
        this.fragment = fragment;
        this.originalTitle = toolbar.getTitle();
        this.originalTitleMargin = new int[]{toolbar.getTitleMarginStart(), toolbar.getTitleMarginEnd()};

        // Initialize EditText and container
        this.searchEditText = createSearchEditText();
        this.searchContainer = (FrameLayout) searchEditText.getParent();
        setupSearchMenu();
        toolbar.addView(searchContainer);
    }

    private EditText createSearchEditText() {
        Context context = toolbar.getContext();

        // Create container for the search bar
        FrameLayout container = new FrameLayout(context);
        container.setId(getResourceIdentifier("search_view", "id"));
        container.setVisibility(View.GONE);

        // Set container layout params
        ViewGroup.MarginLayoutParams containerParams = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int marginHorizontal = (int) (8 * context.getResources().getDisplayMetrics().density); // 8dp
        int marginVertical = (int) (4 * context.getResources().getDisplayMetrics().density); // 4dp
        containerParams.setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical);
        container.setLayoutParams(containerParams);

        // Create background drawable with rounded corners
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(28f * context.getResources().getDisplayMetrics().density); // 28dp corner radius
        // Adjust background color based on theme
        int baseColor = ThemeHelper.getBackgroundColor();
        int adjustedColor = ThemeHelper.isDarkTheme()
                ? changerColor(baseColor, 1.1111f) // Lighten for dark theme
                : changerColor(baseColor, 0.95f);  // Darken for light theme
        background.setColor(adjustedColor);
        container.setBackground(background);

        // Add elevation for depth (like YouTube's search bar)
        container.setElevation(2f * context.getResources().getDisplayMetrics().density); // 2dp elevation

        // Create EditText
        EditText editText = new EditText(context);
        editText.setHint(StringRef.str("revanced_search_settings"));
        editText.setBackground(null);
        editText.setTextColor(ThemeHelper.getForegroundColor());
        editText.setHintTextColor(ThemeHelper.getForegroundColor() & 0x80FFFFFF); // 50% opacity
        editText.setTextSize(16f);
        editText.setPadding(
                (int) (16 * context.getResources().getDisplayMetrics().density), // 16dp left
                (int) (10 * context.getResources().getDisplayMetrics().density), // 10dp top
                (int) (16 * context.getResources().getDisplayMetrics().density), // 16dp right
                (int) (10 * context.getResources().getDisplayMetrics().density)  // 10dp bottom
        );

        // Set single-line and non-wrapping properties
        editText.setSingleLine(true);
        editText.setHorizontallyScrolling(true);

        // Set EditText layout params with vertical centering
        FrameLayout.LayoutParams editTextParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        editTextParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        editText.setLayoutParams(editTextParams);

        // Add EditText to container
        container.addView(editText);

        // Add TextWatcher
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s != null && s.length() > 0;
                toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id"))
                        .setIcon(getResourceIdentifier(
                                ThemeHelper.isDarkTheme() ? "quantum_ic_close_white_24" : "quantum_ic_close_black_24",
                                "drawable"))
                        .setVisible(hasText);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                Logger.printDebug(() -> "Search query: " + s);
                fragment.filterPreferences(s.toString());
            }
        });

        return editText;
    }

    private void setupSearchMenu() {
        // Inflate menu and set initial icon
        toolbar.inflateMenu(getResourceIdentifier("revanced_search_menu", "menu"));
        toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id"))
                .setIcon(getResourceIdentifier(
                        ThemeHelper.isDarkTheme() ? "yt_outline_search_white_24" : "yt_outline_search_black_24",
                        "drawable"))
                .setTooltipText(null);

        // Set menu item click listener
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == getResourceIdentifier("action_search", "id")) {
                if (!isSearchActive) {
                    openSearch();
                } else {
                    clearSearch();
                }
                return true;
            }
            return false;
        });

        // Set navigation click listener
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
        toolbar.setTitleMarginStart(0);
        toolbar.setTitleMarginEnd(0);
        searchContainer.setVisibility(View.VISIBLE);
        searchEditText.requestFocus();

        // Show keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void clearSearch() {
        searchEditText.setText("");
        toolbar.getMenu().findItem(getResourceIdentifier("action_search", "id")).setVisible(false);
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
        toolbar.setTitleMarginStart(originalTitleMargin[0]);
        toolbar.setTitleMarginEnd(originalTitleMargin[1]);
        searchContainer.setVisibility(View.GONE);
        searchEditText.setText("");

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
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
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        if (factor > 1.0f) {
            // Lighten: Interpolate toward white (255)
            float t = 1.0f - (1.0f / factor); // Interpolation parameter
            red = (int) Math.round(red + (255 - red) * t);
            green = (int) Math.round(green + (255 - green) * t);
            blue = (int) Math.round(blue + (255 - blue) * t);
        } else {
            // Darken or no change: Scale toward black
            red = (int) (red * factor);
            green = (int) (green * factor);
            blue = (int) (blue * factor);
        }

        // Ensure values are within [0, 255]
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        return Color.argb(alpha, red, green, blue);
    }
}