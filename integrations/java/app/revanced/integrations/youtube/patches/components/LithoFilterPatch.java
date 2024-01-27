package app.revanced.integrations.youtube.patches.components;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.BooleanSetting;
import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.shared.settings.StringSetting;
import app.revanced.integrations.youtube.ByteTrieSearch;
import app.revanced.integrations.youtube.StringTrieSearch;
import app.revanced.integrations.youtube.TrieSearch;
import app.revanced.integrations.youtube.settings.Settings;

abstract class FilterGroup<T> {
    final static class FilterGroupResult {
        private BooleanSetting setting;
        private int matchedIndex;
        private int matchedLength;
        // In the future it might be useful to include which pattern matched,
        // but for now that is not needed.

        FilterGroupResult() {
            this(null, -1, 0);
        }

        FilterGroupResult(BooleanSetting setting, int matchedIndex, int matchedLength) {
            setValues(setting, matchedIndex, matchedLength);
        }

        public void setValues(BooleanSetting setting, int matchedIndex, int matchedLength) {
            this.setting = setting;
            this.matchedIndex = matchedIndex;
            this.matchedLength = matchedLength;
        }

        /**
         * A null value if the group has no setting,
         * or if no match is returned from {@link FilterGroupList#check(Object)}.
         */
        public BooleanSetting getSetting() {
            return setting;
        }

        public boolean isFiltered() {
            return matchedIndex >= 0;
        }

        /**
         * Matched index of first pattern that matched, or -1 if nothing matched.
         */
        public int getMatchedIndex() {
            return matchedIndex;
        }

        /**
         * Length of the matched filter pattern.
         */
        public int getMatchedLength() {
            return matchedLength;
        }
    }

    protected final BooleanSetting setting;
    protected final T[] filters;

    /**
     * Initialize a new filter group.
     *
     * @param setting The associated setting.
     * @param filters The filters.
     */
    @SafeVarargs
    public FilterGroup(final BooleanSetting setting, final T... filters) {
        this.setting = setting;
        this.filters = filters;
        if (filters.length == 0) {
            throw new IllegalArgumentException("Must use one or more filter patterns (zero specified)");
        }
    }

    public boolean isEnabled() {
        return setting == null || setting.get();
    }

    /**
     * @return If {@link FilterGroupList} should include this group when searching.
     * By default, all filters are included except non enabled settings that require reboot.
     */
    public boolean includeInSearch() {
        return isEnabled() || !setting.rebootApp;
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + (setting == null ? "(null setting)" : setting);
    }

    public abstract FilterGroupResult check(final T stack);
}

class StringFilterGroup extends FilterGroup<String> {

    public StringFilterGroup(final BooleanSetting setting, final String... filters) {
        super(setting, filters);
    }

    @Override
    public FilterGroupResult check(final String string) {
        int matchedIndex = -1;
        int matchedLength = 0;
        if (isEnabled()) {
            for (String pattern : filters) {
                if (!string.isEmpty()) {
                    final int indexOf = pattern.indexOf(string);
                    if (indexOf >= 0) {
                        matchedIndex = indexOf;
                        matchedLength = pattern.length();
                        break;
                    }
                }
            }
        }
        return new FilterGroupResult(setting, matchedIndex, matchedLength);
    }
}

final class CustomFilterGroup extends StringFilterGroup {

    private static String[] getFilterPatterns(StringSetting setting) {
        String[] patterns = setting.get().split("\\s+");
        for (String pattern : patterns) {
            if (!StringTrieSearch.isValidPattern(pattern)) {
                Utils.showToastLong("Invalid custom filter, resetting to default");
                setting.resetToDefault();
                return getFilterPatterns(setting);
            }
        }
        return patterns;
    }

    public CustomFilterGroup(BooleanSetting setting, StringSetting filter) {
        super(setting, getFilterPatterns(filter));
    }
}

