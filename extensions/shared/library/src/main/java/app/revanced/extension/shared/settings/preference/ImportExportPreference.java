package app.revanced.extension.shared.settings.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

import static app.revanced.extension.shared.StringRef.str;

@SuppressWarnings({"unused", "deprecation"})
public class ImportExportPreference extends EditTextPreference implements Preference.OnPreferenceClickListener {

    private String existingSettings;

    private void init() {
        setSelectable(true);

        EditText editText = getEditText();
        editText.setTextIsSelectable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editText.setAutofillHints((String) null);
        }
        editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7); // Use a smaller font to reduce text wrap.

        setOnPreferenceClickListener(this);
    }

    public ImportExportPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    public ImportExportPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public ImportExportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ImportExportPreference(Context context) {
        super(context);
        init();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        try {
            // Must set text before preparing dialog, otherwise text is non selectable if this preference is later reopened.
            existingSettings = Setting.exportToJson(getContext());
            getEditText().setText(existingSettings);
        } catch (Exception ex) {
            Logger.printException(() -> "showDialog failure", ex);
        }
        return true;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        try {
            Utils.setEditTextDialogTheme(builder);

            // Show the user the settings in JSON format.
            builder.setNeutralButton(str("revanced_settings_import_copy"), (dialog, which) -> {
                Utils.setClipboard(getEditText().getText());
            }).setPositiveButton(str("revanced_settings_import"), (dialog, which) -> {
                importSettings(builder.getContext(), getEditText().getText().toString());
            });
        } catch (Exception ex) {
            Logger.printException(() -> "onPrepareDialogBuilder failure", ex);
        }
    }

    private void importSettings(Context context, String replacementSettings) {
        try {
            if (replacementSettings.equals(existingSettings)) {
                return;
            }
            AbstractPreferenceFragment.settingImportInProgress = true;

            final boolean rebootNeeded = Setting.importFromJSON(context, replacementSettings);
            if (rebootNeeded) {
                AbstractPreferenceFragment.showRestartDialog(getContext());
            }
        } catch (Exception ex) {
            Logger.printException(() -> "importSettings failure", ex);
        } finally {
            AbstractPreferenceFragment.settingImportInProgress = false;
        }
    }

}