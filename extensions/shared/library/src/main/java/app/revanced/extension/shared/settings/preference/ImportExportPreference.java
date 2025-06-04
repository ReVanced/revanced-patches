package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.Setting;

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
            // Must set text before showing dialog,
            // otherwise text is non-selectable if this preference is later reopened.
            existingSettings = Setting.exportToJson(getContext());
            getEditText().setText(existingSettings);
        } catch (Exception ex) {
            Logger.printException(() -> "showDialog failure", ex);
        }
        return true;
    }

    @Override
    protected void showDialog(Bundle state) {
        try {
            Context context = getContext();
            EditText editText = getEditText();

            // Remove EditText from its current parent, if any.
            ViewGroup parent = (ViewGroup) editText.getParent();
            if (parent != null) {
                parent.removeView(editText);
            }

            // Style the EditText to match the dialog theme.
            editText.setTextColor(Utils.isDarkModeEnabled() ? Color.WHITE : Color.BLACK);
            editText.setBackgroundColor(Utils.isDarkModeEnabled() ? Color.BLACK : Color.WHITE);
            editText.setPadding(dipToPixels(8), dipToPixels(8), dipToPixels(8), dipToPixels(8));
            ShapeDrawable editTextBackground = new ShapeDrawable(new RoundRectShape(
                    AbstractPreferenceFragment.createCornerRadii(10), null, null));
            editTextBackground.getPaint().setColor(Utils.isDarkModeEnabled() ? Color.BLACK : Color.WHITE);
            editText.setBackground(editTextBackground);

            // Create a custom dialog using the same style as AbstractPreferenceFragment.
            Pair<Dialog, LinearLayout> dialogPair = AbstractPreferenceFragment.createCustomDialog(
                    context,
                    str("revanced_pref_import_export_title"), // Title for the dialog.
                    null, // Message is replaced by EditText.
                    null, // OK button text (will be customized below).
                    null, // OK action (will be customized below).
                    null, // Cancel action (will be customized below).
                    null, // Neutral button text (will be customized below).
                    null  // Neutral button action (will be customized below).
            );

            // Add the EditText to the dialog's layout.
            LinearLayout mainLayout = dialogPair.second;
            // Remove empty message TextView from the dialog's layout
            TextView messageView = (TextView) mainLayout.getChildAt(1); // Message is added at index 1 in createCustomDialog
            if (TextUtils.isEmpty(messageView.getText())) {
                mainLayout.removeView(messageView);
            }
            LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            editTextParams.setMargins(0, dipToPixels(8), 0, dipToPixels(8));
            mainLayout.addView(editText, 1, editTextParams); // Add EditText after title, before buttons.

            // Remove the existing button container
            int childCount = mainLayout.getChildCount();
            if (childCount > 2) {
                mainLayout.removeViewAt(childCount - 1);
            }

            // Create first row button container (Cancel and Import)
            LinearLayout firstButtonRow = new LinearLayout(context);
            firstButtonRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams firstRowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            firstRowParams.setMargins(0, dipToPixels(8), 0, dipToPixels(4));
            firstButtonRow.setLayoutParams(firstRowParams);
            firstButtonRow.setGravity(android.view.Gravity.START);

            // Add Cancel button (left)
            AbstractPreferenceFragment.addButton(
                    firstButtonRow,
                    context,
                    context.getString(android.R.string.cancel),
                    () -> {}, // Cancel action just dismisses the dialog
                    false,
                    true,
                    false,
                    dialogPair.first
            );

            // Add Import button (right)
            AbstractPreferenceFragment.addButton(
                    firstButtonRow,
                    context,
                    str("revanced_settings_import"),
                    () -> importSettings(context, editText.getText().toString()),
                    true,
                    false,
                    false,
                    dialogPair.first
            );

            // Create second row button container (Export)
            LinearLayout secondButtonRow = new LinearLayout(context);
            secondButtonRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams secondRowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            secondRowParams.setMargins(0, dipToPixels(4), 0, 0);
            secondButtonRow.setLayoutParams(secondRowParams);
            secondButtonRow.setGravity(android.view.Gravity.END);

            // Create a spacer to align Export button with Import button
            View spacer = new View(context);
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, dipToPixels(36));
            spacerParams.weight = 1;
            spacer.setLayoutParams(spacerParams);
            secondButtonRow.addView(spacer);

            // Add Export button (right, same width as Import)
            AbstractPreferenceFragment.addButton(
                    secondButtonRow,
                    context,
                    str("revanced_settings_import_copy"),
                    () -> Utils.setClipboard(editText.getText()),
                    false,
                    false,
                    true,
                    dialogPair.first
            );

            // Add button rows to main layout
            mainLayout.addView(firstButtonRow);
            mainLayout.addView(secondButtonRow);

            dialogPair.first.show();
        } catch (Exception ex) {
            Logger.printException(() -> "showDialog failure", ex);
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
                AbstractPreferenceFragment.showRestartDialog(context);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "importSettings failure", ex);
        } finally {
            AbstractPreferenceFragment.settingImportInProgress = false;
        }
    }
}
