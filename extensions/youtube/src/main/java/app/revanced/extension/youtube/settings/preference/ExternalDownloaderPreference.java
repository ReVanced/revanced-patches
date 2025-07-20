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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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
        SEAL("Seal", "com.junkfood.seal", "https://github.com/JunkFood02/Seal/releases/latest"),
        TUBULAR("Tubular", "org.polymorphicshade.tubular", "https://github.com/polymorphicshade/Tubular/releases/latest"),
        LIBRETUBE("LibreTube", "com.github.libretube", "https://github.com/libre-tube/LibreTube/releases/latest"),
        NEW_PIPE("NewPipe", "org.schabi.newpipe", "https://github.com/TeamNewPipe/NewPipe/releases/latest"),
        CUSTOM(str("revanced_external_downloader_custom_item"), null, null);

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
            if (packageName == null) return null;
            for (Downloader downloader : values()) {
                if (packageName.equals(downloader.packageName)) {
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
            entryValues[i] = downloaders[i].packageName != null
                    ? downloaders[i].packageName
                    : Downloader.CUSTOM.name;
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
                Downloader.findByPackageName(packageName) != null
                        ? packageName
                        : Downloader.CUSTOM.name
        );
        listView.setAdapter(adapter);

        // Set checked item, default to Custom if packageName is not predefined.
        if (packageName != null) {
            CharSequence[] entryValues = getEntryValues();
            Downloader downloader = Downloader.findByPackageName(packageName);
            int selectedPosition = -1;
            if (downloader != null) {
                // Select predefined option if packageName matches.
                for (int i = 0; i < entryValues.length; i++) {
                    if (packageName.equals(entryValues[i].toString())) {
                        selectedPosition = i;
                        break;
                    }
                }
            } else {
                // Select Custom for non-predefined package names.
                for (int i = 0; i < entryValues.length; i++) {
                    if (entryValues[i].toString().equals(Downloader.CUSTOM.name)) {
                        selectedPosition = i;
                        break;
                    }
                }
            }
            if (selectedPosition >= 0) {
                listView.setItemChecked(selectedPosition, true);
                listView.setSelection(selectedPosition);
                adapter.setSelectedValue(entryValues[selectedPosition].toString());
                adapter.notifyDataSetChanged();
            }
        }

        // Handle item click to select value.
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedValue = getEntryValues()[position].toString();
            Downloader downloader = Downloader.findByPackageName(selectedValue);
            if (downloader != null && downloader != Downloader.CUSTOM) {
                editText.setText(downloader.packageName);
                editText.setEnabled(false); // Disable editing for predefined options.
            } else {
                editText.setText(""); // Clear text for Custom.
                editText.setHint(str("revanced_external_downloader_custom_item_hint")); // Set hint for Custom.
                editText.setEnabled(true); // Enable editing for Custom.
            }
            editText.setSelection(editText.getText().length());
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
        editText.setSingleLine(true); // Restrict EditText to a single line.
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI); // Prevent fullscreen keyboard mode.
        editText.setSelection(packageName != null ? packageName.length() : 0);
        // Set initial EditText state based on selected downloader.
        Downloader selectedDownloader = Downloader.findByPackageName(packageName);
        editText.setEnabled(selectedDownloader == null || selectedDownloader == Downloader.CUSTOM);
        if (selectedDownloader == null || selectedDownloader == Downloader.CUSTOM) {
            editText.setHint(str("revanced_external_downloader_custom_item_hint")); // Set hint for Custom if selected.
        }

        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String input = s.toString().trim();
                Downloader downloader = Downloader.findByPackageName(input);
                if (downloader == null || downloader == Downloader.CUSTOM) {
                    // Select Custom when input is not a predefined package.
                    for (int i = 0; i < getEntryValues().length; i++) {
                        if (getEntryValues()[i].toString().equals(Downloader.CUSTOM.name)) {
                            listView.setItemChecked(i, true);
                            listView.setSelection(i);
                            adapter.setSelectedValue(Downloader.CUSTOM.name);
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                } else {
                    // Select predefined option if input matches a downloader.
                    for (int i = 0; i < getEntryValues().length; i++) {
                        if (input.equals(getEntryValues()[i].toString())) {
                            listView.setItemChecked(i, true);
                            listView.setSelection(i);
                            adapter.setSelectedValue(input);
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
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
                    if (newValue.isEmpty()) {
                        // Show dialog if EditText is empty.
                        Utils.createCustomDialog(
                                context,
                                str("revanced_external_downloader_name_title"),
                                str("revanced_external_downloader_empty_warning"),
                                null,
                                null,
                                () -> {}, // OK button does nothing (dismiss only).
                                null,
                                null,
                                null,
                                false
                        ).first.show();
                        return;
                    }
                    checkPackageIsInstalled(newValue);
                },
                () -> {}, // Cancel button action (dismiss only).
                str("revanced_settings_reset"),
                () -> {
                    final String newValue = settings.defaultValue;
                    editText.setText(newValue);
                    editText.setSelection(newValue.length());
                    editText.setEnabled(false); // Disable editing on reset.
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
        dialogPair.first.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); // Adjust dialog size instead of fullscreen keyboard.

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

            int finalHeight = Math.min(totalHeight, Utils.percentageHeightToPixels(30)); // 30% of the screen height.
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
            String okButtonText = downloader != null && downloader.url != null
                    ? str("gms_core_dialog_open_website_text") // Open website.
                    : null; // Ok.
            // Show a dialog if the package is not installed, using the app name for predefined downloaders.
            String displayName = downloader != null && downloader != Downloader.CUSTOM
                    ? downloader.name
                    : newValue;
            String message = str("revanced_external_downloader_not_installed_warning", displayName);
            Utils.createCustomDialog(
                    context,
                    str("revanced_external_downloader_name_title"),
                    message,
                    null,
                    okButtonText,
                    () -> {
                        // OK button action: open the downloader's URL if available and save custom package name.
                        if (downloader != null && downloader.url != null) {
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
