package app.revanced.extension.youtube.sponsorblock.objects;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;
import static app.revanced.extension.youtube.sponsorblock.objects.SegmentCategory.applyOpacityToColor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.preference.ListPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Objects;

import app.revanced.extension.shared.settings.preference.CustomColorPickerView;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("deprecation")
public class SegmentCategoryListPreference extends ListPreference {
    private final SegmentCategory category;
    private TextView colorDotView;
    private EditText colorEditText;
    private EditText opacityEditText;
    /**
     * #RRGGBB
     */
    private int categoryColor;
    /**
     * [0, 1]
     */
    private float categoryOpacity;
    private int selectedDialogEntryIndex;

    public SegmentCategoryListPreference(Context context, SegmentCategory category) {
        super(context);
        this.category = Objects.requireNonNull(category);

        // Edit: Using preferences to sync together multiple pieces
        // of code is messy and should be rethought.
        setKey(category.behaviorSetting.key);
        setDefaultValue(category.behaviorSetting.defaultValue);

        final boolean isHighlightCategory = category == SegmentCategory.HIGHLIGHT;
        setEntries(isHighlightCategory
                ? CategoryBehaviour.getBehaviorDescriptionsWithoutSkipOnce()
                : CategoryBehaviour.getBehaviorDescriptions());
        setEntryValues(isHighlightCategory
                ? CategoryBehaviour.getBehaviorKeyValuesWithoutSkipOnce()
                : CategoryBehaviour.getBehaviorKeyValues());
        setSummary(category.description.toString());

        updateUI();
    }

