package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.ResourceType;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.ui.CustomDialog;

@SuppressWarnings("deprecation")
public abstract class AbstractPreferenceFragment extends PreferenceFragment {

    private static class DebouncedListView extends ListView {
        private long lastClick;

        public DebouncedListView(Context context) {
            super(context);

            setId(android.R.id.list); // Required so PreferenceFragment recognizes it.

            // Match the default layout params
            setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }

        @Override
        public boolean performItemClick(View view, int position, long id) {
            final long now = SystemClock.elapsedRealtime();
            if (now - lastClick < 500) {
                return true; // Ignore fast double click.
            }
            lastClick = now;

            return super.performItemClick(view, position, id);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public static AbstractPreferenceFragment instance;

    /**
     * Indicates that if a preference changes,
     * to apply the change from the Setting to the UI component.
     */
    public static boolean settingImportInProgress;

    /**
     * Prevents recursive calls during preference <-> UI syncing from showing extra dialogs.
     */
    private static boolean updatingPreference;

    /**
     * Used to prevent showing reboot dialog, if user cancels a setting user dialog.
     */
    private static boolean showingUserDialogMessage;

    /**
     * Confirm and restart dialog button text and title.
     * Set by subclasses if Strings cannot be added as a resource.
     */
    @Nullable
    protected static CharSequence restartDialogTitle, restartDialogMessage, restartDialogButtonText, confirmDialogTitle;

    private static final int READ_REQUEST_CODE = 42;
    private static final int WRITE_REQUEST_CODE = 43;
    private String existingSettings = "";

    private EditText currentImportExportEditText;

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            if (updatingPreference) {
                Logger.printDebug(() -> "Ignoring preference change as sync is in progress");
                return;
            }

            Setting<?> setting = Setting.getSettingFromPath(Objects.requireNonNull(str));
            if (setting == null) {
                return;
            }
            Preference pref = findPreference(str);
            if (pref == null) {
                return;
            }
            Logger.printDebug(() -> "Preference changed: " + setting.key);

            if (!settingImportInProgress && !showingUserDialogMessage) {
                if (setting.userDialogMessage != null && !prefIsSetToDefault(pref, setting)) {
                    // Do not change the setting yet, to allow preserving whatever
                    // list/text value was previously set if it needs to be reverted.
                    showSettingUserDialogConfirmation(pref, setting);
                    return;
                } else if (setting.rebootApp) {
                    showRestartDialog(getContext());
                }
            }

            updatingPreference = true;
            // Apply 'Setting <- Preference', unless during importing when it needs to be 'Setting -> Preference'.
            // Updating here can cause a recursive call back into this same method.
            updatePreference(pref, setting, true, settingImportInProgress);
            // Update any other preference availability that may now be different.
            updateUIAvailability();
            updatingPreference = false;
        } catch (Exception ex) {
            Logger.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    /**
     * Initialize this instance, and do any custom behavior.
     * <p>
     * To ensure all {@link Setting} instances are correctly synced to the UI,
     * it is important that subclasses make a call or otherwise reference their Settings class bundle
     * so all app specific {@link Setting} instances are loaded before this method returns.
     */
    protected void initialize() {
        String preferenceResourceName;
        if (BaseSettings.SHOW_MENU_ICONS.get()) {
            preferenceResourceName = Utils.appIsUsingBoldIcons()
                    ? "revanced_prefs_icons_bold"
                    : "revanced_prefs_icons";
        } else {
            preferenceResourceName = "revanced_prefs";
        }

        final var identifier = Utils.getResourceIdentifier(ResourceType.XML, preferenceResourceName);
        if (identifier == 0) return;
        addPreferencesFromResource(identifier);

        PreferenceScreen screen = getPreferenceScreen();
        Utils.sortPreferenceGroups(screen);
        Utils.setPreferenceTitlesToMultiLineIfNeeded(screen);
    }

    private void showSettingUserDialogConfirmation(Preference pref, Setting<?> setting) {
        Utils.verifyOnMainThread();

        final var context = getContext();
        if (confirmDialogTitle == null) {
            confirmDialogTitle = str("revanced_settings_confirm_user_dialog_title");
        }

        showingUserDialogMessage = true;

        CharSequence message = BulletPointPreference.formatIntoBulletPoints(
                Objects.requireNonNull(setting.userDialogMessage).toString());

        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                context,
                confirmDialogTitle, // Title.
                message,
                null, // No EditText.
                null, // OK button text.
                () -> {
                    // OK button action. User confirmed, save to the Setting.
                    updatePreference(pref, setting, true, false);

                    // Update availability of other preferences that may be changed.
                    updateUIAvailability();

                    if (setting.rebootApp) {
                        showRestartDialog(context);
                    }
                },
                () -> {
                    // Cancel button action. Restore whatever the setting was before the change.
                    updatePreference(pref, setting, true, true);
                },
                null, // No Neutral button.
                null, // No Neutral button action.
                true  // Dismiss dialog when onNeutralClick.
        );

        dialogPair.first.setOnDismissListener(d -> showingUserDialogMessage = false);
        dialogPair.first.setCancelable(false);

        // Show the dialog.
        dialogPair.first.show();
    }

