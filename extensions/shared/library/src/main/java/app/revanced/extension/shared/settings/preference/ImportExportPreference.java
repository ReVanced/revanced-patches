package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.app.Dialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import app.revanced.extension.shared.Logger;

@SuppressWarnings({"unused", "deprecation"})
public class ImportExportPreference extends Preference implements Preference.OnPreferenceClickListener {

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

    private void init() {
        setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        try {
            if (AbstractPreferenceFragment.instance != null) {
                AbstractPreferenceFragment.instance.showImportExportTextDialog();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onPreferenceClick failure", ex);
        }

        return true;
    }
}
