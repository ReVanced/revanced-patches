package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.Utils.dipToPixels;
import static app.revanced.extension.shared.settings.preference.SortedListPreference.ListPreferenceArrayAdapter;
import static app.revanced.extension.shared.settings.preference.SortedListPreference.setCheckedListView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.LinearLayout;
import android.widget.ListView;

import app.revanced.extension.shared.Utils;

/**
 * A custom ListPreference that uses a styled custom dialog with a custom checkmark indicator.
 */
@SuppressWarnings({"unused", "deprecation"})
public class CustomDialogListPreference extends ListPreference {

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

        setCheckedListView(this, listView);

        // Create the custom dialog without OK button.
        Pair<Dialog, LinearLayout> dialogPair = Utils.createCustomDialog(
                getContext(),
                getTitle() != null ? getTitle().toString() : "", // Title.
                null, // No message.
                null, // No EditText.
                null, // No OK button text.
                null, // No OK button action.
                () -> {}, // Cancel button action (just dismiss).
                null, // No Neutral button text.
                null, // No Neutral button action.
                true // Dismiss dialog when onNeutralClick.
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
        listViewParams.height = Math.min(listViewParams.height, maxHeight);
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
