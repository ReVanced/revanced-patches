package app.revanced.extension.shared.patches.litho;

import androidx.annotation.NonNull;

import java.util.*;

import app.revanced.extension.shared.ByteTrieSearch;
import app.revanced.extension.shared.StringTrieSearch;
import app.revanced.extension.shared.TrieSearch;
import app.revanced.extension.shared.patches.litho.FilterGroup.ByteArrayFilterGroup;
import app.revanced.extension.shared.patches.litho.FilterGroup.StringFilterGroup;

public abstract class FilterGroupList<V, T extends FilterGroup<V>> implements Iterable<T> {

    private final List<T> filterGroups = new ArrayList<>();
    private final TrieSearch<V> search = createSearchGraph();

    @SafeVarargs
    public final void addAll(final T... groups) {
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

    public FilterGroup.FilterGroupResult check(V stack) {
        FilterGroup.FilterGroupResult result = new FilterGroup.FilterGroupResult();
        search.matches(stack, result);
        return result;

    }

    protected abstract TrieSearch<V> createSearchGraph();

    public static final class StringFilterGroupList extends FilterGroupList<String, StringFilterGroup> {
        protected StringTrieSearch createSearchGraph() {
            return new StringTrieSearch();
        }
    }

    /**
     * If searching for a single byte pattern, then it is slightly better to use
     * {@link ByteArrayFilterGroup#check(byte[])} as it uses KMP which is faster
     * than a prefix tree to search for only 1 pattern.
     */
    public static final class ByteArrayFilterGroupList extends FilterGroupList<byte[], ByteArrayFilterGroup> {
        protected ByteTrieSearch createSearchGraph() {
            return new ByteTrieSearch();
        }
    }
}