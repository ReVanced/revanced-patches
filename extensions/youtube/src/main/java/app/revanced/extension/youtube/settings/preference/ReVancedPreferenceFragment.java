package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Insets;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory;
import app.revanced.extension.youtube.settings.LicenseActivityHook;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup;

/**
 * Preference fragment for ReVanced settings.
 */
@SuppressWarnings("deprecation")
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {

    /**
     * The main PreferenceScreen used to display the current set of preferences.
     * This screen is manipulated during initialization and filtering to show or hide preferences.
     */
    private PreferenceScreen preferenceScreen;

    /**
     * A copy of the original PreferenceScreen created during initialization.
     * Used to restore the preference structure to its initial state after filtering or other modifications.
     */
    private PreferenceScreen originalPreferenceScreen;

    /**
     * Used for searching preferences. A Collection of all preferences including nested preferences.
     * Root preferences are excluded (no need to search what's on the root screen),
     * but their sub preferences are included.
     */
    private final List<AbstractPreferenceSearchData<?>> allPreferences = new ArrayList<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getBackButtonDrawable() {
        final int backButtonResource = getResourceIdentifier("revanced_settings_toolbar_arrow_left", "drawable");
        Drawable drawable = Utils.getContext().getResources().getDrawable(backButtonResource);
        drawable.setTint(Utils.getAppForegroundColor());
        return drawable;
    }

    /**
     * Sets the system navigation bar color for the activity.
     * Applies the background color obtained from {@link Utils#getAppBackgroundColor()} to the navigation bar.
     * For Android 10 (API 29) and above, enforces navigation bar contrast to ensure visibility.
     */
    public static void setNavigationBarColor(@Nullable Window window) {
        if (window == null) {
            Logger.printDebug(() -> "Cannot set navigation bar color, window is null");
            return;
        }

        window.setNavigationBarColor(Utils.getAppBackgroundColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(true);
        }
    }

    /**
     * Initializes the preference fragment, copying the original screen to allow full restoration.
     */
    @Override
    protected void initialize() {
        super.initialize();

        try {
            preferenceScreen = getPreferenceScreen();
            Utils.sortPreferenceGroups(preferenceScreen);

            // Store the original structure for restoration after filtering.
            originalPreferenceScreen = getPreferenceManager().createPreferenceScreen(getContext());
            for (int i = 0, count = preferenceScreen.getPreferenceCount(); i < count; i++) {
                originalPreferenceScreen.addPreference(preferenceScreen.getPreference(i));
            }

            setPreferenceScreenToolbar(preferenceScreen);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Called when the fragment starts, ensuring all preferences are collected after initialization.
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            if (allPreferences.isEmpty()) {
                // Must collect preferences on start and not in initialize since
                // legacy SB settings are not loaded yet.
                Logger.printDebug(() -> "Collecting preferences to search");

                // Do not show root menu preferences in search results.
                // Instead search for everything that's not shown when search is not active.
                collectPreferences(preferenceScreen, 1, 0);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onStart failure", ex);
        }
    }

    /**
     * Recursively collects all preferences from the screen or group.
     * @param includeDepth Menu depth to start including preferences.
     *                     A value of 0 adds all preferences.
     */
    private void collectPreferences(PreferenceGroup group, int includeDepth, int currentDepth) {
        for (int i = 0, count = group.getPreferenceCount(); i < count; i++) {
            Preference preference = group.getPreference(i);
            if (includeDepth <= currentDepth && !(preference instanceof PreferenceCategory)
                    && !(preference instanceof SponsorBlockPreferenceGroup)) {

                AbstractPreferenceSearchData<?> data;
                if (preference instanceof SwitchPreference switchPref) {
                    data = new SwitchPreferenceSearchData(switchPref);
                } else if (preference instanceof ListPreference listPref) {
                    data = new ListPreferenceSearchData(listPref);
                } else {
                    data = new PreferenceSearchData(preference);
                }

                allPreferences.add(data);
            }

            if (preference instanceof PreferenceGroup subGroup) {
                collectPreferences(subGroup, includeDepth, currentDepth + 1);
            }
        }
    }

    /**
     * Filters the preferences using the given query string and applies highlighting.
     */
    public void filterPreferences(String query) {
        preferenceScreen.removeAll();

        if (TextUtils.isEmpty(query)) {
            // Restore original preferences and their titles/summaries/entries.
            for (int i = 0, count = originalPreferenceScreen.getPreferenceCount(); i < count; i++) {
                preferenceScreen.addPreference(originalPreferenceScreen.getPreference(i));
            }

            for (AbstractPreferenceSearchData<?> data : allPreferences) {
                data.clearHighlighting();
            }

            return;
        }

        // Navigation path -> Category
        Map<String, PreferenceCategory> categoryMap = new HashMap<>();
        String queryLower = Utils.removePunctuationToLowercase(query);

        Pattern queryPattern = Pattern.compile(Pattern.quote(Utils.removePunctuationToLowercase(query)),
                Pattern.CASE_INSENSITIVE);

        for (AbstractPreferenceSearchData<?> data : allPreferences) {
            if (data.matchesSearchQuery(queryLower)) {
                data.applyHighlighting(queryLower, queryPattern);

                String navigationPath = data.navigationPath;
                PreferenceCategory group = categoryMap.computeIfAbsent(navigationPath, key -> {
                    PreferenceCategory newGroup = new PreferenceCategory(preferenceScreen.getContext());
                    newGroup.setTitle(navigationPath);
                    preferenceScreen.addPreference(newGroup);
                    return newGroup;
                });
                group.addPreference(data.preference);
            }
        }

        // Show 'No results found' if search results are empty.
        if (categoryMap.isEmpty()) {
            Preference noResultsPreference = new Preference(preferenceScreen.getContext());
            noResultsPreference.setTitle(str("revanced_settings_search_no_results_title", query));
            noResultsPreference.setSummary(str("revanced_settings_search_no_results_summary"));
            noResultsPreference.setSelectable(false);
            // Set icon for the placeholder preference.
            noResultsPreference.setLayoutResource(getResourceIdentifier(
                    "revanced_preference_with_icon_no_search_result", "layout"));
            noResultsPreference.setIcon(getResourceIdentifier("revanced_settings_search_icon", "drawable"));
            preferenceScreen.addPreference(noResultsPreference);
        }
    }

    /**
     * Sets toolbar for all nested preference screens.
     */
    private void setPreferenceScreenToolbar(PreferenceScreen parentScreen) {
        for (int i = 0, count = parentScreen.getPreferenceCount(); i < count; i++) {
            Preference childPreference = parentScreen.getPreference(i);
            if (childPreference instanceof PreferenceScreen) {
                // Recursively set sub preferences.
                setPreferenceScreenToolbar((PreferenceScreen) childPreference);

                childPreference.setOnPreferenceClickListener(
                        childScreen -> {
                            Dialog preferenceScreenDialog = ((PreferenceScreen) childScreen).getDialog();
                            ViewGroup rootView = (ViewGroup) preferenceScreenDialog
                                    .findViewById(android.R.id.content)
                                    .getParent();

                            // Fix the system navigation bar color for submenus.
                            setNavigationBarColor(preferenceScreenDialog.getWindow());

                            // Fix edge-to-edge screen with Android 15 and YT 19.45+
                            // https://developer.android.com/develop/ui/views/layout/edge-to-edge#system-bars-insets
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                                    Insets statusInsets = insets.getInsets(WindowInsets.Type.statusBars());
                                    Insets navInsets = insets.getInsets(WindowInsets.Type.navigationBars());
                                    Insets cutoutInsets = insets.getInsets(WindowInsets.Type.displayCutout());

                                    // Apply padding for display cutout in landscape.
                                    int leftPadding = cutoutInsets.left;
                                    int rightPadding = cutoutInsets.right;
                                    int topPadding = statusInsets.top;
                                    int bottomPadding = navInsets.bottom;

                                    v.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
                                    return insets;
                                });
                            }

                            Toolbar toolbar = new Toolbar(childScreen.getContext());
                            toolbar.setTitle(childScreen.getTitle());
                            toolbar.setNavigationIcon(getBackButtonDrawable());
                            toolbar.setNavigationOnClickListener(view -> preferenceScreenDialog.dismiss());

                            final int margin = Utils.dipToPixels(16);
                            toolbar.setTitleMargin(margin, 0, margin, 0);

                            TextView toolbarTextView = Utils.getChildView(toolbar,
                                    true, TextView.class::isInstance);
                            if (toolbarTextView != null) {
                                toolbarTextView.setTextColor(Utils.getAppForegroundColor());
                                toolbarTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            }

                            LicenseActivityHook.setToolbarLayoutParams(toolbar);

                            if (LicenseActivityHook.searchViewController != null
                                    && LicenseActivityHook.searchViewController.isSearchActive()) {
                                toolbar.post(() -> LicenseActivityHook.searchViewController.closeSearch());
                            }

                            rootView.addView(toolbar, 0);
                            return false;
                        }
                );
            }
        }
    }
}

