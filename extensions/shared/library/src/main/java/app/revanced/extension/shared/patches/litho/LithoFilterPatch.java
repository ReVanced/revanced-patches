package app.revanced.extension.shared.patches.litho;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.patches.litho.FilterGroup.StringFilterGroup;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.StringTrieSearch;
import app.revanced.extension.shared.settings.YouTubeAndMusicSettings;

@SuppressWarnings("unused")
public final class LithoFilterPatch {
    /**
     * Simple wrapper to pass the litho parameters through the prefix search.
     */
    private static final class LithoFilterParameters {
        final String identifier;
        final String path;
        final String accessibility;
        final byte[] buffer;

        LithoFilterParameters(String lithoIdentifier, String lithoPath,
                              String accessibility, byte[] buffer) {
            this.identifier = lithoIdentifier;
            this.path = lithoPath;
            this.accessibility = accessibility;
            this.buffer = buffer;
        }

        @NonNull
        @Override
        public String toString() {
            // Estimate the percentage of the buffer that are Strings.
            StringBuilder builder = new StringBuilder(Math.max(100, buffer.length / 2));
            builder.append("ID: ");
            builder.append(identifier);
            if (!accessibility.isEmpty()) {
                // AccessibilityId and AccessibilityText are pieces of BufferStrings.
                builder.append(" Accessibility: ");
                builder.append(accessibility);
            }
            builder.append(" Path: ");
            builder.append(path);
            if (YouTubeAndMusicSettings.DEBUG_PROTOBUFFER.get()) {
                builder.append(" BufferStrings: ");
                findAsciiStrings(builder, buffer);
            }

            return builder.toString();
        }

        /**
         * Search through a byte array for all ASCII strings.
         */
        static void findAsciiStrings(StringBuilder builder, byte[] buffer) {
            // Valid ASCII values (ignore control characters).
            final int minimumAscii = 32;  // 32 = space character
            final int maximumAscii = 126; // 127 = delete character
            final int minimumAsciiStringLength = 4; // Minimum length of an ASCII string to include.
            String delimitingCharacter = "❙"; // Non ascii character, to allow easier log filtering.

            final int length = buffer.length;
            int start = 0;
            int end = 0;
            while (end < length) {
                int value = buffer[end];
                if (value < minimumAscii || value > maximumAscii || end == length - 1) {
                    if (end - start >= minimumAsciiStringLength) {
                        for (int i = start; i < end; i++) {
                            builder.append((char) buffer[i]);
                        }
                        builder.append(delimitingCharacter);
                    }
                    start = end + 1;
                }
                end++;
            }
        }
    }

    /**
     * Placeholder for actual filters.
     */
    private static final class DummyFilter extends Filter {
    }

    private static final Filter[] filters = new Filter[]{
            new DummyFilter() // Replaced during patching, do not touch.
    };

    /**
     * Litho layout fixed thread pool size override.
     * <p>
     * Unpatched YouTube uses a layout fixed thread pool between 1 and 3 threads:
     * <pre>
     * 1 thread - > Device has less than 6 cores
     * 2 threads -> Device has over 6 cores and less than 6GB of memory
     * 3 threads -> Device has over 6 cores and more than 6GB of memory
     * </pre>
     *
     * Using more than 1 thread causes layout issues such as the You tab watch/playlist shelf
     * that is sometimes incorrectly hidden (ReVanced is not hiding it), and seems to
     * fix a race issue if using the active navigation tab status with litho filtering.
     */
    private static final int LITHO_LAYOUT_THREAD_POOL_SIZE = 1;

    /**
     * For YouTube 20.22+, this is set to true by a patch,
     * because it cannot use the thread buffer due to the buffer frequently not being correct,
     * especially for components that are recreated such as dragging off-screen then back on screen.
     * Instead, parse the identifier found near the start of the buffer and use that to
     * identify the correct buffer to use when filtering.
     * <p>
     * <b>This is set during patching, do not change manually.</b>
     */
    private static final boolean EXTRACT_IDENTIFIER_FROM_BUFFER = false;

