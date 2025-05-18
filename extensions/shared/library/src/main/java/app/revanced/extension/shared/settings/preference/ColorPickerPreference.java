package app.revanced.extension.shared.settings.preference;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.StringSetting;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;

/**
 * A custom preference for selecting a color via a hexadecimal code or a color picker dialog.
 * Extends {@link EditTextPreference} to display a colored dot next to the title and input field,
 * reflecting the currently selected color.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ColorPickerPreference extends EditTextPreference {
    /** TextView displaying a colored dot for the selected color preview. */
    private TextView colorPreview;
    /** Current color in RGB format (without alpha). */
    private int currentColor;
    /** Associated setting for storing the color value. */
    private StringSetting colorSetting;
    /** Original title of the preference, excluding the color dot. */
    private CharSequence originalTitle;

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
        originalTitle = super.getTitle();
        getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        loadFromSettings();
        updateTitleWithColorDot();
    }

    /**
     * Loads the color from the associated {@link StringSetting}.
     * Resets to default if the stored color is invalid.
     */
    private void loadFromSettings() {
        if (colorSetting == null) return;
        try {
            setColor(colorSetting.get());
        } catch (Exception ex) {
            Logger.printException(() -> "Invalid color: " + colorSetting.get(), ex);
            colorSetting.resetToDefault();
            setColor(colorSetting.get());
        }
    }

    /**
     * Sets the selected color and updates the UI and settings.
     *
     * @param colorString The color in hexadecimal format (e.g., "#RRGGBB").
     * @throws IllegalArgumentException If the color string is invalid.
     */
    public final void setColor(String colorString) throws IllegalArgumentException {
        currentColor = Color.parseColor(colorString) & 0xFFFFFF;
        if (colorSetting != null) {
            colorSetting.save(String.format("#%06X", currentColor));
        }
        updateTitleWithColorDot();
        updateColorPreview();
    }

    @Override
    public void setText(String text) {
        super.setText(text);

        setColor(text);
    }

    /**
     * Creates a layout with a color preview and EditText for hex color input.
     *
     * @param context The context for creating the layout.
     * @return A LinearLayout containing the color preview and EditText.
     */
    private LinearLayout createColorInputLayout(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setPadding(70, 0, 70, 0);

        colorPreview = new TextView(context);
        colorPreview.setOnClickListener(v -> showColorPickerDialog(context));
        updateColorPreview();
        layout.addView(colorPreview);

        EditText editText = getEditText();
        ViewParent parent = editText.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(editText);
        }
        editText.setText(String.format("#%06X", currentColor));
        editText.addTextChangedListener(createColorTextWatcher());
        layout.addView(editText);

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
     * Displays a color picker dialog for selecting a color.
     *
     * @param context The context for creating the dialog.
     */
    private void showColorPickerDialog(Context context) {
        final int originalColor = currentColor;
        EditText editText = getEditText();
        int initialColor = parseInitialColor(editText.getText().toString(), originalColor);

        RelativeLayout layout = new RelativeLayout(context);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        View dialogView = LayoutInflater.from(context)
                .inflate(getResourceIdentifier("revanced_color_picker", "layout"), layout);
        CustomColorPickerView colorPickerView = dialogView.findViewById(
                getResourceIdentifier("color_picker_view", "id"));
        colorPickerView.setInitialColor(initialColor);

        AlertDialog dialog = createColorPickerDialog(context, dialogView);
        setupColorPickerDialogListeners(dialog, colorPickerView, editText, originalColor);
        dialog.show();
    }

    /**
     * Parses the initial color for the color picker dialog.
     *
     * @param colorString The color string from EditText.
     * @param fallbackColor The fallback color if parsing fails.
     * @return The parsed or fallback color.
     */
    private int parseInitialColor(String colorString, int fallbackColor) {
        try {
            return Color.parseColor(colorString);
        } catch (IllegalArgumentException e) {
            return fallbackColor;
        }
    }

    /**
     * Creates an AlertDialog for the color picker.
     *
     * @param context The context for the dialog.
     * @param dialogView The view containing the color picker.
     * @return The configured AlertDialog.
     */
    private AlertDialog createColorPickerDialog(Context context, View dialogView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null);
        Utils.setEditTextDialogTheme(builder);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * Sets up listeners for the color picker dialog buttons and color changes.
     *
     * @param dialog The AlertDialog instance.
     * @param colorPickerView The color picker view.
     * @param editText The EditText for color input.
     * @param originalColor The original color to restore on cancel.
     */
    private void setupColorPickerDialogListeners(AlertDialog dialog, CustomColorPickerView colorPickerView,
                                                 EditText editText, int originalColor) {
        dialog.setOnShowListener(d -> {
            colorPickerView.setOnColorChangedListener(color -> {
                String hexColor = String.format("#%06X", color & 0xFFFFFF);
                editText.setText(hexColor);
                editText.setSelection(hexColor.length());
            });

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String hexColor = String.format("#%06X", colorPickerView.getColor() & 0xFFFFFF);
                editText.setText(hexColor);
                editText.setSelection(hexColor.length());
                dialog.dismiss();
            });

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                String hexColor = String.format("#%06X", originalColor);
                editText.setText(hexColor);
                currentColor = originalColor;
                updateColorPreview();
                dialog.dismiss();
            });
        });
    }

    /**
     * Creates a TextWatcher to monitor changes in the EditText for color input.
     *
     * @return A TextWatcher that updates the color preview on valid input.
     */
    private TextWatcher createColorTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) return;
                try {
                    currentColor = Color.parseColor(s.toString()) & 0xFFFFFF;
                    updateColorPreview();
                } catch (IllegalArgumentException ignored) {
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
        builder.setView(createColorInputLayout(builder.getContext()));
        if (colorSetting != null) {
            builder.setNeutralButton(str("revanced_settings_reset"), null);
        }
    }

    /**
     * Configures the dialog with listeners for reset and save actions.
     *
     * @param state The state passed to the superclass.
     */
    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null || colorSetting == null) return;

        EditText editText = getEditText();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> resetToDefault(editText));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> saveColor(editText, dialog));
    }

    /**
     * Resets the color to the default value and updates the UI.
     *
     * @param editText The EditText to update with the default color.
     */
    private void resetToDefault(EditText editText) {
        try {
            String defaultValue = colorSetting.defaultValue;
            editText.setText(defaultValue);
            editText.setSelection(defaultValue.length());
            currentColor = Color.parseColor(defaultValue) & 0xFFFFFF;
            updateColorPreview();
        } catch (Exception ex) {
            Logger.printException(() -> "Reset color failure", ex);
        }
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
            setColor(colorString);
            dialog.dismiss();
        } catch (IllegalArgumentException ex) {
            Logger.printException(() -> "Invalid color format: " + colorString, ex);
        }
    }

    /**
     * Generates a string with a span tag for a colored dot.
     *
     * @param color The RGB color (without alpha).
     * @return A string with the span tag for the colored dot.
     */
    private static String getColorDotSpan(int color) {
        return String.format("<span style=\"color:#%06X; font-size:1.5em;\">⬤</span>", color & 0xFFFFFF);
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
        String combined = "⬤ " + originalTitle;
        SpannableString spannable = new SpannableString(combined);
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
