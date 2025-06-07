package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.Utils.dipToPixels;

import android.app.Dialog;
import android.content.Context;
import android.preference.ListPreference;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        // Android does not have a triple class like Kotlin, So instead use a nested pair.
        // Cannot easily use a SortedMap, because if two entries incorrectly have
        // identical names then the duplicates entries are not preserved.
        List<Pair<String, Pair<CharSequence, CharSequence>>> lastEntries = new ArrayList<>();

        for (int i = 0; i < entrySize; i++) {
            Pair<CharSequence, CharSequence> pair = new Pair<>(entries[i], entryValues[i]);
            if (i < firstEntriesToPreserve) {
                firstEntries.add(pair);
            } else {
                lastEntries.add(new Pair<>(Utils.removePunctuationToLowercase(pair.first), pair));
            }
        }

        //noinspection ComparatorCombinators
        Collections.sort(lastEntries, (pair1, pair2)
                -> pair1.first.compareTo(pair2.first));

        CharSequence[] sortedEntries = new CharSequence[entrySize];
        CharSequence[] sortedEntryValues = new CharSequence[entrySize];

        int i = 0;
        for (Pair<CharSequence, CharSequence> pair : firstEntries) {
            sortedEntries[i] = pair.first;
            sortedEntryValues[i] = pair.second;
            i++;
        }

        for (Pair<String, Pair<CharSequence, CharSequence>> outer : lastEntries) {
            Pair<CharSequence, CharSequence> inner = outer.second;
            sortedEntries[i] = inner.first;
            sortedEntryValues[i] = inner.second;
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

    @Override
    protected void showDialog(Bundle state) {
        // Create ListView.
        ListView listView = new ListView(getContext());
        listView.setId(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Create custom adapter for the ListView.
        CustomArrayAdapter adapter = new CustomArrayAdapter(
                getContext(),
                Utils.getResourceIdentifier("revanced_custom_list_item_checked", "layout"),
                getEntries(),
                getEntryValues(),
                getValue()
        );
        listView.setAdapter(adapter);

        // Set the currently selected value.
        String currentValue = getValue();
        if (currentValue != null) {
            CharSequence[] entryValues = getEntryValues();
            for (int i = 0; i < entryValues.length; i++) {
                if (currentValue.equals(entryValues[i])) {
                    listView.setItemChecked(i, true);
                    listView.setSelection(i);
                    break;
                }
            }
        }

        // Create the custom dialog without OK button.
        Pair<Dialog, LinearLayout> dialogPair = Utils.createCustomDialog(
                getContext(),
                getTitle() != null ? getTitle().toString() : "",
                null,
                null,
                null, // No OK button text.
                null, // No OK button action.
                () -> {}, // Cancel button action (just dismiss).
                null,
                null,
                true
        );

        Dialog dialog = dialogPair.first;
        LinearLayout mainLayout = dialogPair.second;

        // Add ListView to the main layout.
        LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        listViewParams.setMargins(0, dipToPixels(8), 0, dipToPixels(8));
        int maxHeight = (int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.6);
        listViewParams.height = maxHeight;
        mainLayout.addView(listView, mainLayout.getChildCount() - 1, listViewParams);

        // Handle item click to select value and dismiss dialog.
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedValue = getEntryValues()[position].toString();
            if (callChangeListener(selectedValue)) {
                setValue(selectedValue);
                adapter.setSelectedValue(selectedValue);
                adapter.notifyDataSetChanged();
            }
            dialog.dismiss();
        });

        // Show the dialog.
        dialog.show();
    }

    /**
     * Custom ArrayAdapter to handle checkmark visibility.
     */
    private static class CustomArrayAdapter extends ArrayAdapter<CharSequence> {
        private final CharSequence[] entryValues;
        private String selectedValue;
        private final int layoutResourceId;

        public CustomArrayAdapter(Context context, int resource, CharSequence[] entries,
                                  CharSequence[] entryValues, String selectedValue) {
            super(context, resource, entries);
            this.layoutResourceId = resource;
            this.entryValues = entryValues;
            this.selectedValue = selectedValue;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(layoutResourceId, parent, false);
                holder = new ViewHolder();
                holder.checkIcon = view.findViewById(Utils.getResourceIdentifier("revanced_check_icon", "id"));
                holder.placeholder = view.findViewById(Utils.getResourceIdentifier("revanced_check_icon_placeholder", "id"));
                holder.itemText = view.findViewById(Utils.getResourceIdentifier("revanced_item_text", "id"));
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            // Set text.
            holder.itemText.setText(getItem(position));

            // Show or hide checkmark and placeholder.
            String currentValue = entryValues[position].toString();
            boolean isSelected = currentValue.equals(selectedValue);
            holder.checkIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            holder.placeholder.setVisibility(isSelected ? View.GONE : View.VISIBLE);

            return view;
        }

        public void setSelectedValue(String value) {
            this.selectedValue = value;
        }

        private static class ViewHolder {
            ImageView checkIcon;
            View placeholder;
            TextView itemText;
        }
    }
}
