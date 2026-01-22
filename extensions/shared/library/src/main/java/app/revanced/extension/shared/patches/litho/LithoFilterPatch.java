package app.revanced.extension.shared.patches.litho;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.StringTrieSearch;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.YouTubeAndMusicSettings;

import app.revanced.extension.shared.patches.litho.FilterGroup.StringFilterGroup;

@SuppressWarnings("unused")
public final class LithoFilterPatch {
    /**
     * Simple wrapper to pass the litho parameters through the prefix search.
     */
    private static final class LithoFilterParameters {
        final String identifier;
        final String path;
        final byte[] buffer;

        LithoFilterParameters(String lithoIdentifier, String lithoPath, byte[] buffer) {
            this.identifier = lithoIdentifier;
            this.path = lithoPath;
            this.buffer = buffer;
        }

        @NonNull
        @Override
        public String toString() {
            // Estimate the percentage of the buffer that are Strings.
            StringBuilder builder = new StringBuilder(Math.max(100, buffer.length / 2));
            builder.append( "ID: ");
            builder.append(identifier);
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
     * Placeholder for actual filters.
     */
    private static final class DummyFilter extends Filter { }

    private static final Filter[] filters = new Filter[] {
            new DummyFilter() // Replaced patching, do not touch.
    };

    private static final StringTrieSearch pathSearchTree = new StringTrieSearch();
    private static final StringTrieSearch identifierSearchTree = new StringTrieSearch();

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Because litho filtering is multi-threaded and the buffer is passed in from a different injection point,
     * the buffer is saved to a ThreadLocal so each calling thread does not interfere with other threads.
     */
    private static final ThreadLocal<byte[]> bufferThreadLocal = new ThreadLocal<>();

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
                                    parameters.path, parameters.buffer, group, type, matchedStartIndex);

                            if (isFiltered && BaseSettings.DEBUG.get()) {
                                if (type == Filter.FilterContentType.IDENTIFIER) {
                                    Logger.printDebug(() -> "Filtered " + filterSimpleName
                                            + " identifier: " + parameters.identifier);
                                } else {
                                    Logger.printDebug(() -> "Filtered " + filterSimpleName
                                            + " path: " + parameters.path);
                                }
                            }

                            return isFiltered;
                        }
                );
            }
        }
    }

    /**
     * Injection point.  Called off the main thread.
     * Targets 20.22+
     */
    public static void setProtoBuffer(byte[] buffer) {
        // Set the buffer to a thread local.  The buffer will remain in memory, even after the call to #filter completes.
        // This is intentional, as it appears the buffer can be set once and then filtered multiple times.
        // The buffer will be cleared from memory after a new buffer is set by the same thread,
        // or when the calling thread eventually dies.
        bufferThreadLocal.set(buffer);
    }

    /**
     * Injection point.  Called off the main thread.
     * Targets 20.21 and lower.
     */
    public static void setProtoBuffer(@Nullable ByteBuffer buffer) {
        // Set the buffer to a thread local.  The buffer will remain in memory, even after the call to #filter completes.
        // This is intentional, as it appears the buffer can be set once and then filtered multiple times.
        // The buffer will be cleared from memory after a new buffer is set by the same thread,
        // or when the calling thread eventually dies.
        if (buffer == null || !buffer.hasArray()) {
            // It appears the buffer can be cleared out just before the call to #filter()
            // Ignore this null value and retain the last buffer that was set.
            Logger.printDebug(() -> "Ignoring null or empty buffer: " + buffer);
        } else {
            setProtoBuffer(buffer.array());
        }
    }

    /**
     * Injection point.
     */
    public static boolean isFiltered(String lithoIdentifier, StringBuilder pathBuilder) {
        try {
            if (lithoIdentifier.isEmpty() && pathBuilder.length() == 0) {
                return false;
            }

            byte[] buffer = bufferThreadLocal.get();
            // Potentially the buffer may have been null or never set up until now.
            // Use an empty buffer so the litho id/path filters still work correctly.
            if (buffer == null) {
                buffer = EMPTY_BYTE_ARRAY;
            }

            LithoFilterParameters parameter = new LithoFilterParameters(
                    lithoIdentifier, pathBuilder.toString(), buffer);
            Logger.printDebug(() -> "Searching " + parameter);

            if (identifierSearchTree.matches(parameter.identifier, parameter)) {
                return true;
            }

            if (pathSearchTree.matches(parameter.path, parameter)) {
                return true;
            }
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
