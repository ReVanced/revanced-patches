package app.revanced.extension.shared.patches.litho;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import app.revanced.extension.shared.ConversionContext.ContextInterface;
import app.revanced.extension.shared.Logger;
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
            if (YouTubeAndMusicSettings.DEBUG_PROTOCOLBUFFER.get()) {
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
            // Logger ignores text past 4096 bytes on each line. Must wrap lines otherwise logging is clipped.
            final int preferredLineLength = 3000; // Preferred length before wrapping on next substring.
            final int maxLineLength = 3300; // Hard limit to line wrap in the middle of substring.
            String delimitingCharacter = "❙"; // Non ascii character, to allow easier log filtering.

            final int length = buffer.length;
            final int lastIndex = length - 1;
            int start = 0;
            int currentLineLength = 0;

            for (int end = 0; end < length; end++) {
                final int value = buffer[end];
                final boolean isAscii = (value >= minimumAscii && value <= maximumAscii);
                final boolean atEnd = (end == lastIndex);

                if (!isAscii || atEnd) {
                    int wordEnd = end + ((atEnd && isAscii) ? 1 : 0);

                    if (wordEnd - start >= minimumAsciiStringLength) {
                        for (int i = start; i < wordEnd; i++) {
                            builder.append((char) buffer[i]);
                            currentLineLength++;

                            // Hard line limit. Hard wrap the current substring to next logger line.
                            if (currentLineLength >= maxLineLength) {
                                builder.append('\n');
                                currentLineLength = 0;
                            }
                        }

                        // Wrap after substring if over preferred limit.
                        if (currentLineLength >= preferredLineLength) {
                            builder.append('\n');
                            currentLineLength = 0;
                        }

                        builder.append(delimitingCharacter);
                        currentLineLength++;
                    }

                    start = end + 1;
                }
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

    /**
     * Injection point.  Called off the main thread.
     * Targets 20.21 and lower.
     */
    public static void setProtobufBuffer(@Nullable ByteBuffer buffer) {
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
    public static boolean isFiltered(ContextInterface contextInterface, @Nullable byte[] bytes,
                                     @Nullable String accessibilityId, @Nullable String accessibilityText) {
        try {
            String identifier = contextInterface.patch_getIdentifier();
            StringBuilder pathBuilder = contextInterface.patch_getPathBuilder();
            if (identifier.isEmpty() || pathBuilder.length() == 0) {
                return false;
            }

            byte[] buffer = EXTRACT_IDENTIFIER_FROM_BUFFER
                    ? bytes
                    : bufferThreadLocal.get();

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
