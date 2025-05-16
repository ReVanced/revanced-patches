package app.revanced.extension.shared.settings.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PreferenceList that sorts itself.
 * By default the first entry is preserved in its original position,
 * and all other entries are sorted alphabetically.
 *
 * Ideally the 'keep first entries to preserve' is an xml parameter,
 * but currently that's not so simple since Extensions code cannot use
 * generated code from the Patches repo (which is required for custom xml parameters).
 *
 * If any class wants to use a different getFirstEntriesToPreserve value,
 * it needs to subclass this preference and override {@link #getFirstEntriesToPreserve}.
 */
@SuppressWarnings({"unused", "deprecation"})
public class SortedListPreference extends ListPreference {

    /**
     * Sorts the list entries, but preserves the first N entries in their current position.
     */
    protected void sortEntryAndValues() {
        final int firstEntriesToPreserve = getFirstEntriesToPreserve();

        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        final int entrySize = entries.length;

        if (entrySize != entryValues.length) {
            // Xml array declaration has a missing/extra entry.
            throw new IllegalStateException();
        }

        List<Pair<CharSequence, CharSequence>> firstPairs = new ArrayList<>(firstEntriesToPreserve);
        List<Pair<CharSequence, CharSequence>> pairsToSort = new ArrayList<>(entrySize);

        for (int i = 0; i < entrySize; i++) {
            Pair<CharSequence, CharSequence> pair = new Pair<>(entries[i], entryValues[i]);
            if (i < firstEntriesToPreserve) {
                firstPairs.add(pair);
            } else {
                pairsToSort.add(pair);
            }
        }

        Collections.sort(pairsToSort, (pair1, pair2)
                -> pair1.first.toString().compareToIgnoreCase(pair2.first.toString()));

        CharSequence[] sortedEntries = new CharSequence[entrySize];
        CharSequence[] sortedEntryValues = new CharSequence[entrySize];

        int i = 0;
        for (Pair<CharSequence, CharSequence> pair : firstPairs) {
            sortedEntries[i] = pair.first;
            sortedEntryValues[i] = pair.second;
            i++;
        }

        for (Pair<CharSequence, CharSequence> pair : pairsToSort) {
            sortedEntries[i] = pair.first;
            sortedEntryValues[i] = pair.second;
            i++;
        }

        super.setEntries(sortedEntries);
        super.setEntryValues(sortedEntryValues);
    }

    protected int getFirstEntriesToPreserve() {
        return 1;
    }

    /**
     * If changing both entry and values and the number of entries/values
     * differs from what is currently set, then first set the values by calling
     * {@link #setEntryValues(CharSequence[])} before calling this method.
     */
    @Override
    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);

        sortEntryAndValues();
    }

    public SortedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        sortEntryAndValues();
    }

    public SortedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        sortEntryAndValues();
    }

    public SortedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        sortEntryAndValues();
    }

    public SortedListPreference(Context context) {
        super(context);

        sortEntryAndValues();
    }
}
