package app.revanced.extension.youtube.settings;

import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.StringRef;
import app.revanced.extension.youtube.ThemeHelper;

import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

public class SearchTextView {
    private final EditText searchEditText;
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

        // Initialize EditText
        searchEditText = createSearchEditText();
        setupSearchMenu();
        toolbar.addView(searchEditText);
    }

    private EditText createSearchEditText() {
        EditText editText = new EditText(toolbar.getContext());
        editText.setId(getResourceIdentifier("search_view", "id"));
        editText.setHint(StringRef.str("revanced_search_settings"));
        editText.setBackground(null);
        editText.setTextColor(ThemeHelper.getForegroundColor());
        editText.setHintTextColor(ThemeHelper.getForegroundColor() & 0x80FFFFFF); // 50% opacity
        editText.setVisibility(View.GONE);

        // Set layout params
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        editText.setLayoutParams(params);

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
        searchEditText.setVisibility(View.VISIBLE);
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
        searchEditText.setVisibility(View.GONE);
        searchEditText.setText("");

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }
}