package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

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
            existingSettings = SettingsEnum.exportJSON(getContext());
            getEditText().setText(existingSettings);
        } catch (Exception ex) {
            LogHelper.printException(() -> "showDialog failure", ex);
        }
        return true;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        try {
            // Show the user the settings in JSON format.
            builder.setNeutralButton(str("revanced_settings_import_copy"), (dialog, which) -> {
                ReVancedUtils.setClipboard(getEditText().getText().toString());
            }).setPositiveButton(str("revanced_settings_import"), (dialog, which) -> {
                importSettings(getEditText().getText().toString());
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "onPrepareDialogBuilder failure", ex);
        }
    }

    private void importSettings(String replacementSettings) {
        try {
            if (replacementSettings.equals(existingSettings)) {
                return;
            }
            ReVancedSettingsFragment.settingImportInProgress = true;
            final boolean rebootNeeded = SettingsEnum.importJSON(replacementSettings);
            if (rebootNeeded) {
                ReVancedSettingsFragment.showRestartDialog(getContext());
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "importSettings failure", ex);
        } finally {
            ReVancedSettingsFragment.settingImportInProgress = false;
        }
    }

}