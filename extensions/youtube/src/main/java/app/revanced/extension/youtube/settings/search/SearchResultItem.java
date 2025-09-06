package app.revanced.extension.youtube.settings.search;

import static app.revanced.extension.youtube.settings.search.SearchViewController.DRAWABLE_REVANCED_SETTINGS_INFO;
import static app.revanced.extension.youtube.settings.search.SearchViewController.DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON;

import android.graphics.Color;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategoryListPreference;

/**
 * Abstract base class for search result items, defining common fields and behavior.
 */
@SuppressWarnings("deprecation")
public abstract class SearchResultItem {
    static final int TYPE_REGULAR = 0;
    static final int TYPE_SWITCH = 1;
    static final int TYPE_LIST = 2;
    static final int TYPE_COLOR_PICKER = 3;
    static final int TYPE_SEGMENT_CATEGORY = 4;
    static final int TYPE_GROUP_HEADER = 5;
    static final int TYPE_NO_RESULTS = 6;

    final String navigationPath;
    final List<String> navigationKeys;
    final int preferenceType;
    CharSequence title;
    CharSequence summary;
    boolean highlightingApplied;

    SearchResultItem(String navPath, List<String> navKeys, int type) {
        this.navigationPath = navPath;
        this.navigationKeys = new ArrayList<>(navKeys != null ? navKeys : Collections.emptyList());
        this.preferenceType = type;
        this.highlightingApplied = false;
    }

    abstract boolean matchesQuery(String query);
    abstract void applyHighlighting(Pattern queryPattern);
    abstract void clearHighlighting();

