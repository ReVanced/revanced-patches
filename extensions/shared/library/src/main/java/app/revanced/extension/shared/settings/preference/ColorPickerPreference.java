package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.StringSetting;

/**
 * A custom preference for selecting a color via a hexadecimal code or a color picker dialog.
 * Extends {@link EditTextPreference} to display a colored dot in the widget area,
 * reflecting the currently selected color. The dot is dimmed when the preference is disabled.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ColorPickerPreference extends EditTextPreference {

    /**
     * Character to show the color appearance.
     */
    public static final String COLOR_DOT_STRING = "⬤";

    /**
     * Length of a valid color string of format #RRGGBB.
     */
    public static final int COLOR_STRING_LENGTH = 7;

    /**
     * Hex string of 0 to 6 characters.
     */
    private static final Pattern COLOR_STRING_PATTERN = Pattern.compile("[0-9A-Fa-f]{0,6}");

    /**
     * // Alpha for dimming when the preference is disabled.
     */
    private static final float DISABLED_ALPHA = 0.5f; // 50%

    /**
     * TextView displaying a colored dot for the selected color preview in the dialog.
     */
    private TextView colorPreview;

    /**
     * TextView displaying a colored dot in the widget area.
     */
    private TextView widgetColorDot;

    /**
     * Current color in RGB format (without alpha).
     */
    private int currentColor;

    /**
     * Associated setting for storing the color value.
     */
    private StringSetting colorSetting;

    /**
     * TextWatcher for the EditText to monitor color input changes.
     */
    private TextWatcher colorTextWatcher;

    /**
     * Removes non valid hex characters, converts to all uppercase,
     * and adds # character to the start if not present.
     */
    public static String cleanupColorCodeString(String colorString) {
        StringBuilder result = new StringBuilder("#");
        Matcher matcher = COLOR_STRING_PATTERN.matcher(colorString);

        while (matcher.find()) {
            result.append(matcher.group());
        }

        return result.toString().toUpperCase(Locale.ROOT);
    }

    private static String getColorString(int originalColor) {
        return String.format("#%06X", originalColor);
    }

    /**
     * Creates a Spanned object for a colored dot using SpannableString.
     *
     * @param color The RGB color (without alpha).
     * @return A Spanned object with the colored dot.
     */
    public static Spanned getColorDot(int color) {
        SpannableString spannable = new SpannableString(COLOR_DOT_STRING);
        spannable.setSpan(new ForegroundColorSpan(color | 0xFF000000), 0, COLOR_DOT_STRING.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public ColorPickerPreference(Context context) {
        super(context);
        init();
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializes the preference by setting up the EditText, loading the color, and set the widget layout.
     */
    private void init() {
        colorSetting = (StringSetting) Setting.getSettingFromPath(getKey());
        if (colorSetting == null) {
            Logger.printException(() -> "Could not find color setting for: " + getKey());
        }

        EditText editText = getEditText();
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editText.setAutofillHints((String) null);
        }

        loadFromSettings();

        // Set the widget layout to a custom layout containing the colored dot
        setWidgetLayoutResource(getResourceIdentifier("revanced_color_dot_widget", "layout"));
    }

    /**
     * Loads the color from the associated {@link StringSetting}.
     * Resets to default if the stored color is invalid.
     */
    private void loadFromSettings() {
        String colorString = colorSetting.get();

        try {
            setText(colorString);
        } catch (Exception ex) {
            Logger.printDebug(() -> "Parse color error: " + colorString, ex);
            Utils.showToastShort(str("revanced_settings_color_invalid"));
            colorSetting.resetToDefault();
            loadFromSettings();
        }
    }

    /**
     * Sets the selected color and updates the UI and settings.
     *
     * @param colorString The color in hexadecimal format (e.g., "#RRGGBB").
     * @throws IllegalArgumentException If the color string is invalid.
     */
    @Override
    public final void setText(String colorString) throws IllegalArgumentException {
        try {
            super.setText(colorString);

            currentColor = Color.parseColor(colorString) & 0xFFFFFF;
            if (colorSetting != null) {
                colorSetting.save(getColorString(currentColor));
            }
            updateColorPreview();
            updateWidgetColorDot();
        } catch (IllegalArgumentException ex) {
            // This code is reached if the user pastes settings json with an invalid color
            // since this preference is updated with the new setting text.
            Logger.printDebug(() -> "Parse color error: " + colorString, ex);
            Utils.showToastShort(str("revanced_settings_color_invalid"));
            setText(colorSetting.resetToDefault());
        } catch (Exception ex) {
            Logger.printException(() -> "setText failure: " + colorString, ex);
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        // Find the TextView in the widget area
        widgetColorDot = view.findViewById(getResourceIdentifier(
                "revanced_color_dot_widget", "id"));
        if (widgetColorDot != null) {
            widgetColorDot.setText(getColorDot(currentColor));
            widgetColorDot.setAlpha(isEnabled() ? 1.0f : DISABLED_ALPHA);
        }
    }

    /**
     * Creates a layout with a color preview and EditText for hex color input.
     *
     * @param context The context for creating the layout.
     * @return A LinearLayout containing the color preview and EditText.
     */
    private LinearLayout createDialogLayout(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(70, 0, 70, 0);

        // Inflate color picker.
        View colorPickerView = LayoutInflater.from(context).inflate(
                getResourceIdentifier("revanced_color_picker", "layout"), null);
        CustomColorPickerView customColorPickerView = colorPickerView.findViewById(
                getResourceIdentifier("color_picker_view", "id"));
        // Set the initial color to the saved color from settings
        customColorPickerView.setColor(currentColor);
        layout.addView(colorPickerView);

        // Horizontal layout for preview and EditText.
        LinearLayout inputLayout = new LinearLayout(context);
        inputLayout.setOrientation(LinearLayout.HORIZONTAL);
        inputLayout.setPadding(0, 20, 0, 0);

        colorPreview = new TextView(context);
        updateColorPreview();
        inputLayout.addView(colorPreview);

        EditText editText = getEditText();
        ViewParent parent = editText.getParent();
        if (parent instanceof ViewGroup parentViewGroup) {
            parentViewGroup.removeView(editText);
        }
        String colorString = getColorString(currentColor);
        editText.setText(colorString);
        editText.setSelection(colorString.length());
        colorTextWatcher = createColorTextWatcher(customColorPickerView);
        editText.addTextChangedListener(colorTextWatcher);
        inputLayout.addView(editText);

        layout.addView(inputLayout);

        // Set up color picker listener with debouncing.
        customColorPickerView.setOnColorChangedListener(color -> {
            try {
                Logger.printDebug(() -> "setOnColorChangedListener");
                String hexColor = getColorString(color & 0xFFFFFF);
                currentColor = color & 0xFFFFFF;
                updateColorPreview();
                updateWidgetColorDot();
                if (!editText.getText().toString().equals(hexColor)) {
                    editText.setText(hexColor);
                    editText.setSelection(hexColor.length());
                }
            } catch (Exception ex) {
                Logger.printException(() -> "setOnColorChangedListener failure", ex);
            }
        });

        return layout;
    }

    /**
     * Updates the color preview TextView with a colored dot.
     */
    private void updateColorPreview() {
        if (colorPreview != null) {
            colorPreview.setText(getColorDot(currentColor));
            colorPreview.setAlpha(1.0f);
        }
    }

    private void updateWidgetColorDot() {
        if (widgetColorDot != null) {
            widgetColorDot.setText(getColorDot(currentColor));
            widgetColorDot.setAlpha(isEnabled() ? 1.0f : DISABLED_ALPHA);
        }
    }

    /**
     * Creates a TextWatcher to monitor changes in the EditText for color input.
     *
     * @return A TextWatcher that updates the color preview on valid input.
     */
    private TextWatcher createColorTextWatcher(CustomColorPickerView colorPickerView) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable edit) {
                String colorString = edit.toString();

                try {
                    String normalizedColorString = cleanupColorCodeString(colorString);
                    if (!normalizedColorString.equals(colorString)) {
                        edit.replace(0, colorString.length(), normalizedColorString);
                        return;
                    }

                    if (normalizedColorString.length() != 7) {
                        // User is still typing out the color.
                        return;
                    }

                    final int newColor = Color.parseColor(colorString);
                    if (currentColor != newColor) {
                        Logger.printDebug(() -> "afterTextChanged to: " + normalizedColorString);
                        currentColor = newColor;
                        updateColorPreview();
                        updateWidgetColorDot();
                        colorPickerView.setColor(newColor);
                    }
                } catch (IllegalArgumentException ex) {
                    // Should never be reached since input is validated before using.
                    Logger.printException(() -> "afterTextChanged bad color: " + colorString, ex);
                } catch (Exception ex) {
                    Logger.printException(() -> "afterTextChanged failure", ex);
                }
            }
        };
    }

    /**
     * Prepares the dialog builder with a custom view and reset button.
     *
     * @param builder The AlertDialog.Builder to configure.
     */
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        Utils.setEditTextDialogTheme(builder);
        LinearLayout dialogLayout = createDialogLayout(builder.getContext());
        builder.setView(dialogLayout);
        final int originalColor = currentColor;

        builder.setNeutralButton(str("revanced_settings_reset_color"), (dialog, which) -> {
            try {
                setText(colorSetting.resetToDefault());
            } catch (Exception ex) {
                Logger.printException(() -> "setNeutralButton failure", ex);
            }
        });

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            try {
                String colorString = getEditText().getText().toString();

                if (colorString.length() != COLOR_STRING_LENGTH) {
                    // User did not finish entering the color.
                    return;
                }

                setText(colorString);
            } catch (Exception ex) {
                // Should never happen due to a bad color string,
                // since the text is validated and fixed while the user types.
                Logger.printException(() -> "setPositiveButton failure", ex);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            try {
                // Restore the original color.
                setText(getColorString(originalColor));
            } catch (Exception ex) {
                Logger.printException(() -> "setNegativeButton failure", ex);
            }
        });
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) return;

        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (colorTextWatcher != null) {
            getEditText().removeTextChangedListener(colorTextWatcher);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateWidgetColorDot();
    }
}
