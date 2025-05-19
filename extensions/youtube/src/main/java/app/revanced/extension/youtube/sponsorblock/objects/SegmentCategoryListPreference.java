package app.revanced.extension.youtube.sponsorblock.objects;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;
import static app.revanced.extension.youtube.sponsorblock.objects.SegmentCategory.applyOpacityToColor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.ListPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Objects;

import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.settings.preference.CustomColorPickerView;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("deprecation")
public class SegmentCategoryListPreference extends ListPreference {
    private final SegmentCategory category;
    private TextView colorDotView;
    private EditText colorEditText;
    private EditText opacityEditText;
    private CustomColorPickerView colorPickerView;
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
        super.setSummary(category.description.toString());

        updateUI();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        try {
            Utils.setEditTextDialogTheme(builder);

            categoryColor = category.getColorNoOpacity();
            categoryOpacity = category.getOpacity();

            Context context = builder.getContext();
            LinearLayout mainLayout = new LinearLayout(context);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(70, 0, 70, 0);

            // Inflate the color picker view.
            View colorPickerContainer = LayoutInflater.from(context)
                    .inflate(getResourceIdentifier("revanced_color_picker", "layout"), null);
            colorPickerView = colorPickerContainer.findViewById(
                    getResourceIdentifier("color_picker_view", "id"));
            colorPickerView.setColor(categoryColor);
            colorPickerView.updateSelectedColor();
            mainLayout.addView(colorPickerContainer);

            // Grid layout for color and opacity inputs.
            GridLayout gridLayout = new GridLayout(context);
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
            gridLayout.addView(colorDotView);
            updateCategoryColorDot();

            gridParams = new GridLayout.LayoutParams();
            gridParams.rowSpec = GridLayout.spec(0); // First row.
            gridParams.columnSpec = GridLayout.spec(2); // Third column.
            colorEditText = new EditText(context);
            colorEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            colorEditText.setAutofillHints((String) null);
            colorEditText.setTypeface(Typeface.MONOSPACE);
            colorEditText.setTextLocale(Locale.US);
            colorEditText.setText(String.format("#%06X", categoryColor & 0xFFFFFF));
            colorEditText.addTextChangedListener(new TextWatcher() {
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

                        String normalizedColorString = ColorPickerPreference.cleanupColorCodeString(colorString);
                        if (!normalizedColorString.equals(colorString)) {
                            edit.replace(0, colorString.length(), normalizedColorString);
                            return;
                        }

                        if (normalizedColorString.length() != ColorPickerPreference.COLOR_STRING_LENGTH) {
                            // User is still typing out the color.
                            return;
                        }

                        int newColor = Color.parseColor(colorString);
                        categoryColor = newColor & 0xFFFFFF;
                        updateCategoryColorDot();
                        colorPickerView.setColor(newColor);
                    } catch (IllegalArgumentException ex) {
                        // Should never be reached since input is validated before using.
                        Logger.printException(() -> "afterTextChanged bad color: " + colorString, ex);
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
            opacityEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            opacityEditText.setAutofillHints((String) null);
            opacityEditText.setTypeface(Typeface.MONOSPACE);
            opacityEditText.setTextLocale(Locale.US);
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

            mainLayout.addView(gridLayout);

            // Set up color picker listener.
            colorPickerView.setOnColorChangedListener(color -> {
                String hexColor = String.format("#%06X", color & 0xFFFFFF);
                colorEditText.setText(hexColor);
                colorEditText.setSelection(hexColor.length());
                categoryColor = color & 0xFFFFFF;
                updateCategoryColorDot();
            });

            builder.setView(mainLayout);
            builder.setTitle(category.title.toString());

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            });
            builder.setNeutralButton(str("revanced_settings_reset_color"), (dialog, which) -> {
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
                    Utils.showToastShort(str("revanced_settings_color_invalid"));
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

    @Override
    public void setSummary(CharSequence summary) {
        // Ignore calls to set the summary.
        // Summary is always the description of the category.
        //
        // This is required otherwise the ReVanced preference fragment
        // sets all ListPreference summaries to show the current selection.
    }
}
