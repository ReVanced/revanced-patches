package app.revanced.extension.shared.patches.components;

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
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.ByteTrieSearch;
import app.revanced.extension.shared.patches.litho.FilterGroup.StringFilterGroup;
import app.revanced.extension.shared.settings.YouTubeAndMusicSettings;
import app.revanced.extension.shared.patches.litho.Filter;

/**
 * Allows custom filtering using a path and optionally a proto buffer string.
 */
@SuppressWarnings("unused")
public final class CustomFilter extends Filter {

    private static void showInvalidSyntaxToast(@NonNull String expression) {
        Utils.showToastLong(str("revanced_custom_filter_toast_invalid_syntax", expression));
    }

    private static class CustomFilterGroup extends StringFilterGroup {
        /**
         * Optional character for the path that indicates the custom filter path must match the start.
         * Must be the first character of the expression.
         */
        public static final String SYNTAX_STARTS_WITH = "^";

        /**
         * Optional character that separates the path from a proto buffer string pattern.
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

            // Map key is the path including optional special characters (^ and/or $)
            Map<String, CustomFilterGroup> result = new HashMap<>();
            Pattern pattern = Pattern.compile(
                    "(" // map key group
                            + "(\\Q" + SYNTAX_STARTS_WITH + "\\E?)" // optional starts with
                            + "([^\\Q" + SYNTAX_BUFFER_SYMBOL + "\\E]*)" // path
                            + "(\\Q" + SYNTAX_BUFFER_SYMBOL + "\\E?)" // optional buffer symbol
                            + ")" // end map key group
                            + "(.*)"); // optional buffer string

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
                final boolean hasBufferSymbol = !matcher.group(4).isEmpty();
                final String bufferString = matcher.group(5);

                if (path.isBlank() || (hasBufferSymbol && bufferString.isBlank())) {
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
                if (hasBufferSymbol) {
                    group.addBufferString(bufferString);
                }
            }

            return result.values();
        }

        final boolean startsWith;
        ByteTrieSearch bufferSearch;

        CustomFilterGroup(boolean startsWith, @NonNull String path) {
            super(YouTubeAndMusicSettings.CUSTOM_FILTER, path);
            this.startsWith = startsWith;
        }

        void addBufferString(@NonNull String bufferString) {
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
    public boolean isFiltered(String identifier, String path, byte[] buffer,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        // All callbacks are custom filter groups.
        CustomFilterGroup custom = (CustomFilterGroup) matchedGroup;
        if (custom.startsWith && contentIndex != 0) {
            return false;
        }

        if (custom.bufferSearch == null) {
            return true; // No buffer filter, only path filtering.
        }

        return custom.bufferSearch.matches(buffer);
    }
}