    /**
     * Updates all Preferences values and their availability using the current values in {@link Setting}.
     */
    protected void updateUIToSettingValues() {
        updatePreferenceScreen(getPreferenceScreen(), true, true);
    }

    /**
     * Updates Preferences availability only using the status of {@link Setting}.
     */
    protected void updateUIAvailability() {
        updatePreferenceScreen(getPreferenceScreen(), false, false);
    }

    /**
     * @return If the preference is currently set to the default value of the Setting.
     */
    protected boolean prefIsSetToDefault(Preference pref, Setting<?> setting) {
        Object defaultValue = setting.defaultValue;
        if (pref instanceof SwitchPreference switchPref) {
            return switchPref.isChecked() == (Boolean) defaultValue;
        }
        String defaultValueString = defaultValue.toString();
        if (pref instanceof EditTextPreference editPreference) {
            return editPreference.getText().equals(defaultValueString);
        }
        if (pref instanceof ListPreference listPref) {
            return listPref.getValue().equals(defaultValueString);
        }

        throw new IllegalStateException("Must override method to handle preference type: " + pref.getClass());
    }

    /**
     * Syncs all UI Preferences to any {@link Setting} they represent.
     */
    private void updatePreferenceScreen(@NonNull PreferenceGroup group,
                                        boolean syncSettingValue,
                                        boolean applySettingToPreference) {
        // Alternatively this could iterate through all Settings and check for any matching Preferences,
        // but there are many more Settings than UI preferences so it's more efficient to only check
        // the Preferences.
        for (int i = 0, prefCount = group.getPreferenceCount(); i < prefCount; i++) {
            Preference pref = group.getPreference(i);
            if (pref instanceof PreferenceGroup subGroup) {
                updatePreferenceScreen(subGroup, syncSettingValue, applySettingToPreference);
            } else if (pref.hasKey()) {
                String key = pref.getKey();
                Setting<?> setting = Setting.getSettingFromPath(key);

                if (setting != null) {
                    updatePreference(pref, setting, syncSettingValue, applySettingToPreference);
                } else if (BaseSettings.DEBUG.get() && (pref instanceof SwitchPreference
                        || pref instanceof EditTextPreference || pref instanceof ListPreference)) {
                    // Probably a typo in the patches preference declaration.
                    Logger.printException(() -> "Preference key has no setting: " + key);
                }
            }
        }
    }

    /**
     * Handles syncing a UI Preference with the {@link Setting} that backs it.
     * If needed, subclasses can override this to handle additional UI Preference types.
     *
     * @param applySettingToPreference If true, then apply {@link Setting} -> Preference.
     *                                 If false, then apply {@link Setting} <- Preference.
     */
    protected void syncSettingWithPreference(@NonNull Preference pref,
                                             @NonNull Setting<?> setting,
                                             boolean applySettingToPreference) {
        if (pref instanceof SwitchPreference switchPref) {
            BooleanSetting boolSetting = (BooleanSetting) setting;
            if (applySettingToPreference) {
                switchPref.setChecked(boolSetting.get());
            } else {
                BooleanSetting.privateSetValue(boolSetting, switchPref.isChecked());
            }
        } else if (pref instanceof EditTextPreference editPreference) {
            if (applySettingToPreference) {
                editPreference.setText(setting.get().toString());
            } else {
                Setting.privateSetValueFromString(setting, editPreference.getText());
            }
        } else if (pref instanceof ListPreference listPref) {
            if (applySettingToPreference) {
                listPref.setValue(setting.get().toString());
            } else {
                Setting.privateSetValueFromString(setting, listPref.getValue());
            }
            updateListPreferenceSummary(listPref, setting);
        } else if (!pref.getClass().equals(Preference.class)) {
            // Ignore root preference class because there is no data to sync.
            Logger.printException(() -> "Setting cannot be handled: " + pref.getClass() + ": " + pref);
        }
    }

    /**
     * Updates a UI Preference with the {@link Setting} that backs it.
     *
     * @param syncSetting If the UI should be synced {@link Setting} <-> Preference
     * @param applySettingToPreference If true, then apply {@link Setting} -> Preference.
     *                                 If false, then apply {@link Setting} <- Preference.
     */
    private void updatePreference(@NonNull Preference pref, @NonNull Setting<?> setting,
                                  boolean syncSetting, boolean applySettingToPreference) {
        if (!syncSetting && applySettingToPreference) {
            throw new IllegalArgumentException();
        }

        if (syncSetting) {
            syncSettingWithPreference(pref, setting, applySettingToPreference);
        }

        updatePreferenceAvailability(pref, setting);
    }