@SuppressWarnings("deprecation")
class AbstractPreferenceSearchData<T extends Preference> {
    /**
     * @return The navigation path for the given preference, such as "Player > Action buttons".
     */
    private static String getPreferenceNavigationString(Preference preference) {
        Deque<CharSequence> pathElements = new ArrayDeque<>();

        while (true) {
            preference = preference.getParent();

            if (preference == null) {
                if (pathElements.isEmpty()) {
                    return "";
                }
                Locale locale = BaseSettings.REVANCED_LANGUAGE.get().getLocale();
                return Utils.getTextDirectionString(locale) + String.join(" > ", pathElements);
            }

            if (!(preference instanceof NoTitlePreferenceCategory)
                    && !(preference instanceof SponsorBlockPreferenceGroup)) {
                CharSequence title = preference.getTitle();
                if (title != null && title.length() > 0) {
                    pathElements.addFirst(title);
                }
            }
        }
    }

    /**
     * Highlights the search query in the given text by applying color span.
     * @param text The original text to process.
     * @param queryPattern The search query to highlight.
     * @return The text with highlighted query matches as a SpannableStringBuilder.
     */
    static CharSequence highlightSearchQuery(CharSequence text, Pattern queryPattern) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        final int adjustedColor = Utils.adjustColorBrightness(Utils.getAppBackgroundColor(),
                0.95f, 1.20f);
        BackgroundColorSpan highlightSpan = new BackgroundColorSpan(adjustedColor);

        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        Matcher matcher = queryPattern.matcher(text);