    /**
     * Displays a color picker dialog for selecting a color.
     *
     * @param context The context for creating the dialog.
     */
    private void showColorPickerDialog(Context context) {
        // Store the original color in case the user cancels the dialog.
        final int originalColor = Color.parseColor(category.getColorString()) & 0xFFFFFF;

        // Parse the initial color from the EditText, falling back to originalColor if parsing fails.
        int initialColor = parseInitialColor(colorEditText.getText().toString(), originalColor);

        // Create a layout container for the color picker dialog view.
        RelativeLayout layout = new RelativeLayout(context);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        // Inflate the color picker view into the layout.
        View dialogView = LayoutInflater.from(context)
                .inflate(getResourceIdentifier("revanced_color_picker", "layout"), layout);

        // Get the custom color picker view from the inflated layout.
        CustomColorPickerView colorPickerView = dialogView.findViewById(
                getResourceIdentifier("color_picker_view", "id"));
        colorPickerView.setInitialColor(initialColor);

        // Create and configure the AlertDialog.
        AlertDialog dialog = createColorPickerDialog(context, dialogView);

        // Set up the dialog's positive/negative button behavior and color change listener.
        setupColorPickerDialogListeners(dialog, colorPickerView, colorEditText, originalColor);

        // Show the dialog.
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

        // Apply dynamic theme styling to the dialog.
        Utils.setEditTextDialogTheme(builder);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // Prevent accidental dismiss.
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
            // Update EditText as the user selects a color.
            colorPickerView.setOnColorChangedListener(color -> {
                String hexColor = String.format("#%06X", color & 0xFFFFFF);
                editText.setText(hexColor);
                editText.setSelection(hexColor.length());
            });

            // Handle OK button click: apply selected color.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String hexColor = String.format("#%06X", colorPickerView.getColor() & 0xFFFFFF);
                editText.setText(hexColor);
                editText.setSelection(hexColor.length());
                try {
                    categoryColor = Color.parseColor(hexColor) & 0xFFFFFF;
                    updateCategoryColorDot();
                } catch (IllegalArgumentException ex) {
                    Logger.printException(() -> "Invalid color: " + hexColor, ex);
                }
                dialog.dismiss();
            });

            // Handle Cancel button click: revert to original color.
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                String hexColor = String.format("#%06X", originalColor);
                editText.setText(hexColor);
                categoryColor = originalColor;
                updateCategoryColorDot();
                dialog.dismiss();
            });
        });
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        try {
            Utils.setEditTextDialogTheme(builder);

            categoryColor = category.getColorNoOpacity();
            categoryOpacity = category.getOpacity();

            Context context = builder.getContext();
            GridLayout gridLayout = new GridLayout(context);
            gridLayout.setPadding(70, 0, 70, 0); // Padding for the entire layout.
            gridLayout.setColumnCount(3);
            gridLayout.setRowCount(2);

            GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.rowSpec = GridLayout.spec(0); // First row.
            gridParams.columnSpec = GridLayout.spec(0); // First column.
            TextView colorTextLabel = new TextView(context);
            colorTextLabel.setText(str("revanced_sb_color_dot_label"));
            colorTextLabel.setLayoutParams(gridParams);
            gridLayout.addView(colorTextLabel);

            gridParams = new GridLayout.LayoutParams();
            gridParams.rowSpec = GridLayout.spec(0); // First row.
            gridParams.columnSpec = GridLayout.spec(1); // Second column.
            gridParams.setMargins(0, 0, 10, 0);
            colorDotView = new TextView(context);
            colorDotView.setLayoutParams(gridParams);
            colorDotView.setOnClickListener(v -> showColorPickerDialog(context));
            gridLayout.addView(colorDotView);
            updateCategoryColorDot();

            gridParams = new GridLayout.LayoutParams();
            gridParams.rowSpec = GridLayout.spec(0); // First row.
            gridParams.columnSpec = GridLayout.spec(2); // Third column.
            colorEditText = new EditText(context);
            colorEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            colorEditText.setTextLocale(Locale.US);
            colorEditText.setText(category.getColorString());
            colorEditText.addTextChangedListener(new TextWatcher() {
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

                        categoryColor = Color.parseColor(colorString);
                        updateCategoryColorDot();
                    } catch (IllegalArgumentException ex) {
                        // Ignore.
                    }
                }
            });
            colorEditText.setLayoutParams(gridParams);
            gridLayout.addView(colorEditText);

            gridParams = new GridLayout.LayoutParams();
            gridParams.rowSpec = GridLayout.spec(1); // Second row.
            gridParams.columnSpec = GridLayout.spec(0, 1); // First and second column.
            TextView opacityLabel = new TextView(context);
            opacityLabel.setText(str("revanced_sb_color_opacity_label"));
            opacityLabel.setLayoutParams(gridParams);
            gridLayout.addView(opacityLabel);

            gridParams = new GridLayout.LayoutParams();
            gridParams.rowSpec = GridLayout.spec(1); // Second row.
            gridParams.columnSpec = GridLayout.spec(2); // Third column.
            opacityEditText = new EditText(context);
            opacityEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            opacityEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable edit) {
                    try {
                        String editString = edit.toString();
                        final int opacityStringLength = editString.length();

                        final int maxOpacityStringLength = 4; // [0.00, 1.00]
                        if (opacityStringLength > maxOpacityStringLength) {
                            edit.delete(maxOpacityStringLength, opacityStringLength);
                            return;
                        }

                        final float opacity = opacityStringLength == 0
                                ? 0
                                : Float.parseFloat(editString);
                        if (opacity < 0) {
                            categoryOpacity = 0;
                            edit.replace(0, opacityStringLength, "0");
                            return;
                        } else if (opacity > 1.0f) {
                            categoryOpacity = 1;
                            edit.replace(0, opacityStringLength, "1.0");
                            return;
                        } else if (!editString.endsWith(".")) {
                            // Ignore "0." and "1." until the user finishes entering a valid number.
                            categoryOpacity = opacity;
                        }

                        updateCategoryColorDot();
                    } catch (NumberFormatException ex) {
                        // Should never happen.
                        Logger.printException(() -> "Could not parse opacity string", ex);
                    }
                }
            });
            opacityEditText.setLayoutParams(gridParams);
            gridLayout.addView(opacityEditText);
            updateOpacityText();

            builder.setView(gridLayout);
            builder.setTitle(category.title.toString());

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            });
            builder.setNeutralButton(str("revanced_sb_reset_color"), (dialog, which) -> {
                try {
                    category.resetColorAndOpacity();
                    updateUI();
                    Utils.showToastShort(str("revanced_sb_color_reset"));
                } catch (Exception ex) {
                    Logger.printException(() -> "setNeutralButton failure", ex);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            selectedDialogEntryIndex = findIndexOfValue(getValue());
            builder.setSingleChoiceItems(getEntries(), selectedDialogEntryIndex,
                    (dialog, which) -> selectedDialogEntryIndex = which);
        } catch (Exception ex) {
            Logger.printException(() -> "onPrepareDialogBuilder failure", ex);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        try {
            if (positiveResult && selectedDialogEntryIndex >= 0 && getEntryValues() != null) {
                String value = getEntryValues()[selectedDialogEntryIndex].toString();
                if (callChangeListener(value)) {
                    setValue(value);
                    category.setBehaviour(Objects.requireNonNull(CategoryBehaviour.byReVancedKeyValue(value)));
                    SegmentCategory.updateEnabledCategories();
                }

                try {
                    String colorString = colorEditText.getText().toString();
                    if (!colorString.equals(category.getColorString()) || categoryOpacity != category.getOpacity()) {
                        category.setColor(colorString);
                        category.setOpacity(categoryOpacity);
                        Utils.showToastShort(str("revanced_sb_color_changed"));
                    }
                } catch (IllegalArgumentException ex) {
                    Utils.showToastShort(str("revanced_sb_color_invalid"));
                }

                updateUI();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onDialogClosed failure", ex);
        }
    }

    private void applyOpacityToCategoryColor() {
        categoryColor = applyOpacityToColor(categoryColor, categoryOpacity);
    }

    public void updateUI() {
        categoryColor = category.getColorNoOpacity();
        categoryOpacity = category.getOpacity();
        applyOpacityToCategoryColor();

        setTitle(category.getTitleWithColorDot(categoryColor));
    }

    private void updateCategoryColorDot() {
        applyOpacityToCategoryColor();

        colorDotView.setText(SegmentCategory.getCategoryColorDot(categoryColor));
    }

    private void updateOpacityText() {
        opacityEditText.setText(String.format(Locale.US, "%.2f", categoryOpacity));
    }
}
