package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
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

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.StringSetting;

/**
 * A custom preference for selecting a color via a hexadecimal code or a color picker dialog.
 * Extends {@link EditTextPreference} to display a colored dot next to the title and input field,
 * reflecting the currently selected color.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ColorPickerPreference extends EditTextPreference {
    /**
     * TextView displaying a colored dot for the selected color preview.
     */
    private TextView colorPreview;
    /**
     * Current color in RGB format (without alpha).
     */
    private int currentColor;
    /**
     * Associated setting for storing the color value.
     */
    private StringSetting colorSetting;
    /**
     * Original title of the preference, excluding the color dot.
     */
    private CharSequence originalTitle;
    /**
     * TextWatcher for the EditText to monitor color input changes.
     */
    private TextWatcher colorTextWatcher;

    private static String getColorString(int originalColor) {
        return String.format("#%06X", originalColor);
    }

    /**
     * Constructs a ColorPickerPreference with specified context and attributes.
     *
     * @param context The context in which the view is running.
     * @param attrs   The XML tag attributes.
     */
    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructs a ColorPickerPreference with specified context.
     *
     * @param context The context in which the view is running.
     */
    public ColorPickerPreference(Context context) {
        super(context);
        init();
    }

    /**
     * Initializes the preference by setting up the EditText, loading the color, and updating the title.
     */
    private void init() {
        colorSetting = (StringSetting) Setting.getSettingFromPath(getKey());
        if (colorSetting == null) {
            Logger.printException(() -> "Could not find color setting for: " + getKey());
        }
        originalTitle = super.getTitle();
        getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        loadFromSettings();
        updateTitleWithColorDot();
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
            Logger.printException(() -> "Invalid color: " + colorString, ex);
            setText(colorSetting.resetToDefault());
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
        if (colorString.equals(getText())) {
            Logger.printDebug(() -> "Ignoring unchanged color: " + colorString);
            return;
        }

        super.setText(colorString);

        currentColor = Color.parseColor(colorString) & 0xFFFFFF;
        if (colorSetting != null) {
            colorSetting.save(getColorString(currentColor));
        }
        updateTitleWithColorDot();
        updateColorPreview();
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
        View colorPickerView = LayoutInflater.from(context)
                .inflate(getResourceIdentifier("revanced_color_picker", "layout"), null);
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
        editText.setText(getColorString(currentColor));
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
                try {
                    String colorString = edit.toString();
                    final int colorStringLength = colorString.length();

                    if (!colorString.startsWith("#")) {
                        edit.insert(0, "#"); // Recursively calls back into this method.
                        return;
                    }

                    final int maxColorStringLength = 7; // #RRGGBB
                    if (colorStringLength > maxColorStringLength) {
                        edit.delete(maxColorStringLength, colorStringLength);
                        return;
                    }

                    final int newColor = Color.parseColor(colorString) & 0xFFFFFF;
                    if (currentColor != newColor) {
                        Logger.printDebug(() -> "afterTextChanged");
                        currentColor = newColor;
                        updateColorPreview();
                        colorPickerView.setColor(newColor);
                    }
                } catch (IllegalArgumentException ex) {
                    Logger.printDebug(() -> "afterTextChanged bad color", ex);
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
        Context context = builder.getContext();
        LinearLayout dialogLayout = createDialogLayout(context);
        builder.setView(dialogLayout);

        builder.setNeutralButton(str("revanced_settings_reset"), (dialog, which) -> {
            try {
                setText(colorSetting.resetToDefault());
                Utils.showToastShort(str("revanced_settings_color_reset"));
            } catch (Exception ex) {
                Logger.printException(() -> "setNeutralButton failure", ex);
            }
        });

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            EditText editText = getEditText();
            saveColor(editText, (AlertDialog) dialog);
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) return;

        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Saves the color from the EditText and dismisses the dialog.
     *
     * @param editText The EditText containing the color input.
     * @param dialog The AlertDialog to dismiss.
     */
    private void saveColor(EditText editText, AlertDialog dialog) {
        String colorString = editText.getText().toString();
        if (TextUtils.isEmpty(colorString)) {
            if (colorSetting != null) {
                colorSetting.resetToDefault();
                loadFromSettings();
            }
            dialog.dismiss();
            return;
        }

        try {
            setText(colorString);
            dialog.dismiss();
        } catch (IllegalArgumentException ex) {
            Logger.printException(() -> "Invalid color format: " + colorString, ex);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (colorTextWatcher != null) {
            getEditText().removeTextChangedListener(colorTextWatcher);
        }
    }

    /**
     * Creates a Spanned object for a colored dot using SpannableString.
     *
     * @param color The RGB color (without alpha).
     * @return A Spanned object with the colored dot.
     */
    public static Spanned getColorDot(int color) {
        String dot = "⬤";
        SpannableString spannable = new SpannableString(dot);
        spannable.setSpan(new ForegroundColorSpan(color | 0xFF000000), 0, dot.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new RelativeSizeSpan(1.5f), 0, 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Creates a Spanned object combining the original title with a colored dot.
     *
     * @return A Spanned object with the title and colored dot.
     */
    public final Spanned getTitleWithColorDot() {
        SpannableString spannable = new SpannableString("⬤ " + originalTitle);
        spannable.setSpan(new ForegroundColorSpan(currentColor | 0xFF000000), 0, 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Updates the preference title with a colored dot.
     */
    private void updateTitleWithColorDot() {
        setTitle(getTitleWithColorDot());
    }
}