        while (matcher.find()) {
            spannable.setSpan(
                    highlightSpan,
                    matcher.start(),
                    matcher.end(),
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        return spannable;
    }

    final T preference;
    final String key;
    final String navigationPath;
    boolean highlightingApplied;

    @Nullable
    CharSequence originalTitle;
    @Nullable
    String searchTitle;

    AbstractPreferenceSearchData(T pref) {
        preference = pref;
        key = Utils.removePunctuationToLowercase(pref.getKey());
        navigationPath = getPreferenceNavigationString(pref);
    }

    @CallSuper
    void updateSearchDataIfNeeded() {
        if (highlightingApplied) {
            // Must clear, otherwise old highlighting is still applied.
            clearHighlighting();
        }

        CharSequence title = preference.getTitle();
        if (originalTitle != title) { // Check using reference equality.
            originalTitle = title;
            searchTitle = Utils.removePunctuationToLowercase(title);
        }
    }

    @CallSuper
    boolean matchesSearchQuery(String query) {
        updateSearchDataIfNeeded();

        return key.contains(query)
                || searchTitle != null && searchTitle.contains(query);
    }

    @CallSuper
    void applyHighlighting(String query, Pattern queryPattern) {
        preference.setTitle(highlightSearchQuery(originalTitle, queryPattern));
        highlightingApplied = true;
    }

    @CallSuper
    void clearHighlighting() {
        if (highlightingApplied) {
            preference.setTitle(originalTitle);
            highlightingApplied = false;
        }
    }
}

/**
 * Regular preference type that only uses the base preference summary.
 * Should only be used if a more specific data class does not exist.
 */
@SuppressWarnings("deprecation")
class PreferenceSearchData extends AbstractPreferenceSearchData<Preference> {
    @Nullable
    CharSequence originalSummary;
    @Nullable
    String searchSummary;

    PreferenceSearchData(Preference pref) {
        super(pref);
    }

