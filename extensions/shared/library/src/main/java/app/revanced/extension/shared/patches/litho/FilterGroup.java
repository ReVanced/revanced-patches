package app.revanced.extension.shared.patches.litho;

import androidx.annotation.NonNull;
import app.revanced.extension.shared.ByteTrieSearch;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BooleanSetting;

public abstract class FilterGroup<T> {
    public final static class FilterGroupResult {
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean includeInSearch() {
        return isEnabled() || !setting.rebootApp;
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + (setting == null ? "(null setting)" : setting);
    }

    public abstract FilterGroupResult check(final T stack);


    public static class StringFilterGroup extends FilterGroup<String> {

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
                        final int indexOf = string.indexOf(pattern);
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

    /**
     * If you have more than 1 filter patterns, then all instances of
     * this class should filtered using {@link FilterGroupList.ByteArrayFilterGroupList#check(byte[])},
     * which uses a prefix tree to give better performance.
     */
    public static class ByteArrayFilterGroup extends FilterGroup<byte[]> {

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
        public ByteArrayFilterGroup(BooleanSetting setting, String... filters) {
            super(setting, ByteTrieSearch.convertStringsToBytes(filters));
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
}
