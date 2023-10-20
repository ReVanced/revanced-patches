package app.revanced.integrations.patches.components;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;

abstract class FilterGroup<T> {
    final static class FilterGroupResult {
        private SettingsEnum setting;
        private int matchedIndex;
        private int matchedLength;
        // In the future it might be useful to include which pattern matched,
        // but for now that is not needed.

        FilterGroupResult() {
            this(null, -1, 0);
        }

        FilterGroupResult(SettingsEnum setting, int matchedIndex, int matchedLength) {
            setValues(setting, matchedIndex, matchedLength);
        }

        public void setValues(SettingsEnum setting, int matchedIndex, int matchedLength) {
            this.setting = setting;
            this.matchedIndex = matchedIndex;
            this.matchedLength = matchedLength;
        }

        /**
         * A null value if the group has no setting,
         * or if no match is returned from {@link FilterGroupList#check(Object)}.
         */
        public SettingsEnum getSetting() {
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

    protected final SettingsEnum setting;
    protected final T[] filters;

    /**
     * Initialize a new filter group.
     *
     * @param setting The associated setting.
     * @param filters The filters.
     */
    @SafeVarargs
    public FilterGroup(final SettingsEnum setting, final T... filters) {
        this.setting = setting;
        this.filters = filters;
        if (filters.length == 0) {
            throw new IllegalArgumentException("Must use one or more filter patterns (zero specified)");
        }
    }

    public boolean isEnabled() {
        return setting == null || setting.getBoolean();
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

    public StringFilterGroup(final SettingsEnum setting, final String... filters) {
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

    public CustomFilterGroup(final SettingsEnum setting, final SettingsEnum filter) {
        super(setting, filter.getString().split("\\s+"));
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

    public ByteArrayFilterGroup(SettingsEnum setting, byte[]... filters) {
        super(setting, filters);
    }

    private synchronized void buildFailurePatterns() {
        if (failurePatterns != null) return; // Thread race and another thread already initialized the search.
        LogHelper.printDebug(() -> "Building failure array for: " + this);
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
            if (failurePatterns == null) {
                buildFailurePatterns(); // Lazy load.
            }
            for (int i = 0, length = filters.length; i < length; i++) {
                byte[] filter = filters[i];
                matchedIndex = indexOf(bytes, filter, failurePatterns[i]);
                if (matchedIndex >= 0) {
                    matchedLength = filter.length;
                    break;
                }
            }
        }
        return new FilterGroupResult(setting, matchedIndex, matchedLength);
    }
}


final class ByteArrayAsStringFilterGroup extends ByteArrayFilterGroup {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ByteArrayAsStringFilterGroup(SettingsEnum setting, String... filters) {
        super(setting, Arrays.stream(filters).map(String::getBytes).toArray(byte[][]::new));
    }
}

abstract class FilterGroupList<V, T extends FilterGroup<V>> implements Iterable<T> {

    private final List<T> filterGroups = new ArrayList<>();
    /**
     * Search graph. Created only if needed.
     */
    private volatile TrieSearch<V> search;

    @SafeVarargs
    protected final void addAll(final T... groups) {
        filterGroups.addAll(Arrays.asList(groups));
        search = null; // Rebuild, if already created.
    }

    protected final synchronized void buildSearch() {
        // Since litho filtering is multi-threaded, this method can be concurrently called by multiple threads.
        if (search != null) return; // Thread race and another thread already initialized the search.
        LogHelper.printDebug(() -> "Creating prefix search tree for: " + this);
        TrieSearch<V> search = createSearchGraph();
        for (T group : filterGroups) {
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
        this.search = search; // Must set after it's completely initialized.
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
        if (search == null) {
            buildSearch(); // Lazy load.
        }
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

abstract class Filter {
    /**
     * All group filters must be set before the constructor call completes.
     * Otherwise {@link #isFiltered(String, String, byte[], FilterGroupList, FilterGroup, int)}
     * will never be called for any matches.
     */

    protected final StringFilterGroupList pathFilterGroupList = new StringFilterGroupList();
    protected final StringFilterGroupList identifierFilterGroupList = new StringFilterGroupList();

    /**
     * Called after an enabled filter has been matched.
     * Default implementation is to always filter the matched item.
     * Subclasses can perform additional or different checks if needed.
     * <p>
     * Method is called off the main thread.
     *
     * @param matchedList  The list the group filter belongs to.
     * @param matchedGroup The actual filter that matched.
     * @param matchedIndex Matched index of string/array.
     * @return True if the litho item should be filtered out.
     */
    @SuppressWarnings("rawtypes")
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (SettingsEnum.DEBUG.getBoolean()) {
            if (matchedList == identifierFilterGroupList) {
                LogHelper.printDebug(() -> getClass().getSimpleName() + " Filtered identifier: " + identifier);
            } else {
                LogHelper.printDebug(() -> getClass().getSimpleName() + " Filtered path: " + path);
            }
        }
        return true;
    }
}

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
            if (SettingsEnum.DEBUG_PROTOBUFFER.getBoolean()) {
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
            filterGroupLists(identifierSearchTree, filter, filter.identifierFilterGroupList);
            filterGroupLists(pathSearchTree, filter, filter.pathFilterGroupList);
        }

        LogHelper.printDebug(() -> "Using: "
                + identifierSearchTree.numberOfPatterns() + " identifier filters"
                + " (" + identifierSearchTree.getEstimatedMemorySize() + " KB), "
                + pathSearchTree.numberOfPatterns() + " path filters"
                + " (" + pathSearchTree.getEstimatedMemorySize() + " KB)");
    }

    private static <T> void filterGroupLists(TrieSearch<T> pathSearchTree,
                                             Filter filter, FilterGroupList<T, ? extends FilterGroup<T>> list) {
        for (FilterGroup<T> group : list) {
            if (!group.includeInSearch()) {
                continue;
            }
            for (T pattern : group.filters) {
                pathSearchTree.addPattern(pattern, (textSearched, matchedStartIndex, matchedLength, callbackParameter) -> {
                            if (!group.isEnabled()) return false;
                            LithoFilterParameters parameters = (LithoFilterParameters) callbackParameter;
                            return filter.isFiltered(parameters.identifier, parameters.path, parameters.protoBuffer,
                                    list, group, matchedStartIndex);
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
                LogHelper.printException(() -> "Proto buffer is null"); // Should never happen
                return false;
            }
            LithoFilterParameters parameter = new LithoFilterParameters(lithoIdentifier, pathBuilder, protobufBuffer);
            LogHelper.printDebug(() -> "Searching " + parameter);

            if (parameter.identifier != null) {
                if (identifierSearchTree.matches(parameter.identifier, parameter)) return true;
            }
            if (pathSearchTree.matches(parameter.path, parameter)) return true;
        } catch (Exception ex) {
            LogHelper.printException(() -> "Litho filter failure", ex);
        }

        return false;
    }
}