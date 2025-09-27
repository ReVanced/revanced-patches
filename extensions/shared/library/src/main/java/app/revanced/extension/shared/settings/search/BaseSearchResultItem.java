package app.revanced.extension.shared.settings.search;

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
import app.revanced.extension.shared.settings.preference.UrlLinkPreference;

/**
 * Abstract base class for search result items, defining common fields and behavior.
 */
public abstract class BaseSearchResultItem {
    // Enum to represent view types.
    public enum ViewType {
        REGULAR,
        SWITCH,
        LIST,
        COLOR_PICKER,
        GROUP_HEADER,
        NO_RESULTS,
        URL_LINK;

        // Get the corresponding layout resource ID.
        public int getLayoutResourceId() {
            return switch (this) {
                case REGULAR, URL_LINK -> getResourceIdentifier("revanced_preference_search_result_regular");
                case SWITCH -> getResourceIdentifier("revanced_preference_search_result_switch");
                case LIST   -> getResourceIdentifier("revanced_preference_search_result_list");
                case COLOR_PICKER -> getResourceIdentifier("revanced_preference_search_result_color");
                case GROUP_HEADER -> getResourceIdentifier("revanced_preference_search_result_group_header");
                case NO_RESULTS   -> getResourceIdentifier("revanced_preference_search_no_result");
            };
        }

        private static int getResourceIdentifier(String name) {
            // Placeholder for actual resource identifier retrieval.
            return Utils.getResourceIdentifierOrThrow(name, "layout");
        }
    }

    final String navigationPath;
    final List<String> navigationKeys;
    final ViewType preferenceType;
    CharSequence highlightedTitle;
    CharSequence highlightedSummary;
    boolean highlightingApplied;

