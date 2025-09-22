package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.sf;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.CustomDialogListPreference;
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.youtube.settings.Settings;

/**
 * A custom ListPreference for selecting an external downloader package with checkmarks and EditText for custom package names.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ExternalDownloaderPreference extends CustomDialogListPreference {

    /**
     * Enum representing supported external downloaders with their display names, package names, and download URLs.
     */
    private enum Downloader {
        YTDLNIS("YTDLnis",
                "com.deniscerri.ytdl",
                "https://ytdlnis.org",
                true),
        SEAL("Seal",
                "com.junkfood.seal",
                "https://github.com/JunkFood02/Seal/releases/latest",
                true),
        GRAYJAY("Grayjay",
                "com.futo.platformplayer",
                "https://grayjay.app"),
        LIBRETUBE("LibreTube",
                "com.github.libretube",
                "https://libretube.dev"),
        NEWPIPE("NewPipe",
                "org.schabi.newpipe",
                "https://newpipe.net"),
        PIPEPIPE("PipePipe",
                "InfinityLoop1309.NewPipeEnhanced",
                "https://pipepipe.dev"),
        TUBULAR("Tubular",
                "org.polymorphicshade.tubular",
                "https://github.com/polymorphicshade/Tubular/releases/latest"),
        OTHER(sf("revanced_external_downloader_other_item").toString(),
                null,
                null,
                true);

        private static final Map<String, Downloader> PACKAGE_TO_ENUM = new HashMap<>();

        static {
            for (Downloader downloader : values()) {
                String packageName = downloader.packageName;
                if (packageName != null) {
                    PACKAGE_TO_ENUM.put(packageName, downloader);
                }
            }
        }

        /**
         * Finds a Downloader by its package name. This method can never return {@link #OTHER}.
         * @return The Downloader enum or null if not found.
         */
        @Nullable
        public static Downloader findByPackageName(String packageName) {
            return PACKAGE_TO_ENUM.get(Objects.requireNonNull(packageName));
        }

        public final String name;
        @Nullable
        public final String packageName;
        @Nullable
        public final String downloadUrl;
        /**
         * If a downloader app should be shown in the preference settings
         * if the app is not currently installed.
         */
        public final boolean isPreferred;

        Downloader(String name, String packageName, String downloadUrl) {
            this(name, packageName, downloadUrl, false);
        }

        Downloader(String name, @Nullable String packageName, @Nullable String downloadUrl, boolean isPreferred) {
            this.name = name;
            this.packageName = packageName;
            this.downloadUrl = downloadUrl;
            this.isPreferred = isPreferred;
        }

        public boolean isInstalled() {
            return packageName != null && isAppInstalledAndEnabled(packageName);
        }
    }

    private static boolean isAppInstalledAndEnabled(String packageName) {
        try {
            if (Utils.getContext().getPackageManager().getApplicationInfo(packageName, 0).enabled) {
                Logger.printDebug(() -> "App installed: " + packageName);
                return true;
            }
        } catch (PackageManager.NameNotFoundException error) {
            Logger.printDebug(() -> "App not installed: " + packageName);
        }
        return false;
    }

    private EditText editText;
    private CustomDialogListPreference.ListPreferenceArrayAdapter adapter;

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

    private void updateEntries() {
        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> entryValues = new ArrayList<>();

        for (Downloader downloader : Downloader.values()) {
            if (downloader.isPreferred || downloader.isInstalled()) {
                String packageName = downloader.packageName;

                entries.add(downloader.name);
                entryValues.add(packageName != null
                        ? packageName
                        : Downloader.OTHER.name);
            }
        }

        setEntries(entries.toArray(new CharSequence[0]));
        setEntryValues(entryValues.toArray(new CharSequence[0]));
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
        // Must set entries before showing the dialog, to handle if
        // an app is installed while the settings are open in the background.
        updateEntries();

        Context context = getContext();
        String packageName = Settings.EXTERNAL_DOWNLOADER_PACKAGE_NAME.get();

        // Create the main layout for the dialog content.
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        // Create ListView for predefined downloader apps.
        ListView listView = new ListView(context);
        listView.setId(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Create custom adapter for the ListView.
        final boolean usingCustomDownloader = Downloader.findByPackageName(packageName) == null;
        adapter = new CustomDialogListPreference.ListPreferenceArrayAdapter(
                context,
                LAYOUT_REVANCED_CUSTOM_LIST_ITEM_CHECKED,
                getEntries(),
                getEntryValues(),
                usingCustomDownloader
                        ? Downloader.OTHER.name
                        : packageName
        );
        listView.setAdapter(adapter);

        Function<String, Void> updateListViewSelection = (updatedPackageName) -> {
            String entryValueName = Downloader.findByPackageName(updatedPackageName) == null
                    ? Downloader.OTHER.name
                    : updatedPackageName;
            CharSequence[] entryValues = getEntryValues();

            for (int i = 0, length = entryValues.length; i < length; i++) {
                String entryString = entryValues[i].toString();
                if (entryString.equals(entryValueName)) {
                    listView.setItemChecked(i, true);
                    listView.setSelection(i);
                    adapter.setSelectedValue(entryString);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
            return null;
        };
        updateListViewSelection.apply(packageName);

        // Handle item click to select value.
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedValue = getEntryValues()[position].toString();
            Downloader selectedApp = Downloader.findByPackageName(selectedValue);

            if (selectedApp != null) {
                editText.setText(selectedApp.packageName);
                editText.setEnabled(false); // Disable editing for predefined options.
            } else {
                String savedPackageName = Settings.EXTERNAL_DOWNLOADER_PACKAGE_NAME.get();
                editText.setText(Downloader.findByPackageName(savedPackageName) == null
                        ? savedPackageName // If the user is clicking thru options then retain existing other app.
                        : ""
                );
                editText.setEnabled(true); // Enable editing for Custom.
                editText.requestFocus();
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
        editText.setText(packageName);
        editText.setSelection(packageName.length());
        editText.setHint(str("revanced_external_downloader_other_item_hint"));
        editText.setSingleLine(true); // Restrict EditText to a single line.
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        // Set initial EditText state based on selected downloader.
        editText.setEnabled(usingCustomDownloader);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable edit) {
                String updatedPackageName = edit.toString().trim();
                updateListViewSelection.apply(updatedPackageName);
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
        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                context,
                getTitle() != null ? getTitle().toString() : "",
                null,
                null,
                null,
                () -> {
                    String newValue = editText.getText().toString().trim();
                    if (newValue.isEmpty()) {
                        // Show dialog if EditText is empty.
                        CustomDialog.create(
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

                    if (showDialogIfAppIsNotInstalled(getContext(), newValue)) {
                        return; // Invalid package. Do not save.
                    }

                    // Save custom package name.
                    if (callChangeListener(newValue)) {
                        setValue(newValue);
                    }
                },
                () -> {}, // Cancel button action (dismiss only).
                str("revanced_settings_reset"),
                () -> { // Reset action.
                    String defaultValue = Settings.EXTERNAL_DOWNLOADER_PACKAGE_NAME.defaultValue;
                    editText.setText(defaultValue);
                    editText.setSelection(defaultValue.length());
                    editText.setEnabled(false); // Disable editing on reset.
                    updateListViewSelection.apply(defaultValue);
                },
                false
        );

        // Add the content layout directly to the dialog's main layout.
        LinearLayout dialogMainLayout = dialogPair.second;
        dialogMainLayout.addView(contentLayout, dialogMainLayout.getChildCount() - 1);

        // Update ListView height dynamically based on orientation.
        //noinspection ExtractMethodRecommender
        Runnable updateListViewHeight = () -> {
            int totalHeight = 0;
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter != null) {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                final int listAdapterCount = listAdapter.getCount();
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

            final int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                // In portrait orientation, use WRAP_CONTENT for ListView height.
                listViewParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            } else {
                // In landscape orientation, limit ListView height to 30% of screen height.
                final int maxHeight = Utils.percentageHeightToPixels(30);
                listViewParams.height = Math.min(totalHeight, maxHeight);
            }
            listView.setLayoutParams(listViewParams);
        };

        // Initial height calculation.
        updateListViewHeight.run();

        // Listen for configuration changes (e.g., orientation).
        View dialogView = dialogPair.second;
        // Recalculate height when layout changes (e.g., orientation change).
        dialogView.getViewTreeObserver().addOnGlobalLayoutListener(updateListViewHeight::run);

        // Show the dialog.
        dialogPair.first.show();
    }

    /**
     * @return If the app is not installed and a dialog was shown.
     */
    public static boolean showDialogIfAppIsNotInstalled(Context context, String packageName) {
        if (isAppInstalledAndEnabled(packageName)) {
            return false;
        }

        Downloader downloader = Downloader.findByPackageName(packageName);
        String downloadUrl = downloader != null
                ? downloader.downloadUrl
                : null;
        String okButtonText = downloadUrl != null
                ? str("gms_core_dialog_open_website_text") // Open website.
                : null; // Ok.
        // Show a dialog if the recommended app is not installed or if the custom package cannot be found.
        String message = downloader != null
                ? str("revanced_external_downloader_not_installed_warning", downloader.name)
                : str("revanced_external_downloader_package_not_found_warning", packageName);

        CustomDialog.create(
                context,
                str("revanced_external_downloader_not_found_title"),
                message,
                null,
                okButtonText,
                () -> {
                    try {
                        // OK button action: open the downloader's URL if available.
                        if (downloadUrl != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    } catch (Exception ex) {
                        Logger.printException(() -> "Failed to open downloader URL: " + downloader, ex);
                    }
                },
                () -> {}, // Cancel button action (dismiss only).
                null,
                null,
                false
        ).first.show();

        return true;
    }
}
