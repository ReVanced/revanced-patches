package app.revanced.extension.shared.settings.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import app.revanced.extension.shared.Utils;

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
     * Sorts the current list entries.
     *
     * @param firstEntriesToPreserve The number of entries to preserve in their original position.
     */
    public void sortEntryAndValues(int firstEntriesToPreserve) {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        if (entries == null || entryValues == null) {
            return;
        }

        final int entrySize = entries.length;
        if (entrySize != entryValues.length) {
            // Xml array declaration has a missing/extra entry.
            throw new IllegalStateException();
        }

        List<Pair<CharSequence, CharSequence>> firstEntries = new ArrayList<>(firstEntriesToPreserve);
        SortedMap<String, Pair<CharSequence, CharSequence>> lastEntries = new TreeMap<>();

        for (int i = 0; i < entrySize; i++) {
            Pair<CharSequence, CharSequence> pair = new Pair<>(entries[i], entryValues[i]);
            if (i < firstEntriesToPreserve) {
                firstEntries.add(pair);
            } else {
                lastEntries.put(Utils.removePunctuationToLowercase(pair.first), pair);
            }
        }

        CharSequence[] sortedEntries = new CharSequence[entrySize];
        CharSequence[] sortedEntryValues = new CharSequence[entrySize];

        int i = 0;
        for (Pair<CharSequence, CharSequence> pair : firstEntries) {
            sortedEntries[i] = pair.first;
            sortedEntryValues[i] = pair.second;
            i++;
        }

        for (Pair<CharSequence, CharSequence> pair : lastEntries.values()) {
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

    public SortedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        sortEntryAndValues(getFirstEntriesToPreserve());
    }

    public SortedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        sortEntryAndValues(getFirstEntriesToPreserve());
    }

    public SortedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        sortEntryAndValues(getFirstEntriesToPreserve());
    }

    public SortedListPreference(Context context) {
        super(context);

        sortEntryAndValues(getFirstEntriesToPreserve());
    }
}