/**
 * If you have more than 1 filter patterns, then all instances of
 * this class should filtered using {@link ByteArrayFilterGroupList#check(byte[])},
 * which uses a prefix tree to give better performance.
 */
class ByteArrayFilterGroup extends FilterGroup<byte[]> {

    private volatile int[][] failurePatterns;

    // Modified implementation from https://stackoverflow.com/a/1507813
    private static int indexOf(final byte[] data, final byte[] pattern, final int[] failure) {
        // Finds the first occurrence of the pattern in the byte array using
        // KMP matching algorithm.
        int patternLength = pattern.length;
        for (int i = 0, j = 0, dataLength = data.length; i < dataLength; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == patternLength) {
                return i - patternLength + 1;
            }
        }
        return -1;
    }

    private static int[] createFailurePattern(byte[] pattern) {
        // Computes the failure function using a boot-strapping process,
        // where the pattern is matched against itself.
        final int patternLength = pattern.length;
        final int[] failure = new int[patternLength];

        for (int i = 1, j = 0; i < patternLength; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

    public ByteArrayFilterGroup(BooleanSetting setting, byte[]... filters) {
        super(setting, filters);
    }

    /**
     * Converts the Strings into byte arrays. Used to search for text in binary data.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public ByteArrayFilterGroup(BooleanSetting setting, String... filters) {
        super(setting, Arrays.stream(filters).map(String::getBytes).toArray(byte[][]::new));
    }

    private synchronized void buildFailurePatterns() {
        if (failurePatterns != null) return; // Thread race and another thread already initialized the search.
        Logger.printDebug(() -> "Building failure array for: " + this);
        int[][] failurePatterns = new int[filters.length][];
        int i = 0;
        for (byte[] pattern : filters) {
            failurePatterns[i++] = createFailurePattern(pattern);
        }
        this.failurePatterns = failurePatterns; // Must set after initialization finishes.
    }

    @Override
    public FilterGroupResult check(final byte[] bytes) {
        int matchedLength = 0;
        int matchedIndex = -1;
        if (isEnabled()) {
            int[][] failures = failurePatterns;
            if (failures == null) {
                buildFailurePatterns(); // Lazy load.
                failures = failurePatterns;
            }
            for (int i = 0, length = filters.length; i < length; i++) {
                byte[] filter = filters[i];
                matchedIndex = indexOf(bytes, filter, failures[i]);
                if (matchedIndex >= 0) {
                    matchedLength = filter.length;
                    break;
                }
            }
        }
        return new FilterGroupResult(setting, matchedIndex, matchedLength);
    }
}


abstract class FilterGroupList<V, T extends FilterGroup<V>> implements Iterable<T> {

    private final List<T> filterGroups = new ArrayList<>();
    private final TrieSearch<V> search = createSearchGraph();

    @SafeVarargs
    protected final void addAll(final T... groups) {
        filterGroups.addAll(Arrays.asList(groups));

        for (T group : groups) {
            if (!group.includeInSearch()) {
                continue;
            }
            for (V pattern : group.filters) {
                search.addPattern(pattern, (textSearched, matchedStartIndex, matchedLength, callbackParameter) -> {
                    if (group.isEnabled()) {
                        FilterGroup.FilterGroupResult result = (FilterGroup.FilterGroupResult) callbackParameter;
                        result.setValues(group.setting, matchedStartIndex, matchedLength);
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return filterGroups.iterator();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void forEach(@NonNull Consumer<? super T> action) {
        filterGroups.forEach(action);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Spliterator<T> spliterator() {
        return filterGroups.spliterator();
    }

    protected FilterGroup.FilterGroupResult check(V stack) {
        FilterGroup.FilterGroupResult result = new FilterGroup.FilterGroupResult();
        search.matches(stack, result);
        return result;

    }

    protected abstract TrieSearch<V> createSearchGraph();
}

final class StringFilterGroupList extends FilterGroupList<String, StringFilterGroup> {
    protected StringTrieSearch createSearchGraph() {
        return new StringTrieSearch();
    }
}

/**
 * If searching for a single byte pattern, then it is slightly better to use
 * {@link ByteArrayFilterGroup#check(byte[])} as it uses KMP which is faster
 * than a prefix tree to search for only 1 pattern.
 */
final class ByteArrayFilterGroupList extends FilterGroupList<byte[], ByteArrayFilterGroup> {
    protected ByteTrieSearch createSearchGraph() {
        return new ByteTrieSearch();
    }
}

/**
 * Filters litho based components.
 *
 * Callbacks to filter content are added using {@link #addIdentifierCallbacks(StringFilterGroup...)}
 * and {@link #addPathCallbacks(StringFilterGroup...)}.
 *
 * To filter {@link FilterContentType#PROTOBUFFER}, first add a callback to
 * either an identifier or a path.
 * Then inside {@link #isFiltered(String, String, byte[], StringFilterGroup, FilterContentType, int)}
 * search for the buffer content using either a {@link ByteArrayFilterGroup} (if searching for 1 pattern)
 * or a {@link ByteArrayFilterGroupList} (if searching for more than 1 pattern).
 *
 * All callbacks must be registered before the constructor completes.
 */
abstract class Filter {

    public enum FilterContentType {
        IDENTIFIER,
        PATH,
        PROTOBUFFER
    }

    /**
     * Identifier callbacks.  Do not add to this instance,
     * and instead use {@link #addIdentifierCallbacks(StringFilterGroup...)}.
     */
    protected final List<StringFilterGroup> identifierCallbacks = new ArrayList<>();
    /**
     * Path callbacks. Do not add to this instance,
     * and instead use {@link #addPathCallbacks(StringFilterGroup...)}.
     */
    protected final List<StringFilterGroup> pathCallbacks = new ArrayList<>();

    /**
     * Adds callbacks to {@link #isFiltered(String, String, byte[], StringFilterGroup, FilterContentType, int)}
     * if any of the groups are found.
     */
    protected final void addIdentifierCallbacks(StringFilterGroup... groups) {
        identifierCallbacks.addAll(Arrays.asList(groups));
    }

    /**
     * Adds callbacks to {@link #isFiltered(String, String, byte[], StringFilterGroup, FilterContentType, int)}
     * if any of the groups are found.
     */
    protected final void addPathCallbacks(StringFilterGroup... groups) {
        pathCallbacks.addAll(Arrays.asList(groups));
    }

    /**
     * Called after an enabled filter has been matched.
     * Default implementation is to always filter the matched component and log the action.
     * Subclasses can perform additional or different checks if needed.
     * <p>
     * If the content is to be filtered, subclasses should always
     * call this method (and never return a plain 'true').
     * That way the logs will always show when a component was filtered and which filter hide it.
     * <p>
     * Method is called off the main thread.
     *
     * @param matchedGroup The actual filter that matched.
     * @param contentType  The type of content matched.
     * @param contentIndex Matched index of the identifier or path.
     * @return True if the litho component should be filtered out.
     */
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (BaseSettings.DEBUG.get()) {
            String filterSimpleName = getClass().getSimpleName();
            if (contentType == FilterContentType.IDENTIFIER) {
                Logger.printDebug(() -> filterSimpleName + " Filtered identifier: " + identifier);
            } else {
                Logger.printDebug(() -> filterSimpleName + " Filtered path: " + path);
            }
        }
        return true;
    }
}

/**
 * Placeholder for actual filters.
 */
final class DummyFilter extends Filter { }

@RequiresApi(api = Build.VERSION_CODES.N)
@SuppressWarnings("unused")
public final class LithoFilterPatch {
    /**
     * Simple wrapper to pass the litho parameters through the prefix search.
     */
    private static final class LithoFilterParameters {
        @Nullable
        final String identifier;
        final String path;
        final byte[] protoBuffer;

        LithoFilterParameters(@Nullable String lithoIdentifier, StringBuilder lithoPath, ByteBuffer protoBuffer) {
            this.identifier = lithoIdentifier;
            this.path = lithoPath.toString();
            this.protoBuffer = protoBuffer.array();
        }

        @NonNull
        @Override
        public String toString() {
            // Estimate the percentage of the buffer that are Strings.
            StringBuilder builder = new StringBuilder(protoBuffer.length / 2);
            builder.append( "ID: ");
            builder.append(identifier);
            builder.append(" Path: ");
            builder.append(path);
            if (Settings.DEBUG_PROTOBUFFER.get()) {
                builder.append(" BufferStrings: ");
                findAsciiStrings(builder, protoBuffer);
            }

            return builder.toString();
        }

        /**
         * Search through a byte array for all ASCII strings.
         */
        private static void findAsciiStrings(StringBuilder builder, byte[] buffer) {
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

    private static final Filter[] filters = new Filter[] {
            new DummyFilter() // Replaced by patch.
    };

    private static final StringTrieSearch pathSearchTree = new StringTrieSearch();
    private static final StringTrieSearch identifierSearchTree = new StringTrieSearch();

    /**
     * Because litho filtering is multi-threaded and the buffer is passed in from a different injection point,
     * the buffer is saved to a ThreadLocal so each calling thread does not interfere with other threads.
     */
    private static final ThreadLocal<ByteBuffer> bufferThreadLocal = new ThreadLocal<>();

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
        for (StringFilterGroup group : groups) {
            if (!group.includeInSearch()) {
                continue;
            }
            for (String pattern : group.filters) {
                pathSearchTree.addPattern(pattern, (textSearched, matchedStartIndex, matchedLength, callbackParameter) -> {
                            if (!group.isEnabled()) return false;
                            LithoFilterParameters parameters = (LithoFilterParameters) callbackParameter;
                            return filter.isFiltered(parameters.identifier, parameters.path, parameters.protoBuffer,
                                    group, type, matchedStartIndex);
                        }
                );
            }
        }
    }

    /**
     * Injection point.  Called off the main thread.
     */
    @SuppressWarnings("unused")
    public static void setProtoBuffer(@NonNull ByteBuffer protobufBuffer) {
        // Set the buffer to a thread local.  The buffer will remain in memory, even after the call to #filter completes.
        // This is intentional, as it appears the buffer can be set once and then filtered multiple times.
        // The buffer will be cleared from memory after a new buffer is set by the same thread,
        // or when the calling thread eventually dies.
        bufferThreadLocal.set(protobufBuffer);
    }

    /**
     * Injection point.  Called off the main thread, and commonly called by multiple threads at the same time.
     */
    @SuppressWarnings("unused")
    public static boolean filter(@Nullable String lithoIdentifier, @NonNull StringBuilder pathBuilder) {
        try {
            // It is assumed that protobufBuffer is empty as well in this case.
            if (pathBuilder.length() == 0)
                return false;

            ByteBuffer protobufBuffer = bufferThreadLocal.get();
            if (protobufBuffer == null) {
                Logger.printException(() -> "Proto buffer is null"); // Should never happen.
                return false;
            }

            if (!protobufBuffer.hasArray()) {
                Logger.printDebug(() -> "Proto buffer does not have an array");
                return false;
            }

            LithoFilterParameters parameter = new LithoFilterParameters(lithoIdentifier, pathBuilder, protobufBuffer);
            Logger.printDebug(() -> "Searching " + parameter);

            if (parameter.identifier != null) {
                if (identifierSearchTree.matches(parameter.identifier, parameter)) return true;
            }
            if (pathSearchTree.matches(parameter.path, parameter)) return true;
        } catch (Exception ex) {
            Logger.printException(() -> "Litho filter failure", ex);
        }

        return false;
    }
}