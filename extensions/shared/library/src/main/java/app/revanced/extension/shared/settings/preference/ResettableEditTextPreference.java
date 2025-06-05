package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.Setting;

@SuppressWarnings({"unused", "deprecation"})
public class ResettableEditTextPreference extends EditTextPreference {

    /**
     * Setting to reset.
     */
    @Nullable
    private Setting<?> setting;

    public ResettableEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ResettableEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ResettableEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ResettableEditTextPreference(Context context) {
        super(context);
    }

    public void setSetting(@Nullable Setting<?> setting) {
        this.setting = setting;
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
                    Utils.createCornerRadii(10), null, null));
            editTextBackground.getPaint().setColor(Utils.getAppBackground()); // EditText background color.
            editText.setBackground(editTextBackground);

            // Resolve setting if not already set.
            if (setting == null) {
                String key = getKey();
                if (key != null) {
                    setting = Setting.getSettingFromPath(key);
                }
            }

            // Set initial EditText value to the current persisted value or empty string.
            String initialValue = getText() != null ? getText() : "";
            editText.setText(initialValue);
            editText.setSelection(initialValue.length()); // Move cursor to end.

            // Create custom dialog.
            String neutralButtonText = (setting != null) ? str("revanced_settings_reset") : null;
            Pair<Dialog, LinearLayout> dialogPair = Utils.createCustomDialog(
                    context,
                    getTitle() != null ? getTitle().toString() : "", // Dialog title.
                    null, // Message is replaced by EditText.
                    null, // OK button text.
                    () -> {
                        // Persist the EditText value when OK is clicked.
                        String newValue = editText.getText().toString();
                        if (callChangeListener(newValue)) {
                            setText(newValue);
                        }
                    }, // On OK click.
                    () -> {}, // On Cancel click (just dismiss).
                    neutralButtonText, // Neutral button text (Reset).
                    () -> {} // Overrides to prevent dialog dismissal.
            );

            // Add the EditText to the dialog's layout.
            LinearLayout mainLayout = dialogPair.second;
            // Remove empty message TextView from the dialog's layout.
            TextView messageView = (TextView) mainLayout.getChildAt(1);
            if (TextUtils.isEmpty(messageView.getText())) {
                mainLayout.removeView(messageView);
            }
            LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            editTextParams.setMargins(0, dipToPixels(8), 0, dipToPixels(8));
            int maxHeight = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.6);
            editText.setMaxHeight(maxHeight);
            mainLayout.addView(editText, 1, editTextParams);

            // Override the neutral button's OnClickListener to prevent dialog dismissal.
            if (setting != null) {
                LinearLayout buttonContainer = (LinearLayout) mainLayout.getChildAt(mainLayout.getChildCount() - 1);
                Button neutralButton = (Button) buttonContainer.getChildAt(0); // Neutral button is first.
                neutralButton.setOnClickListener(v -> {
                    try {
                        String defaultStringValue = Objects.requireNonNull(setting).defaultValue.toString();
                        editText.setText(defaultStringValue);
                        editText.setSelection(defaultStringValue.length());
                    } catch (Exception ex) {
                        Logger.printException(() -> "reset failure", ex);
                    }
                });
            }

            dialogPair.first.show();
        } catch (Exception ex) {
            Logger.printException(() -> "showDialog failure", ex);
        }
    }
}
