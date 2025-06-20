package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.Utils.dipToPixels;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
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

import androidx.annotation.NonNull;

import app.revanced.extension.shared.Utils;

/**
 * A custom ListPreference that uses a styled custom dialog with a custom checkmark indicator.
 */
@SuppressWarnings({"unused", "deprecation"})
public class CustomDialogListPreference extends ListPreference {

    /**
     * Custom ArrayAdapter to handle checkmark visibility.
     */
    private static class ListPreferenceArrayAdapter extends ArrayAdapter<CharSequence> {
        private static class SubViewDataContainer {
            ImageView checkIcon;
            View placeholder;
            TextView itemText;
        }

        final int layoutResourceId;
        final CharSequence[] entryValues;
        String selectedValue;

        public ListPreferenceArrayAdapter(Context context, int resource, CharSequence[] entries,
                                          CharSequence[] entryValues, String selectedValue) {
            super(context, resource, entries);
            this.layoutResourceId = resource;
            this.entryValues = entryValues;
            this.selectedValue = selectedValue;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            SubViewDataContainer holder;

            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(layoutResourceId, parent, false);
                holder = new SubViewDataContainer();
                holder.checkIcon = view.findViewById(Utils.getResourceIdentifier(
                        "revanced_check_icon", "id"));
                holder.placeholder = view.findViewById(Utils.getResourceIdentifier(
                        "revanced_check_icon_placeholder", "id"));
                holder.itemText = view.findViewById(Utils.getResourceIdentifier(
                        "revanced_item_text", "id"));
                view.setTag(holder);
            } else {
                holder = (SubViewDataContainer) view.getTag();
            }

            // Set text.
            holder.itemText.setText(getItem(position));
            holder.itemText.setTextColor(Utils.getAppForegroundColor());

            // Show or hide checkmark and placeholder.
            String currentValue = entryValues[position].toString();
            boolean isSelected = currentValue.equals(selectedValue);
            holder.checkIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            holder.checkIcon.setColorFilter(Utils.getAppForegroundColor());
            holder.placeholder.setVisibility(isSelected ? View.GONE : View.VISIBLE);

            return view;
        }

        public void setSelectedValue(String value) {
            this.selectedValue = value;
        }
    }

    public CustomDialogListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomDialogListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomDialogListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDialogListPreference(Context context) {
        super(context);
    }

    @Override
    protected void showDialog(Bundle state) {
        // Create ListView.
        ListView listView = new ListView(getContext());
        listView.setId(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Create custom adapter for the ListView.
        ListPreferenceArrayAdapter adapter = new ListPreferenceArrayAdapter(
                getContext(),
                Utils.getResourceIdentifier("revanced_custom_list_item_checked", "layout"),
                getEntries(),
                getEntryValues(),
                getValue()
        );
        listView.setAdapter(adapter);

        // Set checked item.
        String currentValue = getValue();
        if (currentValue != null) {
            CharSequence[] entryValues = getEntryValues();
            for (int i = 0, length = entryValues.length; i < length; i++) {
                if (currentValue.equals(entryValues[i].toString())) {
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

        // Measure content height before adding ListView to layout.
        // Otherwise, the ListView will push the buttons off the screen.
        int totalHeight = 0;
        int widthSpec = View.MeasureSpec.makeMeasureSpec(
                getContext().getResources().getDisplayMetrics().widthPixels,
                View.MeasureSpec.AT_MOST
        );
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(widthSpec, heightSpec);
            totalHeight += listItem.getMeasuredHeight();
        }

        // Cap the height at maxHeight.
        int maxHeight = (int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.6);
        int finalHeight = Math.min(totalHeight, maxHeight);

        // Add ListView to the main layout with calculated height.
        LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                finalHeight // Use calculated height directly.
        );
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
}
