package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.sf;
import static app.revanced.extension.shared.Utils.dipToPixels;
import static app.revanced.extension.shared.settings.preference.SortedListPreference.setCheckedListView;
import static app.revanced.extension.shared.settings.preference.SortedListPreference.ViewHolder;

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
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.SortedListPreference;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

/**
 * A custom ListPreference that uses a styled custom dialog with a custom checkmark indicator.
 * Custom video speeds used by {@link CustomPlaybackSpeedPatch}.
 */
@SuppressWarnings({"unused", "deprecation"})
public final class CustomVideoSpeedListPreference extends ListPreference {

    /**
     * Initialize a settings preference list with the available playback speeds.
     */
    private void initializeEntryValues() {
        float[] customPlaybackSpeeds = CustomPlaybackSpeedPatch.customPlaybackSpeeds;
        final int numberOfEntries = customPlaybackSpeeds.length + 1;
        String[] preferenceListEntries = new String[numberOfEntries];
        String[] preferenceListEntryValues = new String[numberOfEntries];

        // Auto speed (same behavior as unpatched).
        preferenceListEntries[0] = sf("revanced_custom_playback_speeds_auto").toString();
        preferenceListEntryValues[0] = String.valueOf(Settings.PLAYBACK_SPEED_DEFAULT.defaultValue);

        int i = 1;
        for (float speed : customPlaybackSpeeds) {
            String speedString = String.valueOf(speed);
            preferenceListEntries[i] = speedString + "x";
            preferenceListEntryValues[i] = speedString;
            i++;
        }

        setEntries(preferenceListEntries);
        setEntryValues(preferenceListEntryValues);
    }

    {
        initializeEntryValues();
    }

    public CustomVideoSpeedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomVideoSpeedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomVideoSpeedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoSpeedListPreference(Context context) {
        super(context);
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

        // Measure content height before adding ListView to layout.
        // Otherwise, the ListView will push the buttons off the screen.
        int totalHeight = 0;
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(
                getContext().getResources().getDisplayMetrics().widthPixels,
                View.MeasureSpec.AT_MOST
        );
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(widthSpec, heightSpec);
            totalHeight += listItem.getMeasuredHeight();
        }

        // Cap the height at maxHeight.
        final int maxHeight = (int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.6);
        final int finalHeight = Math.min(totalHeight, maxHeight);

        // Add ListView to the main layout with calculated height.
        LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                finalHeight // Use calculated height directly.
        );
        listViewParams.setMargins(0, dipToPixels(8), 0, dipToPixels(8));
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
        private final int layoutResourceId;
        private String selectedValue;

        public CustomArrayAdapter(Context context, int resource, CharSequence[] entries,
                                  CharSequence[] entryValues, String selectedValue) {
            super(context, resource, entries);
            this.layoutResourceId = resource;
            this.entryValues = entryValues;
            this.selectedValue = selectedValue;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
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
            holder.itemText.setTextColor(Utils.getAppForegroundColor());

            // Show or hide checkmark and placeholder.
            String currentValue = entryValues[position].toString();
            final boolean isSelected = currentValue.equals(selectedValue);
            holder.checkIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            holder.checkIcon.setColorFilter(Utils.getAppForegroundColor());
            holder.placeholder.setVisibility(isSelected ? View.GONE : View.VISIBLE);

            return view;
        }

        public void setSelectedValue(String value) {
            this.selectedValue = value;
        }
    }
}