    protected void updatePreferenceAvailability(@NonNull Preference pref, @NonNull Setting<?> setting) {
        pref.setEnabled(setting.isAvailable());
    }

    protected void updateListPreferenceSummary(ListPreference listPreference, Setting<?> setting) {
        String objectStringValue = setting.get().toString();
        final int entryIndex = listPreference.findIndexOfValue(objectStringValue);
        if (entryIndex >= 0) {
            listPreference.setSummary(listPreference.getEntries()[entryIndex]);
        } else {
            // Value is not an available option.
            // User manually edited import data, or options changed and current selection is no longer available.
            // Still show the value in the summary, so it's clear that something is selected.
            listPreference.setSummary(objectStringValue);
        }
    }

    public static void showRestartDialog(Context context) {
        Utils.verifyOnMainThread();
        if (restartDialogTitle == null) {
            restartDialogTitle = str("revanced_settings_restart_title");
        }
        if (restartDialogMessage == null) {
            restartDialogMessage = str("revanced_settings_restart_dialog_message");
        }
        if (restartDialogButtonText == null) {
            restartDialogButtonText = str("revanced_settings_restart");
        }

        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                context,
                restartDialogTitle,              // Title.
                restartDialogMessage,            // Message.
                null,                            // No EditText.
                restartDialogButtonText,         // OK button text.
                () -> Utils.restartApp(context), // OK button action.
                () -> {},                        // Cancel button action (dismiss only).
                null,                            // No Neutral button text.
                null,                            // No Neutral button action.
                true                             // Dismiss dialog when onNeutralClick.
        );

        // Show the dialog.
        dialogPair.first.show();
    }

