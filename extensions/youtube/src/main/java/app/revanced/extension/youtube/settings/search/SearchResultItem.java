package app.revanced.extension.youtube.settings.search;

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
import app.revanced.extension.shared.settings.preference.CustomDialogListPreference;
import app.revanced.extension.youtube.settings.preference.UrlLinkPreference;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategoryListPreference;

/**
 * Abstract base class for search result items, defining common fields and behavior.
 */
public abstract class SearchResultItem {
    // Enum to represent view types.
    public enum ViewType {
        REGULAR,
        SWITCH,
        LIST,
        COLOR_PICKER,
        SEGMENT_CATEGORY,
        GROUP_HEADER,
        NO_RESULTS,
        URL_LINK;

        // Get the corresponding layout resource ID.
        public int getLayoutResourceId() {
            return switch (this) {
                case REGULAR, URL_LINK -> getResourceIdentifier("revanced_preference_search_result_regular");
                case SWITCH -> getResourceIdentifier("revanced_preference_search_result_switch");
                case LIST   -> getResourceIdentifier("revanced_preference_search_result_list");
                case COLOR_PICKER, SEGMENT_CATEGORY -> getResourceIdentifier("revanced_preference_search_result_color");
                case GROUP_HEADER -> getResourceIdentifier("revanced_preference_search_result_group_header");
                case NO_RESULTS   -> getResourceIdentifier("revanced_preference_search_no_result");
            };
        }

        private static int getResourceIdentifier(String name) {
            // Placeholder for actual resource identifier retrieval.
            return Utils.getResourceIdentifier(name, "layout");
        }
    }

    final String navigationPath;
    final List<String> navigationKeys;
    final ViewType preferenceType;
    CharSequence highlightedTitle;
    CharSequence highlightedSummary;
    boolean highlightingApplied;

    SearchResultItem(String navPath, List<String> navKeys, ViewType type) {
        this.navigationPath = navPath;
        this.navigationKeys = new ArrayList<>(navKeys != null ? navKeys : Collections.emptyList());
        this.preferenceType = type;
        this.highlightedTitle = "";
        this.highlightedSummary = "";
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
            super(navPath, navKeys, ViewType.GROUP_HEADER);
            this.highlightedTitle = navPath;
        }

        @Override
        boolean matchesQuery(String query) {
            return false; // Headers are not directly searchable.
        }

        @Override
        void applyHighlighting(Pattern queryPattern) {}

        @Override
        void clearHighlighting() {}
    }

    /**
     * Search result item for preferences, handling type-specific data and search text.
     */
    @SuppressWarnings("deprecation")
    public static class PreferenceSearchItem extends SearchResultItem {
        final Preference preference;
        final String searchableText;
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
            this.originalTitle = pref.getTitle() != null ? pref.getTitle() : "";
            this.originalSummary = pref.getSummary();
            this.highlightedTitle = this.originalTitle;
            this.highlightedSummary = this.originalSummary != null ? this.originalSummary : "";
            this.originalSummaryOn = null;
            this.originalSummaryOff = null;
            this.originalEntries = null;
            this.color = 0;

            // Initialize type-specific fields.
            initTypeSpecificFields(pref);

            // Build searchable text.
            this.searchableText = buildSearchableText(pref);
        }

        private static ViewType determineType(Preference pref) {
            if (pref instanceof SwitchPreference) return ViewType.SWITCH;
            if (pref instanceof ListPreference && !(pref instanceof SegmentCategoryListPreference)) return ViewType.LIST;
            if (pref instanceof ColorPickerPreference) return ViewType.COLOR_PICKER;
            if (pref instanceof SegmentCategoryListPreference) return ViewType.SEGMENT_CATEGORY;
            if (pref instanceof UrlLinkPreference) return ViewType.URL_LINK;
            if ("no_results_placeholder".equals(pref.getKey())) return ViewType.NO_RESULTS;
            return ViewType.REGULAR;
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
         * Highlights the search query in the title, summary, and entries.
         * Applies adjust background color span to matching text.
         */
        @Override
        void applyHighlighting(Pattern queryPattern) {
            if (highlightingApplied) return;

            // Highlight the title.
            highlightedTitle = highlightSearchQuery(originalTitle, queryPattern);

            // Highlight the summary.
            if (originalSummary != null) {
                highlightedSummary = highlightSearchQuery(originalSummary, queryPattern);
            }

            // Highlight the entries.
            if (preference instanceof CustomDialogListPreference listPref && originalEntries != null) {
                CharSequence[] highlightedEntries = new CharSequence[originalEntries.length];
                for (int i = 0; i < originalEntries.length; i++) {
                    highlightedEntries[i] = highlightSearchQuery(originalEntries[i], queryPattern);
                }
                listPref.setEntries(highlightedEntries);
            }

            highlightingApplied = true;
        }

        /**
         * Clears all search query highlighting from the highlighted fields.
         * Restores original text for display purposes.
         */
        @Override
        void clearHighlighting() {
            if (!highlightingApplied) return;

            // Restore original title.
            highlightedTitle = originalTitle;

            // Restore original summary.
            highlightedSummary = originalSummary != null ? originalSummary : "";

            // Restore original entries.
            if (preference instanceof CustomDialogListPreference listPref) {
                listPref.restoreOriginalEntries();
            }

            highlightingApplied = false;
        }

        void updateOriginalSummary(CharSequence newSummary) {
            this.originalSummary = newSummary;
            this.highlightedSummary = newSummary != null ? newSummary : "";
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
