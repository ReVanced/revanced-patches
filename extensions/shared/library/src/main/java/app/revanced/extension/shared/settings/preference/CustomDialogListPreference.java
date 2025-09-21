package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;

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
import app.revanced.extension.shared.ui.CustomDialog;

/**
 * A custom ListPreference that uses a styled custom dialog with a custom checkmark indicator,
 * supports a static summary and highlighted entries for search functionality.
 */
@SuppressWarnings({"unused", "deprecation"})
public class CustomDialogListPreference extends ListPreference {

    public static final int ID_REVANCED_CHECK_ICON =
            getResourceIdentifierOrThrow("revanced_check_icon", "id");
    public static final int ID_REVANCED_CHECK_ICON_PLACEHOLDER =
            getResourceIdentifierOrThrow("revanced_check_icon_placeholder", "id");
    public static final int ID_REVANCED_ITEM_TEXT =
            getResourceIdentifierOrThrow("revanced_item_text", "id");
    public static final int LAYOUT_REVANCED_CUSTOM_LIST_ITEM_CHECKED =
            getResourceIdentifierOrThrow("revanced_custom_list_item_checked", "layout");

    private String staticSummary = null;
    private CharSequence[] highlightedEntriesForDialog = null;

    /**
     * Set a static summary that will not be overwritten by value changes.
     */
    public void setStaticSummary(String summary) {
        this.staticSummary = summary;
    }

    /**
     * Returns the static summary if set, otherwise null.
     */
    @Nullable
    public String getStaticSummary() {
        return staticSummary;
    }

    /**
     * Always return static summary if set.
     */
    @Override
    public CharSequence getSummary() {
        if (staticSummary != null) {
            return staticSummary;
        }
        return super.getSummary();
    }

    /**
     * Sets highlighted entries for display in the dialog.
     * These entries are used only for the current dialog and are automatically cleared.
     */
    public void setHighlightedEntriesForDialog(CharSequence[] highlightedEntries) {
        this.highlightedEntriesForDialog = highlightedEntries;
    }

    /**
     * Clears highlighted entries after the dialog is closed.
     */
    public void clearHighlightedEntriesForDialog() {
        this.highlightedEntriesForDialog = null;
    }

    /**
     * Returns entries for display in the dialog.
     * If highlighted entries exist, they are used; otherwise, the original entries are returned.
     */
    private CharSequence[] getEntriesForDialog() {
        return highlightedEntriesForDialog != null ? highlightedEntriesForDialog : getEntries();
    }

    /**
     * Custom ArrayAdapter to handle checkmark visibility.
     */
    public static class ListPreferenceArrayAdapter extends ArrayAdapter<CharSequence> {
        private static class SubViewDataContainer {
            ImageView checkIcon;
            View placeholder;
            TextView itemText;
        }

        final int layoutResourceId;
        final CharSequence[] entryValues;
        String selectedValue;

        public ListPreferenceArrayAdapter(Context context, int resource,
                                          CharSequence[] entries,
                                          CharSequence[] entryValues,
                                          String selectedValue) {
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
                holder.checkIcon = view.findViewById(ID_REVANCED_CHECK_ICON);
                holder.placeholder = view.findViewById(ID_REVANCED_CHECK_ICON_PLACEHOLDER);
                holder.itemText = view.findViewById(ID_REVANCED_ITEM_TEXT);
                view.setTag(holder);
            } else {
                holder = (SubViewDataContainer) view.getTag();
            }

            CharSequence itemText = getItem(position);
            holder.itemText.setText(itemText);
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
        Context context = getContext();

        CharSequence[] entriesToShow = getEntriesForDialog();
        CharSequence[] entryValues = getEntryValues();

        // Create ListView.
        ListView listView = new ListView(context);
        listView.setId(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Create custom adapter for the ListView.
        ListPreferenceArrayAdapter adapter = new ListPreferenceArrayAdapter(
                context,
                LAYOUT_REVANCED_CUSTOM_LIST_ITEM_CHECKED,
                entriesToShow,
                entryValues,
                getValue()
        );
        listView.setAdapter(adapter);

        // Set checked item.
        String currentValue = getValue();
        if (currentValue != null) {
            for (int i = 0, length = entryValues.length; i < length; i++) {
                if (currentValue.equals(entryValues[i].toString())) {
                    listView.setItemChecked(i, true);
                    listView.setSelection(i);
                    break;
                }
            }
        }

        // Create the custom dialog without OK button.
        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                context,
                getTitle() != null ? getTitle().toString() : "",
                null,
                null,
                null,
                null,
                this::clearHighlightedEntriesForDialog, // Cancel button action.
                null,
                null,
                true
        );

        Dialog dialog = dialogPair.first;
        // Add a listener to clear when the dialog is closed in any way.
        dialog.setOnDismissListener(dialogInterface -> clearHighlightedEntriesForDialog());

        // Add the ListView to the main layout.
        LinearLayout mainLayout = dialogPair.second;
        LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        mainLayout.addView(listView, mainLayout.getChildCount() - 1, listViewParams);

        // Handle item click to select value and dismiss dialog.
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedValue = entryValues[position].toString();
            if (callChangeListener(selectedValue)) {
                setValue(selectedValue);

                // Update summaries from the original entries (without highlighting).
                if (staticSummary == null) {
                    CharSequence[] originalEntries = getEntries();
                    if (originalEntries != null && position < originalEntries.length) {
                        setSummary(originalEntries[position]);
                    }
                }

                adapter.setSelectedValue(selectedValue);
                adapter.notifyDataSetChanged();
            }

            // Clear highlighted entries before closing.
            clearHighlightedEntriesForDialog();
            dialog.dismiss();
        });

        // Show the dialog.
        dialog.show();
    }
}