    /**
     * Import / Export Subroutines
     */
    @NonNull
    private Button createDialogButton(Context context, String text, int marginLeft, int marginRight, View.OnClickListener listener) {
        int height = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 36f, context.getResources().getDisplayMetrics());
        int paddingHorizontal = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 16f, context.getResources().getDisplayMetrics());
        float radius = android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 20f, context.getResources().getDisplayMetrics());

        Button btn = new Button(context, null, 0);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setTextSize(14);
        btn.setSingleLine(true);
        btn.setEllipsize(android.text.TextUtils.TruncateAt.END);
        btn.setGravity(android.view.Gravity.CENTER);
        btn.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
        btn.setTextColor(Utils.isDarkModeEnabled() ? android.graphics.Color.WHITE : android.graphics.Color.BLACK);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setCornerRadius(radius);
        bg.setColor(Utils.getCancelOrNeutralButtonBackgroundColor());
        btn.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, height, 1.0f);
        params.setMargins(marginLeft, 0, marginRight, 0);
        btn.setLayoutParams(params);
        btn.setOnClickListener(listener);

        return btn;
    }
    public void showImportExportTextDialog() {
        try {
            Activity context = getActivity();
            // Must set text before showing dialog,
            // otherwise text is non-selectable if this preference is later reopened.
            existingSettings = Setting.exportToJson(context);
            currentImportExportEditText = getEditText(context);

            // Create a custom dialog with the EditText.
            Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                    context,
                    str("revanced_pref_import_export_title"), // Title.
                    null,     // No message (EditText replaces it).
                    currentImportExportEditText, // Pass the EditText.
                    str("revanced_settings_save"), // OK button text.
                    () -> importSettingsText(context, currentImportExportEditText.getText().toString()), // OK button action.
                    () -> {}, // Cancel button action (dismiss only).
                    str("revanced_settings_import_copy"), // Neutral button (Copy) text.
                    () -> Utils.setClipboard(currentImportExportEditText.getText().toString()), // Neutral button (Copy) action. Show the user the settings in JSON format.
                    true // Dismiss dialog when onNeutralClick.
            );

            LinearLayout fileButtonsContainer = getLinearLayout(context);
            int margin = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 4f, context.getResources().getDisplayMetrics());

            Button btnExport = createDialogButton(context, str("revanced_settings_export_file"), 0, margin, v -> exportActivity());
            Button btnImport = createDialogButton(context, str("revanced_settings_import_file"), margin, 0, v -> importActivity());

            fileButtonsContainer.addView(btnExport);
            fileButtonsContainer.addView(btnImport);

            dialogPair.second.addView(fileButtonsContainer, 2);

            dialogPair.first.setOnDismissListener(d -> currentImportExportEditText = null);

            // If there are no settings yet, then show the on-screen keyboard and bring focus to
            // the edit text. This makes it easier to paste saved settings after a reinstallation.
            dialogPair.first.setOnShowListener(dialogInterface -> {
                if (existingSettings.isEmpty() && currentImportExportEditText != null) {
                    currentImportExportEditText.postDelayed(() -> {
                        if (currentImportExportEditText != null) {
                            currentImportExportEditText.requestFocus();
                            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) imm.showSoftInput(currentImportExportEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                        }
                    }, 100);
                }
            });

            // Show the dialog.
            dialogPair.first.show();
        } catch (Exception ex) {
            Logger.printException(() -> "showImportExportTextDialog failure", ex);
        }
    }

    @NonNull
    private static LinearLayout getLinearLayout(Context context) {
        LinearLayout fileButtonsContainer = new LinearLayout(context);
        fileButtonsContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams fbParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        int marginTop = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 16f, context.getResources().getDisplayMetrics());
        fbParams.setMargins(0, marginTop, 0, 0);
        fileButtonsContainer.setLayoutParams(fbParams);
        return fileButtonsContainer;
    }

    @NonNull
    private EditText getEditText(Context context) {
        EditText editText = new EditText(context);
        editText.setText(existingSettings);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            editText.setAutofillHints((String) null);
        }
        editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setSingleLine(false);
        editText.setTextSize(14);
        return editText;
    }

    public void exportActivity() {
        try {
            Setting.exportToJson(getActivity());

            String formatDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(new java.util.Date());
            String fileName = "revanced_Settings_" + formatDate + ".txt";

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
            startActivityForResult(intent, WRITE_REQUEST_CODE);
        } catch (Exception ex) {
            Logger.printException(() -> "exportActivity failure", ex);
        }
    }

    public void importActivity() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, READ_REQUEST_CODE);
        } catch (Exception ex) {
            Logger.printException(() -> "importActivity failure", ex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new DebouncedListView(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WRITE_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK && data != null) {
            exportTextToFile(data.getData());
        } else if (requestCode == READ_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK && data != null) {
            importTextFromFile(data.getData());
        }
    }

    protected static void showLocalizedToast(String resourceKey, String fallbackMessage) {
        if (Utils.getResourceIdentifier(ResourceType.STRING, resourceKey) != 0) {
            Utils.showToastLong(str(resourceKey));
        } else {
            Utils.showToastLong(fallbackMessage);
        }
    }

    private void exportTextToFile(android.net.Uri uri) {
        try {
            OutputStream out = getContext().getContentResolver().openOutputStream(uri);
            if (out != null) {
                String textToExport = existingSettings;
                if (currentImportExportEditText != null) {
                    textToExport = currentImportExportEditText.getText().toString();
                }
                out.write(textToExport.getBytes(StandardCharsets.UTF_8));
                out.close();

                showLocalizedToast("revanced_settings_export_file_success", "Settings exported successfully");
            }
        } catch (Exception e) {
            showLocalizedToast("revanced_settings_export_file_failed", "Failed to export settings");
            Logger.printException(() -> "exportTextToFile failure", e);
        }
    }

    @SuppressWarnings("CharsetObjectCanBeUsed")
    private void importTextFromFile(android.net.Uri uri) {
        try {
            InputStream in = getContext().getContentResolver().openInputStream(uri);
            if (in != null) {
                Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                String result = scanner.hasNext() ? scanner.next() : "";
                in.close();

                if (currentImportExportEditText != null) {
                    currentImportExportEditText.setText(result);
                    showLocalizedToast("revanced_settings_import_file_success", "Settings imported successfully, tap Save to apply");
                } else {
                    importSettingsText(getContext(), result);
                }
            }
        } catch (Exception e) {
            showLocalizedToast("revanced_settings_import_file_failed", "Failed to import settings");
            Logger.printException(() -> "importTextFromFile failure", e);
        }
    }

    private void importSettingsText(Context context, String replacementSettings) {
        try {
            existingSettings = Setting.exportToJson(null);
            if (replacementSettings.equals(existingSettings)) {
                return;
            }
            settingImportInProgress = true;
            final boolean rebootNeeded = Setting.importFromJSON(getActivity(), replacementSettings);
            if (rebootNeeded) {
                showRestartDialog(context);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "importSettingsText failure", ex);
        } finally {
            settingImportInProgress = false;
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        try {
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(Setting.preferences.name);

            // Must initialize before adding change listener,
            // otherwise the syncing of Setting -> UI
            // causes a callback to the listener even though nothing changed.
            initialize();
            updateUIToSettingValues();

            preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        } catch (Exception ex) {
            Logger.printException(() -> "onCreate() failure", ex);
        }
    }

    @Override
    public void onDestroy() {
        if (instance == this) {
            instance = null;
        }
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }
}