    /**
     * Turns on additional logging, used for development purposes only.
     */
    public static final boolean DEBUG_EXTRACT_IDENTIFIER_FROM_BUFFER = false;

    /**
     * String suffix for components.
     * Can be any of: ".eml", ".eml-fe", ".e-b", ".eml-js", "e-js-b"
     */
    private static final byte[] LITHO_COMPONENT_EXTENSION_BYTES = ".e".getBytes(StandardCharsets.US_ASCII);

    /**
     * Used as placeholder for litho id/path filters that do not use a buffer
     */
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Because litho filtering is multithreaded and the buffer is passed in from a different injection point,
     * the buffer is saved to a ThreadLocal so each calling thread does not interfere with other threads.
     * Used for 20.21 and lower.
     */
    private static final ThreadLocal<byte[]> bufferThreadLocal = new ThreadLocal<>();

    /**
     * Identifier to protocol buffer mapping.  Only used for 20.22+.
     * Thread local is needed because filtering is multithreaded and each thread can load
     * a different component with the same identifier.
     */
    private static final ThreadLocal<Map<String, byte[]>> identifierToBufferThread = new ThreadLocal<>();

    /**
     * Global shared buffer. Used only if the buffer is not found in the ThreadLocal.
     */
    private static final Map<String, byte[]> identifierToBufferGlobal
            = Collections.synchronizedMap(createIdentifierToBufferMap());

    private static final StringTrieSearch pathSearchTree = new StringTrieSearch();
    private static final StringTrieSearch identifierSearchTree = new StringTrieSearch();

    static {

        for (Filter filter : filters) {
            filterUsingCallbacks(identifierSearchTree, filter,
                    filter.identifierCallbacks, Filter.FilterContentType.IDENTIFIER);
            filterUsingCallbacks(pathSearchTree, filter,
                    filter.pathCallbacks, Filter.FilterContentType.PATH);
        }

        Logger.printDebug(() -> "Using: "
                + identifierSearchTree.numberOfPatterns() + " identifier filters"
                + " (" + identifierSearchTree.getEstimatedMemorySize() + " KB), "
                + pathSearchTree.numberOfPatterns() + " path filters"
                + " (" + pathSearchTree.getEstimatedMemorySize() + " KB)");
    }

    private static void filterUsingCallbacks(StringTrieSearch pathSearchTree,
                                             Filter filter, List<StringFilterGroup> groups,
                                             Filter.FilterContentType type) {
        String filterSimpleName = filter.getClass().getSimpleName();

        for (StringFilterGroup group : groups) {
            if (!group.includeInSearch()) {
                continue;
            }

            for (String pattern : group.filters) {
                pathSearchTree.addPattern(pattern, (textSearched, matchedStartIndex,
                                                    matchedLength, callbackParameter) -> {
                            if (!group.isEnabled()) return false;

                            LithoFilterParameters parameters = (LithoFilterParameters) callbackParameter;
                            final boolean isFiltered = filter.isFiltered(parameters.identifier,
                                    parameters.accessibility, parameters.path, parameters.buffer,
                                    group, type, matchedStartIndex);

                            if (isFiltered && BaseSettings.DEBUG.get()) {
                                Logger.printDebug(() -> type == Filter.FilterContentType.IDENTIFIER
                                        ? filterSimpleName + " filtered identifier: " + parameters.identifier
                                        : filterSimpleName + " filtered path: " + parameters.path);
                            }

                            return isFiltered;
                        }
                );
            }
        }
    }

    private static Map<String, byte[]> createIdentifierToBufferMap() {
        // It's unclear how many items should be cached. This is a guess.
        return Utils.createSizeRestrictedMap(100);
    }

    /**
     * Helper function that differs from {@link Character#isDigit(char)}
     * as this only matches ascii and not Unicode numbers.
     */
    private static boolean isAsciiNumber(byte character) {
        return '0' <= character && character <= '9';
    }