    BaseSearchResultItem(String navPath, List<String> navKeys, ViewType type) {
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
    public static class GroupHeaderItem extends BaseSearchResultItem {
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
    public static class PreferenceSearchItem extends BaseSearchResultItem {
        public final Preference preference;
        final String searchableText;
        final CharSequence originalTitle;
        final CharSequence originalSummary;
        final CharSequence originalSummaryOn;
        final CharSequence originalSummaryOff;
        final CharSequence[] originalEntries;
        private CharSequence[] highlightedEntries;
        private boolean entriesHighlightingApplied;

        @ColorInt
        private int color;

        // Store last applied highlighting pattern to reapply when needed.
        Pattern lastQueryPattern;

        PreferenceSearchItem(Preference pref, String navPath, List<String> navKeys) {
            super(navPath, navKeys, determineType(pref));
            this.preference = pref;
            this.originalTitle = pref.getTitle() != null ? pref.getTitle() : "";
            this.originalSummary = pref.getSummary();
            this.highlightedTitle = this.originalTitle;
            this.highlightedSummary = this.originalSummary != null ? this.originalSummary : "";
            this.color = 0;
            this.lastQueryPattern = null;

            // Initialize type-specific fields.
            FieldInitializationResult result = initTypeSpecificFields(pref);
            this.originalSummaryOn = result.summaryOn;
            this.originalSummaryOff = result.summaryOff;
            this.originalEntries = result.entries;

            // Build searchable text.
            this.searchableText = buildSearchableText(pref);
        }

        private static class FieldInitializationResult {
            CharSequence summaryOn = null;
            CharSequence summaryOff = null;
            CharSequence[] entries = null;
        }

        private static ViewType determineType(Preference pref) {
            if (pref instanceof SwitchPreference) return ViewType.SWITCH;
            if (pref instanceof ListPreference) return ViewType.LIST;
            if (pref instanceof ColorPickerPreference) return ViewType.COLOR_PICKER;
            if (pref instanceof UrlLinkPreference) return ViewType.URL_LINK;
            if ("no_results_placeholder".equals(pref.getKey())) return ViewType.NO_RESULTS;
            return ViewType.REGULAR;
        }

        private FieldInitializationResult initTypeSpecificFields(Preference pref) {
            FieldInitializationResult result = new FieldInitializationResult();

            if (pref instanceof SwitchPreference switchPref) {
                result.summaryOn = switchPref.getSummaryOn();
                result.summaryOff = switchPref.getSummaryOff();
            } else if (pref instanceof ColorPickerPreference colorPref) {
                String colorString = colorPref.getText();
                this.color = TextUtils.isEmpty(colorString) ? 0 : Color.parseColor(colorString);
            } else if (pref instanceof ListPreference listPref) {
                result.entries = listPref.getEntries();
                if (result.entries != null) {
                    this.highlightedEntries = new CharSequence[result.entries.length];
                    System.arraycopy(result.entries, 0, this.highlightedEntries, 0, result.entries.length);
                }
            }

            this.entriesHighlightingApplied = false;
            return result;
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
            if (pref instanceof ListPreference) {
                if (originalEntries != null) {
                    for (CharSequence entry : originalEntries) {
                        appendText(searchBuilder, entry);
                    }
                }
            } else if (pref instanceof SwitchPreference) {
                appendText(searchBuilder, originalSummaryOn);
                appendText(searchBuilder, originalSummaryOff);
            } else if (pref instanceof ColorPickerPreference) {
                appendText(searchBuilder, ColorPickerPreference.getColorString(color, false));
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
         * Gets the current effective summary for this preference, considering state-dependent summaries.
         */
        public CharSequence getCurrentEffectiveSummary() {
            if (preference instanceof CustomDialogListPreference customPref) {
                String staticSum = customPref.getStaticSummary();
                if (staticSum != null) {
                    return staticSum;
                }
            }
            if (preference instanceof SwitchPreference switchPref) {
                boolean currentState = switchPref.isChecked();
                return currentState
                        ? (originalSummaryOn != null ? originalSummaryOn :
                        originalSummary != null ? originalSummary : "")
                        : (originalSummaryOff != null ? originalSummaryOff :
                        originalSummary != null ? originalSummary : "");
            } else if (preference instanceof ListPreference listPref) {
                String value = listPref.getValue();
                CharSequence[] entries = listPref.getEntries();
                CharSequence[] entryValues = listPref.getEntryValues();
                if (value != null && entries != null && entryValues != null) {
                    for (int i = 0, length = entries.length; i < length; i++) {
                        if (value.equals(entryValues[i].toString())) {
                            return originalEntries != null && i < originalEntries.length && originalEntries[i] != null
                                    ? originalEntries[i]
                                    : originalSummary != null ? originalSummary : "";
                        }
                    }
                }
                return originalSummary != null ? originalSummary : "";
            }
            return originalSummary != null ? originalSummary : "";
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
         * Get highlighted entries to show in dialog.
         */
        public CharSequence[] getHighlightedEntries() {
            return highlightedEntries;
        }

        /**
         * Whether highlighting is applied to entries.
         */
        public boolean isEntriesHighlightingApplied() {
            return entriesHighlightingApplied;
        }

        /**
         * Highlights the search query in the title and summary.
         */
        @Override
        void applyHighlighting(Pattern queryPattern) {
            this.lastQueryPattern = queryPattern;
            // Highlight the title.
            highlightedTitle = highlightSearchQuery(originalTitle, queryPattern);

            // Get the current effective summary and highlight it.
            CharSequence currentSummary = getCurrentEffectiveSummary();
            highlightedSummary = highlightSearchQuery(currentSummary, queryPattern);

            // Highlight the entries.
            if (preference instanceof ListPreference && originalEntries != null) {
                highlightedEntries = new CharSequence[originalEntries.length];
                for (int i = 0, length = originalEntries.length; i < length; i++) {
                    if (originalEntries[i] != null) {
                        highlightedEntries[i] = highlightSearchQuery(originalEntries[i], queryPattern);
                    } else {
                        highlightedEntries[i] = null;
                    }
                }
                entriesHighlightingApplied = true;
            }

            highlightingApplied = true;
        }

        /**
         * Clears all search query highlighting and restores original state completely.
         */
        @Override
        void clearHighlighting() {
            if (!highlightingApplied) return;

            // Restore original title.
            highlightedTitle = originalTitle;

            // Restore current effective summary without highlighting.
            highlightedSummary = getCurrentEffectiveSummary();

            // Restore original entries.
            if (originalEntries != null && highlightedEntries != null) {
                System.arraycopy(originalEntries, 0, highlightedEntries, 0,
                        Math.min(originalEntries.length, highlightedEntries.length));
            }

            entriesHighlightingApplied = false;
            highlightingApplied = false;
            lastQueryPattern = null;
        }

        /**
         * Refreshes highlighting for dynamic summaries (like switch preferences).
         * Should be called when the preference state changes.
         */
        public void refreshHighlighting() {
            if (highlightingApplied && lastQueryPattern != null) {
                CharSequence currentSummary = getCurrentEffectiveSummary();
                highlightedSummary = highlightSearchQuery(currentSummary, lastQueryPattern);
            }
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