    void updateSearchDataIfNeeded() {
        super.updateSearchDataIfNeeded();

        CharSequence summary = preference.getSummary();
        if (originalSummary != summary) {
            originalSummary = summary;
            searchSummary = Utils.removePunctuationToLowercase(summary);
        }
    }

    boolean matchesSearchQuery(String query) {
        return super.matchesSearchQuery(query)
                || searchSummary != null && searchSummary.contains(query);
    }

    @Override
    void applyHighlighting(String query, Pattern queryPattern) {
        super.applyHighlighting(query, queryPattern);

        preference.setSummary(highlightSearchQuery(originalSummary, queryPattern));
    }

    @CallSuper
    void clearHighlighting() {
        if (highlightingApplied) {
            preference.setSummary(originalSummary);
        }

        super.clearHighlighting();
    }
}

/**
 * Switch preference type that uses summaryOn and summaryOff.
 */
@SuppressWarnings("deprecation")
class SwitchPreferenceSearchData extends AbstractPreferenceSearchData<SwitchPreference> {
    @Nullable
    CharSequence originalSummaryOn, originalSummaryOff;
    @Nullable
    String searchSummaryOn, searchSummaryOff;

    SwitchPreferenceSearchData(SwitchPreference pref) {
        super(pref);
    }

    void updateSearchDataIfNeeded() {
        super.updateSearchDataIfNeeded();

        CharSequence summaryOn = preference.getSummaryOn();
        if (originalSummaryOn != summaryOn) {
            originalSummaryOn = summaryOn;
            searchSummaryOn = Utils.removePunctuationToLowercase(summaryOn);
        }

        CharSequence summaryOff = preference.getSummaryOff();
        if (originalSummaryOff != summaryOff) {
            originalSummaryOff = summaryOff;
            searchSummaryOff = Utils.removePunctuationToLowercase(summaryOff);
        }
    }

    boolean matchesSearchQuery(String query) {
        return super.matchesSearchQuery(query)
                || searchSummaryOn != null && searchSummaryOn.contains(query)
                || searchSummaryOff != null && searchSummaryOff.contains(query);
    }

    @Override
    void applyHighlighting(String query, Pattern queryPattern) {
        super.applyHighlighting(query, queryPattern);

        preference.setSummaryOn(highlightSearchQuery(originalSummaryOn, queryPattern));
        preference.setSummaryOff(highlightSearchQuery(originalSummaryOff, queryPattern));
    }

    @CallSuper
    void clearHighlighting() {
        if (highlightingApplied) {
            preference.setSummaryOn(originalSummaryOn);
            preference.setSummaryOff(originalSummaryOff);
        }

        super.clearHighlighting();
    }
}

/**
 * List preference type that uses entries.
 */
@SuppressWarnings("deprecation")
class ListPreferenceSearchData extends AbstractPreferenceSearchData<ListPreference> {
    @Nullable
    CharSequence[] originalEntries;
    @Nullable
    String searchEntries;

    ListPreferenceSearchData(ListPreference pref) {
        super(pref);
    }

    void updateSearchDataIfNeeded() {
        super.updateSearchDataIfNeeded();

        CharSequence[] entries = preference.getEntries();
        if (originalEntries != entries) {
            originalEntries = entries;
            searchEntries = Utils.removePunctuationToLowercase(String.join(" ", entries));
        }
    }

    boolean matchesSearchQuery(String query) {
        return super.matchesSearchQuery(query)
                || searchEntries != null && searchEntries.contains(query);
    }

    @Override
    void applyHighlighting(String query, Pattern queryPattern) {
        super.applyHighlighting(query, queryPattern);

        if (originalEntries != null) {
            final int length = originalEntries.length;
            CharSequence[] highlightedEntries = new CharSequence[length];

            for (int i = 0; i < length; i++) {
                highlightedEntries[i] = highlightSearchQuery(originalEntries[i], queryPattern);

                // Cannot highlight the summary text, because ListPreference uses
                // the toString() of the summary CharSequence which strips away all formatting.
            }

            preference.setEntries(highlightedEntries);
        }
    }

    @CallSuper
    void clearHighlighting() {
        if (highlightingApplied) {
            preference.setEntries(originalEntries);
        }

        super.clearHighlighting();
    }
}
