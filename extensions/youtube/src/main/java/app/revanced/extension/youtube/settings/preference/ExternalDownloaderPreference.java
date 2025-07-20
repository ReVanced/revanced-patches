package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.CustomDialogListPreference;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.youtube.settings.Settings;

/**
 * A custom ListPreference for selecting an external downloader package with checkmarks and EditText for custom package names.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ExternalDownloaderPreference extends CustomDialogListPreference {

    private static final StringSetting settings = Settings.EXTERNAL_DOWNLOADER_PACKAGE_NAME;
    private EditText editText;
    private CustomDialogListPreference.ListPreferenceArrayAdapter adapter;

    /**
     * Enum representing supported external downloaders with their display names, package names, and download URLs.
     */
    public enum Downloader {
        YTDLNIS("YTDLnis", "com.deniscerri.ytdl", "https://github.com/deniscerri/ytdlnis/releases/latest"),
        NEW_PIPE("NewPipe", "org.schabi.newpipe", "https://github.com/TeamNewPipe/NewPipe/releases/latest"),
        SEAL("Seal", "com.junkfood.seal", "https://github.com/JunkFood02/Seal/releases/latest"),
        TUBULAR("Tubular", "org.polymorphicshade.tubular", "https://github.com/polymorphicshade/Tubular/releases/latest");

        public final String name;
        public final String packageName;
        public final String url;

        Downloader(String name, String packageName, String url) {
            this.name = name;
            this.packageName = packageName;
            this.url = url;
        }

        /**
         * Finds a Downloader by its package name.
         * @return The Downloader enum or null if not found.
         */
        public static Downloader findByPackageName(String packageName) {
            for (Downloader downloader : values()) {
                if (downloader.packageName.equals(packageName)) {
                    return downloader;
                }
            }
            return null;
        }
    }

    {
        // Instance initialization block.
        Downloader[] downloaders = Downloader.values();
        CharSequence[] entries = new CharSequence[downloaders.length];
        CharSequence[] entryValues = new CharSequence[downloaders.length];
        for (int i = 0; i < downloaders.length; i++) {
            entries[i] = downloaders[i].name;
            entryValues[i] = downloaders[i].packageName;
        }
        setEntries(entries);
        setEntryValues(entryValues);
    }

    public ExternalDownloaderPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ExternalDownloaderPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExternalDownloaderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExternalDownloaderPreference(Context context) {
        super(context);
    }

    /**
     * Sets the summary for this ListPreference.
     */
    @Override
    public void setSummary(CharSequence summary) {
        // Ignore calls to set the summary.
        // Summary is always the description of the category.
        //
        // This is required otherwise the ReVanced preference fragment
        // sets all ListPreference summaries to show the current selection.
    }

    /**
     * Shows a custom dialog with a ListView for predefined downloader packages and EditText for custom package input.
     */
    @Override
    protected void showDialog(@Nullable Bundle state) {
        Context context = getContext();
        String packageName = settings.get();

        // Create the main layout for the dialog content.
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        // Create ListView for predefined downloaders.
        ListView listView = new ListView(context);
        listView.setId(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Create custom adapter for the ListView.
        adapter = new CustomDialogListPreference.ListPreferenceArrayAdapter(
                context,
                Utils.getResourceIdentifier("revanced_custom_list_item_checked", "layout"),
                getEntries(),
                getEntryValues(),
                packageName
        );
        listView.setAdapter(adapter);

        // Set checked item.
        if (packageName != null) {
            CharSequence[] entryValues = getEntryValues();
            for (int i = 0; i < entryValues.length; i++) {
                if (packageName.equals(entryValues[i].toString())) {
                    listView.setItemChecked(i, true);
                    listView.setSelection(i);
                    break;
                }
            }
        }

        // Handle item click to select value.
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedValue = getEntryValues()[position].toString();
            editText.setText(selectedValue);
            editText.setSelection(selectedValue.length());
            adapter.setSelectedValue(selectedValue);
            adapter.notifyDataSetChanged();
        });

        // Add ListView to content layout with initial height.
        LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0 // Initial height, will be updated.
        );
        listViewParams.bottomMargin = dipToPixels(16);
        contentLayout.addView(listView, listViewParams);

        // Add EditText for custom package name.
        editText = new EditText(context);
        editText.setHint(settings.defaultValue);
        editText.setText(packageName);
        editText.setSelection(packageName.length());

        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String input = s.toString().trim();
                if (Downloader.findByPackageName(input) == null) {
                    listView.clearChoices();
                    adapter.setSelectedValue(null);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        ShapeDrawable editTextBackground = new ShapeDrawable(new RoundRectShape(
                Utils.createCornerRadii(10), null, null));
        editTextBackground.getPaint().setColor(Utils.getEditTextBackground());
        final int dip8 = dipToPixels(8);
        editText.setPadding(dip8, dip8, dip8, dip8);
        editText.setBackground(editTextBackground);
        editText.setClipToOutline(true);
        contentLayout.addView(editText);

        // Create the custom dialog.
        Pair<Dialog, LinearLayout> dialogPair = Utils.createCustomDialog(
                context,
                getTitle() != null ? getTitle().toString() : "",
                null,
                null,
                null,
                () -> {
                    String newValue = editText.getText().toString().trim();
                    checkPackageIsInstalled(newValue);
                },
                () -> {}, // Cancel button action (dismiss only).
                str("revanced_settings_reset"),
                () -> {
                    final String newValue = settings.defaultValue;
                    editText.setText(newValue);
                    editText.setSelection(newValue.length());
                    listView.clearChoices();
                    adapter.setSelectedValue(newValue);
                    adapter.notifyDataSetChanged();
                    Downloader downloader = Downloader.findByPackageName(newValue);
                    if (downloader != null) {
                        CharSequence[] entryValues = getEntryValues();
                        for (int i = 0; i < entryValues.length; i++) {
                            if (newValue.equals(entryValues[i].toString())) {
                                listView.setItemChecked(i, true);
                                listView.setSelection(i);
                                break;
                            }
                        }
                    }
                },
                false
        );

        // Add the content layout directly to the dialog's main layout.
        LinearLayout dialogMainLayout = dialogPair.second;
        dialogMainLayout.addView(contentLayout, dialogMainLayout.getChildCount() - 1);

        // Update ListView height dynamically.
        Runnable updateListViewHeight = () -> {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int totalHeight = 0;
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter != null) {
                int listAdapterCount = listAdapter.getCount();
                for (int i = 0; i < listAdapterCount; i++) {
                    View item = listAdapter.getView(i, null, listView);
                    item.measure(
                            View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST),
                            View.MeasureSpec.UNSPECIFIED
                    );
                    totalHeight += item.getMeasuredHeight();
                }
                totalHeight += listView.getDividerHeight() * (listAdapterCount - 1);
            }

            int finalHeight = Math.min(totalHeight, Utils.percentageHeightToPixels(0.3f)); // 30% of the screen height.
            listViewParams.height = finalHeight;
            listView.setLayoutParams(listViewParams);
        };

        // Initial height calculation.
        updateListViewHeight.run();

        // Listen for configuration changes (e.g., orientation).
        View dialogView = dialogPair.second;
        dialogView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // Recalculate height when layout changes (e.g., orientation change).
            updateListViewHeight.run();
        });

        // Show the dialog.
        dialogPair.first.show();
    }

    /**
     * Checks if the package is installed, shows a dialog if not installed for predefined packages.
     */
    private void checkPackageIsInstalled(String newValue) {
        Context context = getContext();
        // Check if the package is installed and enabled.
        boolean packageEnabled = false;
        try {
            packageEnabled = context.getPackageManager().getApplicationInfo(newValue, 0).enabled;
        } catch (PackageManager.NameNotFoundException error) {
            Logger.printDebug(() -> "External downloader could not be found: " + error);
        }
        if (!packageEnabled) {
            // Find the corresponding downloader for the package, if it exists.
            Downloader downloader = Downloader.findByPackageName(newValue);
            // Set OK button text.
            String okButtonText = downloader != null ? str("gms_core_dialog_open_website_text") : null;
            // Show a dialog if the package is not installed.
            String message = str("revanced_external_downloader_not_installed_warning", newValue);
            Utils.createCustomDialog(
                    context,
                    str("revanced_external_downloader_name_title"),
                    message,
                    null,
                    okButtonText,
                    () -> {
                        // OK button action: open the downloader's URL if available and save custom package name.
                        if (downloader != null) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloader.url));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            } catch (Exception ex) {
                                Logger.printException(() -> "Failed to open downloader URL: " + downloader.url, ex);
                            }
                        } else {
                            // Save custom package name if not installed.
                            if (callChangeListener(newValue)) {
                                setValue(newValue);
                            }
                        }
                    },
                    () -> {}, // Cancel button action (dismiss only).
                    null,
                    null,
                    false
            ).first.show();
            return;
        }
        // Proceed with setting the package name if it is installed.
        if (callChangeListener(newValue)) {
            setValue(newValue);
        }
    }
}
