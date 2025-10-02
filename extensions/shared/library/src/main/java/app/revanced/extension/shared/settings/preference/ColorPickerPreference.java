package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;
import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.shared.ui.ColorDot;
import app.revanced.extension.shared.ui.CustomDialog;

/**
 * A custom preference for selecting a color via a hexadecimal code or a color picker dialog.
 * Extends {@link EditTextPreference} to display a colored dot in the widget area,
 * reflecting the currently selected color. The dot is dimmed when the preference is disabled.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ColorPickerPreference extends EditTextPreference {
    /** Length of a valid color string of format #RRGGBB (without alpha) or #AARRGGBB (with alpha). */
    public static final int COLOR_STRING_LENGTH_WITHOUT_ALPHA = 7;
    public static final int COLOR_STRING_LENGTH_WITH_ALPHA = 9;

    /** Matches everything that is not a hex number/letter. */
    private static final Pattern PATTERN_NOT_HEX = Pattern.compile("[^0-9A-Fa-f]");

    /** Alpha for dimming when the preference is disabled. */
    public static final float DISABLED_ALPHA = 0.5f; // 50%

    /** View displaying a colored dot in the widget area. */
    private View widgetColorDot;

    /** Dialog View displaying a colored dot for the selected color preview in the dialog. */
    private View dialogColorDot;

    /** Current color, including alpha channel if opacity slider is enabled. */
    @ColorInt
    private int currentColor;

    /** Associated setting for storing the color value. */
    private StringSetting colorSetting;

    /** Dialog TextWatcher for the EditText to monitor color input changes. */
    private TextWatcher colorTextWatcher;

    /** Dialog color picker view. */
    protected ColorPickerView dialogColorPickerView;

    /** Listener for color changes. */
    protected OnColorChangeListener colorChangeListener;

    /** Whether the opacity slider is enabled. */
    private boolean opacitySliderEnabled = false;

    public static final int ID_REVANCED_COLOR_PICKER_VIEW =
            getResourceIdentifierOrThrow("revanced_color_picker_view", "id");
    public static final int ID_PREFERENCE_COLOR_DOT =
            getResourceIdentifierOrThrow("preference_color_dot", "id");
    public static final int LAYOUT_REVANCED_COLOR_DOT_WIDGET =
            getResourceIdentifierOrThrow("revanced_color_dot_widget", "layout");
    public static final int LAYOUT_REVANCED_COLOR_PICKER =
            getResourceIdentifierOrThrow("revanced_color_picker", "layout");

    /**
     * Removes non valid hex characters, converts to all uppercase,
     * and adds # character to the start if not present.
     */
    public static String cleanupColorCodeString(String colorString, boolean includeAlpha) {
        String result = "#" + PATTERN_NOT_HEX.matcher(colorString)
                .replaceAll("").toUpperCase(Locale.ROOT);

        int maxLength = includeAlpha ? COLOR_STRING_LENGTH_WITH_ALPHA : COLOR_STRING_LENGTH_WITHOUT_ALPHA;
        if (result.length() < maxLength) {
            return result;
        }

        return result.substring(0, maxLength);
    }

    /**
     * @param color Color, with or without alpha channel.
     * @param includeAlpha Whether to include the alpha channel in the output string.
     * @return #RRGGBB or #AARRGGBB hex color string
     */
    public static String getColorString(@ColorInt int color, boolean includeAlpha) {
        if (includeAlpha) {
            return String.format("#%08X", color);
        }
        color = color & 0x00FFFFFF; // Mask to strip alpha.
        return String.format("#%06X", color);
    }

    /**
     * Interface for notifying color changes.
     */
    public interface OnColorChangeListener {
        void onColorChanged(String key, int newColor);
    }

    /**
     * Sets the listener for color changes.
     */
    public void setOnColorChangeListener(OnColorChangeListener listener) {
        this.colorChangeListener = listener;
    }

    /**
     * Enables or disables the opacity slider in the color picker dialog.
     */
    public void setOpacitySliderEnabled(boolean enabled) {
        this.opacitySliderEnabled = enabled;
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
        if (getKey() != null) {
            colorSetting = (StringSetting) Setting.getSettingFromPath(getKey());
            if (colorSetting == null) {
                Logger.printException(() -> "Could not find color setting for: " + getKey());
            }
        } else {
            Logger.printDebug(() -> "initialized without key, settings will be loaded later");
        }

        EditText editText = getEditText();
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editText.setAutofillHints((String) null);
        }

        // Set the widget layout to a custom layout containing the colored dot.
        setWidgetLayoutResource(LAYOUT_REVANCED_COLOR_DOT_WIDGET);
    }

    /**
     * Sets the selected color and updates the UI and settings.
     */
    @Override
    public void setText(String colorString) {
        try {
            Logger.printDebug(() -> "setText: " + colorString);
            super.setText(colorString);

            currentColor = Color.parseColor(colorString);
            if (colorSetting != null) {
                colorSetting.save(getColorString(currentColor, opacitySliderEnabled));
            }
            updateDialogColorDot();
            updateWidgetColorDot();

            // Notify the listener about the color change.
            if (colorChangeListener != null) {
                colorChangeListener.onColorChanged(getKey(), currentColor);
            }
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

    /**
     * Creates a TextWatcher to monitor changes in the EditText for color input.
     */
    private TextWatcher createColorTextWatcher(ColorPickerView colorPickerView) {
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
                    String sanitizedColorString = cleanupColorCodeString(colorString, opacitySliderEnabled);
                    if (!sanitizedColorString.equals(colorString)) {
                        edit.replace(0, colorString.length(), sanitizedColorString);
                        return;
                    }

                    int expectedLength = opacitySliderEnabled
                            ? COLOR_STRING_LENGTH_WITH_ALPHA
                            : COLOR_STRING_LENGTH_WITHOUT_ALPHA;
                    if (sanitizedColorString.length() != expectedLength) {
                        return;
                    }

                    final int newColor = Color.parseColor(colorString);
                    if (currentColor != newColor) {
                        Logger.printDebug(() -> "afterTextChanged: " + sanitizedColorString);
                        currentColor = newColor;
                        updateDialogColorDot();
                        updateWidgetColorDot();
                        colorPickerView.setColor(newColor);
                    }
                } catch (Exception ex) {
                    // Should never be reached since input is validated before using.
                    Logger.printException(() -> "afterTextChanged failure", ex);
                }
            }
        };
    }

    /**
     * Hook for subclasses to add a custom view to the top of the dialog.
     */
    @Nullable
    protected View createExtraDialogContentView(Context context) {
        return null; // Default implementation returns no extra view.
    }

    /**
     * Hook for subclasses to handle the OK button click.
     */
    protected void onDialogOkClicked() {
        // Default implementation does nothing.
    }

    /**
     * Hook for subclasses to handle the Neutral button click.
     */
    protected void onDialogNeutralClicked() {
        // Default implementation.
        try {
            final int defaultColor = Color.parseColor(colorSetting.defaultValue);
            dialogColorPickerView.setColor(defaultColor);
        } catch (Exception ex) {
            Logger.printException(() -> "Reset button failure", ex);
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        Context context = getContext();

        // Create content container for all dialog views.
        LinearLayout contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.VERTICAL);

        // Add extra view from subclass if it exists.
        View extraView = createExtraDialogContentView(context);
        if (extraView != null) {
            contentContainer.addView(extraView);
        }

        // Inflate color picker view.
        View colorPicker = LayoutInflater.from(context).inflate(LAYOUT_REVANCED_COLOR_PICKER, null);
        dialogColorPickerView = colorPicker.findViewById(ID_REVANCED_COLOR_PICKER_VIEW);
        dialogColorPickerView.setOpacitySliderEnabled(opacitySliderEnabled);
        dialogColorPickerView.setColor(currentColor);
        contentContainer.addView(colorPicker);

        // Horizontal layout for preview and EditText.
        LinearLayout inputLayout = new LinearLayout(context);
        inputLayout.setOrientation(LinearLayout.HORIZONTAL);
        inputLayout.setGravity(Gravity.CENTER_VERTICAL);

        dialogColorDot = new View(context);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                dipToPixels(20),
                dipToPixels(20)
        );
        previewParams.setMargins(dipToPixels(16), 0, dipToPixels(10), 0);
        dialogColorDot.setLayoutParams(previewParams);
        inputLayout.addView(dialogColorDot);
        updateDialogColorDot();

        EditText editText = getEditText();
        ViewParent parent = editText.getParent();
        if (parent instanceof ViewGroup parentViewGroup) {
            parentViewGroup.removeView(editText);
        }
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        String currentColorString = getColorString(currentColor, opacitySliderEnabled);
        editText.setText(currentColorString);
        editText.setSelection(currentColorString.length());
        editText.setTypeface(Typeface.MONOSPACE);
        colorTextWatcher = createColorTextWatcher(dialogColorPickerView);
        editText.addTextChangedListener(colorTextWatcher);
        inputLayout.addView(editText);

        // Add a dummy view to take up remaining horizontal space,
        // otherwise it will show an oversize underlined text view.
        View paddingView = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        );
        paddingView.setLayoutParams(params);
        inputLayout.addView(paddingView);

        contentContainer.addView(inputLayout);

        // Create ScrollView to wrap the content container.
        ScrollView contentScrollView = new ScrollView(context);
        contentScrollView.setVerticalScrollBarEnabled(false);
        contentScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        contentScrollView.setLayoutParams(scrollViewParams);
        contentScrollView.addView(contentContainer);

        final int originalColor = currentColor;
        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                context,
                getTitle() != null ? getTitle().toString() : str("revanced_settings_color_picker_title"),
                null,
                null,
                null,
                () -> { // OK button action.
                    try {
                        String colorString = editText.getText().toString();
                        int expectedLength = opacitySliderEnabled
                                ? COLOR_STRING_LENGTH_WITH_ALPHA
                                : COLOR_STRING_LENGTH_WITHOUT_ALPHA;
                        if (colorString.length() != expectedLength) {
                            Utils.showToastShort(str("revanced_settings_color_invalid"));
                            setText(getColorString(originalColor, opacitySliderEnabled));
                            return;
                        }
                        setText(colorString);

                        onDialogOkClicked();
                    } catch (Exception ex) {
                        // Should never happen due to a bad color string,
                        // since the text is validated and fixed while the user types.
                        Logger.printException(() -> "OK button failure", ex);
                    }
                },
                () -> { // Cancel button action.
                    try {
                        setText(getColorString(originalColor, opacitySliderEnabled));
                    } catch (Exception ex) {
                        Logger.printException(() -> "Cancel button failure", ex);
                    }
                },
                str("revanced_settings_reset_color"), // Neutral button text.
                this::onDialogNeutralClicked, // Neutral button action.
                false // Do not dismiss dialog.
        );

        // Add the ScrollView to the dialog's main layout.
        LinearLayout dialogMainLayout = dialogPair.second;
        dialogMainLayout.addView(contentScrollView, dialogMainLayout.getChildCount() - 1);

        // Set up color picker listener with debouncing.
        // Add listener last to prevent callbacks from set calls above.
        dialogColorPickerView.setOnColorChangedListener(color -> {
            // Check if it actually changed, since this callback
            // can be caused by updates in afterTextChanged().
            if (currentColor == color) {
                return;
            }

            String updatedColorString = getColorString(color, opacitySliderEnabled);
            Logger.printDebug(() -> "onColorChanged: " + updatedColorString);
            currentColor = color;
            editText.setText(updatedColorString);
            editText.setSelection(updatedColorString.length());

            updateDialogColorDot();
            updateWidgetColorDot();
        });

        // Configure and show the dialog.
        Dialog dialog = dialogPair.first;
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (colorTextWatcher != null) {
            getEditText().removeTextChangedListener(colorTextWatcher);
            colorTextWatcher = null;
        }

        dialogColorDot = null;
        dialogColorPickerView = null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateWidgetColorDot();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        widgetColorDot = view.findViewById(ID_PREFERENCE_COLOR_DOT);
        updateWidgetColorDot();
    }

    private void updateWidgetColorDot() {
        if (widgetColorDot == null) return;

        ColorDot.applyColorDot(
                widgetColorDot,
                currentColor,
                widgetColorDot.isEnabled()
        );
    }

    private void updateDialogColorDot() {
        if (dialogColorDot == null) return;

        ColorDot.applyColorDot(
                dialogColorDot,
                currentColor,
                true
        );
    }
}