    // Shared method for highlighting text with search query.
    protected static CharSequence highlightSearchQuery(CharSequence text, Pattern queryPattern) {
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
     * Search result item for group headers (navigation path only).
     */
    public static class GroupHeaderItem extends SearchResultItem {
        GroupHeaderItem(String navPath, List<String> navKeys) {
            super(navPath, navKeys, TYPE_GROUP_HEADER);
            this.title = navPath;
            this.summary = "";
        }

        @Override
        boolean matchesQuery(String query) {
            return false; // Headers are not directly searchable.
        }

        @Override
        void applyHighlighting(Pattern queryPattern) {
            if (highlightingApplied) return;
            title = highlightSearchQuery(navigationPath, queryPattern);
            highlightingApplied = true;
        }

        @Override
        void clearHighlighting() {
            if (!highlightingApplied) return;
            title = navigationPath;
            highlightingApplied = false;
        }
    }

    /**
     * Search result item for preferences, handling type-specific data and search text.
     */
    public static class PreferenceSearchItem extends SearchResultItem {
        final Preference preference;
        final String searchableText;
        final int iconResourceId;
        final CharSequence originalTitle;
        CharSequence originalSummary;
        CharSequence originalSummaryOn;
        CharSequence originalSummaryOff;
        CharSequence[] originalEntries;

        @ColorInt
        private int color;

        PreferenceSearchItem(Preference pref, String navPath, List<String> navKeys) {
            super(navPath, navKeys, determineType(pref));
            this.preference = pref;
            this.originalTitle = pref.getTitle();
            this.title = originalTitle != null ? originalTitle : "";
            this.originalSummary = pref.getSummary();
            this.summary = originalSummary != null ? originalSummary : "";
            this.originalSummaryOn = null;
            this.originalSummaryOff = null;
            this.originalEntries = null;
            this.color = 0;

            // Initialize type-specific fields.
            initTypeSpecificFields(pref);

            // Determine icon for special placeholders.
            this.iconResourceId = determineIcon(pref, pref.getKey());

            // Build searchable text.
            this.searchableText = buildSearchableText(pref);
        }

        static String noResultsPlaceholderKey  = "no_results_placeholder";
        static String searchTipsPlaceholderKey = "search_tips_placeholder";

        private static int determineType(Preference pref) {
            if (pref instanceof SwitchPreference) return TYPE_SWITCH;
            if (pref instanceof ListPreference && !(pref instanceof SegmentCategoryListPreference)) return TYPE_LIST;
            if (pref instanceof ColorPickerPreference) return TYPE_COLOR_PICKER;
            if (pref instanceof SegmentCategoryListPreference) return TYPE_SEGMENT_CATEGORY;
            if (noResultsPlaceholderKey.equals(pref.getKey())
                    || searchTipsPlaceholderKey.equals(pref.getKey())) return TYPE_NO_RESULTS;
            return TYPE_REGULAR;
        }

        private void initTypeSpecificFields(Preference pref) {
            if (pref instanceof SwitchPreference switchPref) {
                this.originalSummaryOn = switchPref.getSummaryOn();
                this.originalSummaryOff = switchPref.getSummaryOff();
            } else if (pref instanceof ListPreference listPref && !(pref instanceof SegmentCategoryListPreference)) {
                this.originalEntries = listPref.getEntries();
            } else if (pref instanceof ColorPickerPreference colorPref) {
                String colorString = colorPref.getText();
                this.color = TextUtils.isEmpty(colorString) ? 0 : (Color.parseColor(colorString) | 0xFF000000);
            } else if (pref instanceof SegmentCategoryListPreference segmentPref) {
                this.originalEntries = segmentPref.getEntries();
                this.color = segmentPref.getColorWithOpacity();
            }
        }

        private static int determineIcon(Preference pref, String key) {
            if (pref.getIcon() != null) {
                if (noResultsPlaceholderKey.equals(key)) return DRAWABLE_REVANCED_SETTINGS_SEARCH_ICON;
                if (searchTipsPlaceholderKey.equals(key)) return DRAWABLE_REVANCED_SETTINGS_INFO;
            }
            return 0;
        }

        private String buildSearchableText(Preference pref) {
            StringBuilder searchBuilder = new StringBuilder();
            String key = pref.getKey();
            String normalizedKey = "";
            if (key != null) {
                // Normalize preference key by removing the common "revanced_" prefix
                // so that users can search by the meaningful part only.
                normalizedKey = key.startsWith("revanced_")
                        ? key.substring("revanced_".length())
                        : key;
            }
            appendText(searchBuilder, normalizedKey);
            appendText(searchBuilder, originalTitle);
            appendText(searchBuilder, originalSummary);

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
            } else if (pref instanceof ColorPickerPreference) {
                appendText(searchBuilder, ColorPickerPreference.getColorString(color));
            }

            // Include navigation path in searchable text.
            appendText(searchBuilder, navigationPath);

            return searchBuilder.toString();
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
        @Override
        boolean matchesQuery(String query) {
            return searchableText.contains(Utils.removePunctuationToLowercase(query));
        }

        /**
         * Highlights the search query in the given text by applying a background color span.
         */
        @Override
        void applyHighlighting(Pattern queryPattern) {
            if (highlightingApplied) return;
            title = highlightSearchQuery(originalTitle, queryPattern);
            preference.setTitle(title);
            if (originalSummary != null) {
                summary = highlightSearchQuery(originalSummary, queryPattern);
                preference.setSummary(summary);
            }
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
        @Override
        void clearHighlighting() {
            if (!highlightingApplied) return;
            title = originalTitle;
            preference.setTitle(originalTitle);
            summary = originalSummary;
            preference.setSummary(originalSummary);

            // Clear type-specific highlighting.
            if (preference instanceof SwitchPreference switchPref) {
                switchPref.setSummaryOn(originalSummaryOn);
                switchPref.setSummaryOff(originalSummaryOff);
            } else if (preference instanceof ListPreference listPref && originalEntries != null) {
                listPref.setEntries(originalEntries);
            }

            highlightingApplied = false;
        }

        void updateOriginalSummary(CharSequence newSummary) {
            this.originalSummary = newSummary;
            this.summary = newSummary != null ? newSummary : "";
            preference.setSummary(newSummary);
        }

        int getIconResourceId() {
            return iconResourceId;
        }

        public void setColor(int newColor) {
            this.color = newColor;
        }

        @ColorInt
        public int getColor() {
            return color;
        }
    }
}
