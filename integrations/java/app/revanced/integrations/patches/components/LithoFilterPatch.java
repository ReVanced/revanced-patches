package app.revanced.integrations.patches.components;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

abstract class FilterGroup<T> {
    final static class FilterGroupResult {
        private final boolean filtered;
        private final SettingsEnum setting;

        public FilterGroupResult(final SettingsEnum setting, final boolean filtered) {
            this.setting = setting;
            this.filtered = filtered;
        }

        public SettingsEnum getSetting() {
            return setting;
        }

        public boolean isFiltered() {
            return filtered;
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
    }

    public boolean isEnabled() {
        return setting.getBoolean();
    }

    public abstract FilterGroupResult check(final T stack);
}

class StringFilterGroup extends FilterGroup<String> {

    /**
     * {@link FilterGroup#FilterGroup(SettingsEnum, Object[])}
     */
    public StringFilterGroup(final SettingsEnum setting, final String... filters) {
        super(setting, filters);
    }

    @Override
    public FilterGroupResult check(final String string) {
        return new FilterGroupResult(setting, string != null && ReVancedUtils.containsAny(string, filters));
    }
}

final class CustomFilterGroup extends StringFilterGroup {

    /**
     * {@link FilterGroup#FilterGroup(SettingsEnum, Object[])}
     */
    public CustomFilterGroup(final SettingsEnum setting, final SettingsEnum filter) {
        super(setting, filter.getString().split(","));
    }
}

class ByteArrayFilterGroup extends FilterGroup<byte[]> {
    // Modified implementation from https://stackoverflow.com/a/1507813
    private int indexOf(final byte[] data, final byte[] pattern) {
        if (data.length == 0)
            return -1;
        // Computes the failure function using a boot-strapping process,
        // where the pattern is matched against itself.
        final int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        // Finds the first occurrence of the pattern in the byte array using
        // KMP matching algorithm.

        j = 0;

        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * {@link FilterGroup#FilterGroup(SettingsEnum, Object[])}
     */
    public ByteArrayFilterGroup(final SettingsEnum setting, final byte[]... filters) {
        super(setting, filters);
    }

    @Override
    public FilterGroupResult check(final byte[] bytes) {
        var matched = false;
        for (byte[] filter : filters) {
            if (indexOf(bytes, filter) == -1)
                continue;

            matched = true;
            break;
        }

        final var filtered = matched;
        return new FilterGroupResult(setting, filtered);
    }
}

final class ByteArrayAsStringFilterGroup extends ByteArrayFilterGroup {

    /**
     * {@link ByteArrayFilterGroup#ByteArrayFilterGroup(SettingsEnum, byte[]...)}
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public ByteArrayAsStringFilterGroup(SettingsEnum setting, String... filters) {
        super(setting, Arrays.stream(filters).map(String::getBytes).toArray(byte[][]::new));
    }
}

abstract class FilterGroupList<V, T extends FilterGroup<V>> implements Iterable<T> {
    private final ArrayList<T> filterGroups = new ArrayList<>();

    @SafeVarargs
    protected final void addAll(final T... filterGroups) {
        this.filterGroups.addAll(Arrays.asList(filterGroups));
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

    protected boolean contains(final V stack) {
        for (T filterGroup : this) {
            if (!filterGroup.isEnabled())
                continue;

            var result = filterGroup.check(stack);
            if (result.isFiltered()) {
                return true;
            }
        }

        return false;
    }
}

final class StringFilterGroupList extends FilterGroupList<String, StringFilterGroup> {
}

final class ByteArrayFilterGroupList extends FilterGroupList<byte[], ByteArrayFilterGroup> {
}

abstract class Filter {
    final protected StringFilterGroupList pathFilterGroups = new StringFilterGroupList();
    final protected StringFilterGroupList identifierFilterGroups = new StringFilterGroupList();
    final protected ByteArrayFilterGroupList protobufBufferFilterGroups = new ByteArrayFilterGroupList();

    /**
     * Check if the given path, identifier or protobuf buffer is filtered by any
     * {@link FilterGroup}.
     *
     * @return True if filtered, false otherwise.
     */
    boolean isFiltered(final String path, final String identifier, final byte[] protobufBufferArray) {
        if (pathFilterGroups.contains(path)) {
            LogHelper.printDebug(() -> String.format("Filtered path: %s", path));
            return true;
        }

        if (identifierFilterGroups.contains(identifier)) {
            LogHelper.printDebug(() -> String.format("Filtered identifier: %s", identifier));
            return true;
        }

        if (protobufBufferFilterGroups.contains(protobufBufferArray)) {
            LogHelper.printDebug(() -> "Filtered from protobuf-buffer");
            return true;
        }

        return false;
    }
}

@RequiresApi(api = Build.VERSION_CODES.N)
@SuppressWarnings("unused")
public final class LithoFilterPatch {
    private static final Filter[] filters = new Filter[] {
            new DummyFilter() // Replaced by patch.
    };

    @SuppressWarnings("unused")
    public static boolean filter(final StringBuilder pathBuilder, final String identifier,
            final ByteBuffer protobufBuffer) {
        // TODO: Maybe this can be moved to the Filter class, to prevent unnecessary
        // string creation
        // because some filters might not need the path.
        var path = pathBuilder.toString();

        // It is assumed that protobufBuffer is empty as well in this case.
        if (path.isEmpty())
            return false;

        LogHelper.printDebug(() -> String.format(
                "Searching (ID: %s, Buffer-size: %s): %s",
                identifier, protobufBuffer.remaining(), path));

        var protobufBufferArray = protobufBuffer.array();

        for (var filter : filters) {
            var filtered = filter.isFiltered(path, identifier, protobufBufferArray);

            LogHelper.printDebug(
                    () -> String.format("%s (ID: %s): %s", filtered ? "Filtered" : "Unfiltered", identifier, path));

            if (filtered)
                return true;
        }

        return false;
    }
}