    private static boolean isAsciiLowerCaseLetter(byte character) {
        return 'a' <= character && character <= 'z';
    }

    /**
     * Injection point.  Called off the main thread.
     * Targets 20.22+
     */
    public static void setProtoBuffer(byte[] buffer) {
        if (DEBUG_EXTRACT_IDENTIFIER_FROM_BUFFER) {
            StringBuilder builder = new StringBuilder();
            LithoFilterParameters.findAsciiStrings(builder, buffer);
            Logger.printDebug(() -> "New buffer: " + builder);
        }

        // The identifier always seems to start very close to the buffer start.
        // Highest identifier start index ever observed is 50, with most around 30 to 40.
        // The buffer can be very large with up to 200kb has been observed,
        // so the search is restricted to only the start.
        final int maxBufferStartIndex = 500; // 10x expected upper bound.

        // Could use Boyer-Moore-Horspool since the string is ASCII and has a limited number of
        // unique characters, but it seems to be slower since the extra overhead of checking the
        // bad character array negates any performance gain of skipping a few extra subsearches.
        int emlIndex = -1;
        final int emlStringLength = LITHO_COMPONENT_EXTENSION_BYTES.length;
        final int lastBufferIndexToCheckFrom = Math.min(maxBufferStartIndex, buffer.length - emlStringLength);
        for (int i = 0; i < lastBufferIndexToCheckFrom; i++) {
            boolean match = true;
            for (int j = 0; j < emlStringLength; j++) {
                if (buffer[i + j] != LITHO_COMPONENT_EXTENSION_BYTES[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                emlIndex = i;
                break;
            }
        }

        if (emlIndex < 0) {
            // Buffer is not used for creating a new litho component.
            if (DEBUG_EXTRACT_IDENTIFIER_FROM_BUFFER) {
                Logger.printDebug(() -> "Could not find eml index");
            }
            return;
        }

        int startIndex = emlIndex - 1;
        while (startIndex > 0) {
            final byte character = buffer[startIndex];
            int startIndexFinal = startIndex;
            if (isAsciiLowerCaseLetter(character) || isAsciiNumber(character) || character == '_') {
                // Valid character for the first path element.
                startIndex--;
            } else {
                startIndex++;
                break;
            }
        }

        // Strip away any numbers on the start of the identifier, which can
        // be from random data in the buffer before the identifier starts.
        while (true) {
            final byte character = buffer[startIndex];
            if (isAsciiNumber(character)) {
                startIndex++;
            } else {
                break;
            }
        }

        // Find the pipe character after the identifier.
        int endIndex = -1;
        for (int i = emlIndex, length = buffer.length; i < length; i++) {
            if (buffer[i] == '|') {
                endIndex = i;
                break;
            }
        }
        if (endIndex < 0) {
            if (BaseSettings.DEBUG.get()) {
                Logger.printException(() -> "Debug: Could not find buffer identifier");
            }
            return;
        }

        String identifier = new String(buffer, startIndex, endIndex - startIndex, StandardCharsets.US_ASCII);
        if (DEBUG_EXTRACT_IDENTIFIER_FROM_BUFFER) {
            Logger.printDebug(() -> "Found buffer for identifier: " + identifier);
        }
        identifierToBufferGlobal.put(identifier, buffer);

        Map<String, byte[]> map = identifierToBufferThread.get();
        if (map == null) {
            map = createIdentifierToBufferMap();
            identifierToBufferThread.set(map);
        }
        map.put(identifier, buffer);
    }

    /**
     * Injection point.  Called off the main thread.
     * Targets 20.21 and lower.
     */
    public static void setProtoBuffer(@Nullable ByteBuffer buffer) {
        if (buffer == null || !buffer.hasArray()) {
            // It appears the buffer can be cleared out just before the call to #filter()
            // Ignore this null value and retain the last buffer that was set.
            Logger.printDebug(() -> "Ignoring null or empty buffer: " + buffer);
        } else {
            // Set the buffer to a thread local.  The buffer will remain in memory, even after the call to #filter completes.
            // This is intentional, as it appears the buffer can be set once and then filtered multiple times.
            // The buffer will be cleared from memory after a new buffer is set by the same thread,
            // or when the calling thread eventually dies.
            bufferThreadLocal.set(buffer.array());
        }
    }

    /**
     * Injection point.
     */
    public static boolean isFiltered(String identifier, @Nullable String accessibilityId,
                                     @Nullable String accessibilityText, StringBuilder pathBuilder) {
        try {
            if (identifier.isEmpty() || pathBuilder.length() == 0) {
                return false;
            }

            byte[] buffer = null;
            if (EXTRACT_IDENTIFIER_FROM_BUFFER) {
                final int pipeIndex = identifier.indexOf('|');
                if (pipeIndex >= 0) {
                    // If the identifier contains no pipe, then it's not an ".eml" identifier
                    // and the buffer is not uniquely identified. Typically, this only happens
                    // for subcomponents where buffer filtering is not used.
                    String identifierKey = identifier.substring(0, pipeIndex);

                    var map = identifierToBufferThread.get();
                    if (map != null) {
                        buffer = map.get(identifierKey);
                    }

                    if (buffer == null) {
                        // Buffer for thread local not found. Use the last buffer found from any thread.
                        buffer = identifierToBufferGlobal.get(identifierKey);

                        if (DEBUG_EXTRACT_IDENTIFIER_FROM_BUFFER && buffer == null) {
                            // No buffer is found for some components, such as
                            // shorts_lockup_cell.eml on channel profiles.
                            // For now, just ignore this and filter without a buffer.
                            if (BaseSettings.DEBUG.get()) {
                                Logger.printException(() -> "Debug: Could not find buffer for identifier: " + identifier);
                            }
                        }
                    }
                }
            } else {
                buffer = bufferThreadLocal.get();
            }

            // Potentially the buffer may have been null or never set up until now.
            // Use an empty buffer so the litho id/path filters that do not use a buffer still work.
            if (buffer == null) {
                buffer = EMPTY_BYTE_ARRAY;
            }

            String path = pathBuilder.toString();

            String accessibility = "";
            if (accessibilityId != null && !accessibilityId.isBlank()) {
                accessibility = accessibilityId;
            }
            if (accessibilityText != null && !accessibilityText.isBlank()) {
                accessibility = accessibilityId + '|' + accessibilityText;
            }
            LithoFilterParameters parameter = new LithoFilterParameters(identifier, path, accessibility, buffer);
            Logger.printDebug(() -> "Searching " + parameter);

            return identifierSearchTree.matches(identifier, parameter)
                    || pathSearchTree.matches(path, parameter);
        } catch (Exception ex) {
            Logger.printException(() -> "isFiltered failure", ex);
        }

        return false;
    }

    /**
     * Injection point.
     */
    public static int getExecutorCorePoolSize(int originalCorePoolSize) {
        if (originalCorePoolSize != LITHO_LAYOUT_THREAD_POOL_SIZE) {
            Logger.printDebug(() -> "Overriding core thread pool size from: " + originalCorePoolSize
                    + " to: " + LITHO_LAYOUT_THREAD_POOL_SIZE);
        }

        return LITHO_LAYOUT_THREAD_POOL_SIZE;
    }

    /**
     * Injection point.
     */
    public static int getExecutorMaxThreads(int originalMaxThreads) {
        if (originalMaxThreads != LITHO_LAYOUT_THREAD_POOL_SIZE) {
            Logger.printDebug(() -> "Overriding max thread pool size from: " + originalMaxThreads
                    + " to: " + LITHO_LAYOUT_THREAD_POOL_SIZE);
        }

        return LITHO_LAYOUT_THREAD_POOL_SIZE;
    }
}
