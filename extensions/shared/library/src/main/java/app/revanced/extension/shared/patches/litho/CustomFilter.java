package app.revanced.extension.shared.patches.litho;

import static app.revanced.extension.shared.StringRef.str;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.StringTrieSearch;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.ByteTrieSearch;
import app.revanced.extension.shared.patches.litho.FilterGroup.StringFilterGroup;
import app.revanced.extension.shared.settings.YouTubeAndMusicSettings;

/**
 * Allows custom filtering using a path and optionally a proto buffer string.
 */
@SuppressWarnings("unused")
public final class CustomFilter extends Filter {

    private static void showInvalidSyntaxToast(String expression) {
        Utils.showToastLong(str("revanced_custom_filter_toast_invalid_syntax", expression));
    }

    private static class CustomFilterGroup extends StringFilterGroup {
        /**
         * Optional character for the path that indicates the custom filter path must match the start.
         * Must be the first character of the expression.
         */
        public static final String SYNTAX_STARTS_WITH = "^";

        /**
         * Optional character that separates the path from an accessibility string pattern.
         */
        public static final String SYNTAX_ACCESSIBILITY_SYMBOL = "#";

        /**
         * Optional character that separates the path/accessibility from a proto buffer string pattern.
         */
        public static final String SYNTAX_BUFFER_SYMBOL = "$";

        /**
         * @return the parsed objects
         */
        @NonNull
        @SuppressWarnings("ConstantConditions")
        static Collection<CustomFilterGroup> parseCustomFilterGroups() {
            String rawCustomFilterText = YouTubeAndMusicSettings.CUSTOM_FILTER_STRINGS.get();
            if (rawCustomFilterText.isBlank()) {
                return Collections.emptyList();
            }

            // Map key is the full path including optional special characters (^, #, $),
            // and any accessibility pattern, but does not contain any buffer patterns.
            Map<String, CustomFilterGroup> result = new HashMap<>();

            Pattern pattern = Pattern.compile(
                    "(" // Map key group.
                            // Optional starts with.
                            + "(\\Q" + SYNTAX_STARTS_WITH + "\\E?)"
                            // Path string.
                            + "([^\\Q" + SYNTAX_ACCESSIBILITY_SYMBOL + SYNTAX_BUFFER_SYMBOL + "\\E]*)"
                            // Optional accessibility string.
                            + "(?:\\Q" + SYNTAX_ACCESSIBILITY_SYMBOL + "\\E([^\\Q" + SYNTAX_BUFFER_SYMBOL + "\\E]*))?"
                            // Optional buffer string.
                            + "(?:\\Q" + SYNTAX_BUFFER_SYMBOL + "\\E(.*))?"
                            + ")"); // end map key group

            for (String expression : rawCustomFilterText.split("\n")) {
                if (expression.isBlank()) continue;

                Matcher matcher = pattern.matcher(expression);
                if (!matcher.find()) {
                    showInvalidSyntaxToast(expression);
                    continue;
                }

                final String mapKey = matcher.group(1);
                final boolean pathStartsWith = !matcher.group(2).isEmpty();
                final String path = matcher.group(3);
                final String accessibility = matcher.group(4); // null if not present
                final String buffer = matcher.group(5); // null if not present

                if (path.isBlank()
                        || (accessibility != null && accessibility.isEmpty())
                        || (buffer != null && buffer.isEmpty())) {
                    showInvalidSyntaxToast(expression);
                    continue;
                }

                // Use one group object for all expressions with the same path.
                // This ensures the buffer is searched exactly once
                // when multiple paths are used with different buffer strings.
                CustomFilterGroup group = result.get(mapKey);
                if (group == null) {
                    group = new CustomFilterGroup(pathStartsWith, path);
                    result.put(mapKey, group);
                }

                if (accessibility != null) {
                    group.addAccessibilityString(accessibility);
                }

                if (buffer != null) {
                    group.addBufferString(buffer);
                }
            }

            return result.values();
        }

        final boolean startsWith;
        StringTrieSearch accessibilitySearch;
        ByteTrieSearch bufferSearch;

        CustomFilterGroup(boolean startsWith, String path) {
            super(YouTubeAndMusicSettings.CUSTOM_FILTER, path);
            this.startsWith = startsWith;
        }

        void addAccessibilityString(String accessibilityString) {
            if (accessibilitySearch == null) {
                accessibilitySearch = new StringTrieSearch();
            }
            accessibilitySearch.addPattern(accessibilityString);
        }

        void addBufferString(String bufferString) {
            if (bufferSearch == null) {
                bufferSearch = new ByteTrieSearch();
            }
            bufferSearch.addPattern(bufferString.getBytes());
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CustomFilterGroup{");
            if (accessibilitySearch != null) {
                builder.append(", accessibility=");
                builder.append(accessibilitySearch.getPatterns());
            }

            builder.append("path=");
            if (startsWith) builder.append(SYNTAX_STARTS_WITH);
            builder.append(filters[0]);

            if (bufferSearch != null) {
                String delimitingCharacter = "❙";
                builder.append(", bufferStrings=");
                builder.append(delimitingCharacter);
                for (byte[] bufferString : bufferSearch.getPatterns()) {
                    builder.append(new String(bufferString));
                    builder.append(delimitingCharacter);
                }
            }
            builder.append("}");
            return builder.toString();
        }
    }

    public CustomFilter() {
        Collection<CustomFilterGroup> groups = CustomFilterGroup.parseCustomFilterGroups();

        if (!groups.isEmpty()) {
            CustomFilterGroup[] groupsArray = groups.toArray(new CustomFilterGroup[0]);
            Logger.printDebug(()-> "Using Custom filters: " + Arrays.toString(groupsArray));
            addPathCallbacks(groupsArray);
        }
    }

    @Override
    public boolean isFiltered(String identifier, String accessibility, String path, byte[] buffer,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        // All callbacks are custom filter groups.
        CustomFilterGroup custom = (CustomFilterGroup) matchedGroup;

        // Check path start requirement.
        if (custom.startsWith && contentIndex != 0) {
            return false;
        }

        // Check accessibility string if specified.
        if (custom.accessibilitySearch != null && !custom.accessibilitySearch.matches(accessibility)) {
            return false;
        }

        // Check buffer if specified.
        if (custom.bufferSearch != null && !custom.bufferSearch.matches(buffer)) {
            return false;
        }

        return true; // All custom filter conditions passed.
    }
}
