package app.revanced.extension.youtube.sponsorblock.objects;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;
import static app.revanced.extension.shared.Utils.dipToPixels;
import static app.revanced.extension.shared.settings.preference.ColorPickerPreference.getColorString;
import static app.revanced.extension.youtube.sponsorblock.objects.SegmentCategory.applyOpacityToColor;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.ColorInt;

import java.util.Locale;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.settings.preference.ColorPickerView;
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("deprecation")
public class SegmentCategoryListPreference extends ListPreference {
    private final SegmentCategory category;

    /**
     * RGB format (no alpha).
     */
    @ColorInt
    private int categoryColor;
    /**
     * [0, 1]
     */
    private float categoryOpacity;
    private int selectedDialogEntryIndex;

    private TextView dialogColorDotView;
    private EditText dialogColorEditText;
    private EditText dialogOpacityEditText;
    private ColorPickerView dialogColorPickerView;
    private Dialog dialog;

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
    protected void showDialog(Bundle state) {
        try {
            Context context = getContext();
            categoryColor = category.getColorNoOpacity();
            categoryOpacity = category.getOpacity();
            selectedDialogEntryIndex = findIndexOfValue(getValue());

            // Create the main layout for the dialog content.
            LinearLayout contentLayout = new LinearLayout(context);
            contentLayout.setOrientation(LinearLayout.VERTICAL);

            // Add behavior selection radio buttons.
            RadioGroup radioGroup = new RadioGroup(context);
            radioGroup.setOrientation(RadioGroup.VERTICAL);
            CharSequence[] entries = getEntries();
            for (int i = 0; i < entries.length; i++) {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(entries[i]);
                radioButton.setId(i);
                radioButton.setChecked(i == selectedDialogEntryIndex);
                radioGroup.addView(radioButton);
            }
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> selectedDialogEntryIndex = checkedId);
            radioGroup.setPadding(dipToPixels(10), 0, 0, 0);
            contentLayout.addView(radioGroup);

            // Inflate the color picker view.
            View colorPickerContainer = LayoutInflater.from(context)
                    .inflate(getResourceIdentifier("revanced_color_picker", "layout"), null);
            dialogColorPickerView = colorPickerContainer.findViewById(
                    getResourceIdentifier("revanced_color_picker_view", "id"));
            dialogColorPickerView.setColor(categoryColor);
            contentLayout.addView(colorPickerContainer);

            // Grid layout for color and opacity inputs.
            GridLayout gridLayout = new GridLayout(context);
            gridLayout.setColumnCount(3);
            gridLayout.setRowCount(2);
            gridLayout.setPadding(dipToPixels(16), 0, 0, 0);

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
            gridParams.setMargins(0, 0, dipToPixels(10), 0);
            dialogColorDotView = new TextView(context);
            dialogColorDotView.setLayoutParams(gridParams);
            gridLayout.addView(dialogColorDotView);
            updateCategoryColorDot();

            gridParams = new GridLayout.LayoutParams();
            gridParams.rowSpec = GridLayout.spec(0); // First row.
            gridParams.columnSpec = GridLayout.spec(2); // Third column.
            dialogColorEditText = new EditText(context);
            dialogColorEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            dialogColorEditText.setAutofillHints((String) null);
            dialogColorEditText.setTypeface(Typeface.MONOSPACE);
            dialogColorEditText.setTextLocale(Locale.US);
            dialogColorEditText.setText(getColorString(categoryColor));
            dialogColorEditText.addTextChangedListener(new TextWatcher() {
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
                        String normalizedColorString = ColorPickerPreference.cleanupColorCodeString(colorString);

                        if (!normalizedColorString.equals(colorString)) {
                            edit.replace(0, colorString.length(), normalizedColorString);
                            return;
                        }

                        if (normalizedColorString.length() != ColorPickerPreference.COLOR_STRING_LENGTH) {
                            // User is still typing out the color.
                            return;
                        }

                        // Remove the alpha channel.
                        final int newColor = Color.parseColor(colorString) & 0x00FFFFFF;
                        // Changing view color causes callback into this class.
                        dialogColorPickerView.setColor(newColor);
                    } catch (Exception ex) {
                        // Should never be reached since input is validated before using.
                        Logger.printException(() -> "colorEditText afterTextChanged failure", ex);
                    }
                }
            });
            gridLayout.addView(dialogColorEditText, gridParams);

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
            dialogOpacityEditText = new EditText(context);
            dialogOpacityEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            dialogOpacityEditText.setAutofillHints((String) null);
            dialogOpacityEditText.setTypeface(Typeface.MONOSPACE);
            dialogOpacityEditText.setTextLocale(Locale.US);
            dialogOpacityEditText.addTextChangedListener(new TextWatcher() {
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
                    } catch (Exception ex) {
                        // Should never happen.
                        Logger.printException(() -> "opacityEditText afterTextChanged failure", ex);
                    }
                }
            });
            gridLayout.addView(dialogOpacityEditText, gridParams);
            updateOpacityText();

            contentLayout.addView(gridLayout);

            // Create ScrollView to wrap the content layout.
            ScrollView contentScrollView = new ScrollView(context);
            contentScrollView.setVerticalScrollBarEnabled(false); // Disable vertical scrollbar.
            contentScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER); // Disable overscroll effect.
            LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            );
            contentScrollView.setLayoutParams(scrollViewParams);
            contentScrollView.addView(contentLayout);

            // Create the custom dialog.
            Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                    context,
                    category.title.toString(), // Title.
                    null, // No message (replaced by contentLayout).
                    null, // No EditText.
                    null, // OK button text.
                    () -> {
                        // OK button action.
                        if (selectedDialogEntryIndex >= 0 && getEntryValues() != null) {
                            String value = getEntryValues()[selectedDialogEntryIndex].toString();
                            if (callChangeListener(value)) {
                                setValue(value);
                                category.setBehaviour(Objects.requireNonNull(CategoryBehaviour.byReVancedKeyValue(value)));
                                SegmentCategory.updateEnabledCategories();
                            }

                            try {
                                category.setColor(dialogColorEditText.getText().toString());
                                category.setOpacity(categoryOpacity);
                            } catch (IllegalArgumentException ex) {
                                Utils.showToastShort(str("revanced_settings_color_invalid"));
                            }

                            updateUI();
                        }
                    },
                    () -> {}, // Cancel button action (dismiss only).
                    str("revanced_settings_reset_color"), // Neutral button text.
                    () -> {
                        // Neutral button action (Reset).
                        try {
                            // Setting view color causes callback to update the UI.
                            dialogColorPickerView.setColor(category.getColorNoOpacityDefault());

                            categoryOpacity = category.getOpacityDefault();
                            updateOpacityText();
                        } catch (Exception ex) {
                            Logger.printException(() -> "resetButton onClick failure", ex);
                        }
                    },
                    false // Do not dismiss dialog on Neutral button click.
            );

            // Add the ScrollView to the dialog's main layout.
            LinearLayout dialogMainLayout = dialogPair.second;
            dialogMainLayout.addView(contentScrollView, dialogMainLayout.getChildCount() - 1);

            // Set up color picker listener.
            // Do last to prevent listener callbacks while setting up view.
            dialogColorPickerView.setOnColorChangedListener(color -> {
                if (categoryColor == color) {
                    return;
                }
                categoryColor = color;
                String hexColor = getColorString(color);
                Logger.printDebug(() -> "onColorChanged: " + hexColor);

                updateCategoryColorDot();
                dialogColorEditText.setText(hexColor);
                dialogColorEditText.setSelection(hexColor.length());
            });

            // Show the dialog.
            dialog = dialogPair.first;
            dialog.show();
        } catch (Exception ex) {
            Logger.printException(() -> "showDialog failure", ex);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // Nullify dialog references.
        dialogColorDotView = null;
        dialogColorEditText = null;
        dialogOpacityEditText = null;
        dialogColorPickerView = null;

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @ColorInt
    private int applyOpacityToCategoryColor() {
        return applyOpacityToColor(categoryColor, categoryOpacity);
    }

    public void updateUI() {
        try {
            categoryColor = category.getColorNoOpacity();
            categoryOpacity = category.getOpacity();

            setTitle(category.getTitleWithColorDot(applyOpacityToCategoryColor()));

            Setting<String> behaviorSetting = getCategoryBehaviorSetting();

            if (behaviorSetting != null) {
                setEnabled(behaviorSetting.isAvailable());
            }

        } catch (Exception ex) {
            Logger.printException(() -> "updateUI failure for category: " + category.keyValue, ex);
        }
    }

    /**
     * Returns the Setting object for this category's behavior.
     * This allows the UI to check availability automatically.
     */
    private Setting<String> getCategoryBehaviorSetting() {
        // Map category to its corresponding setting.
        return switch (category.keyValue) {
            case "sponsor" -> Settings.SB_CATEGORY_SPONSOR;
            case "selfpromo" -> Settings.SB_CATEGORY_SELF_PROMO;
            case "interaction" -> Settings.SB_CATEGORY_INTERACTION;
            case "poi_highlight" -> Settings.SB_CATEGORY_HIGHLIGHT;
            case "intro" -> Settings.SB_CATEGORY_INTRO;
            case "outro" -> Settings.SB_CATEGORY_OUTRO;
            case "preview" -> Settings.SB_CATEGORY_PREVIEW;
            case "filler" -> Settings.SB_CATEGORY_FILLER;
            case "music_offtopic" -> Settings.SB_CATEGORY_MUSIC_OFFTOPIC;
            case "unsubmitted" -> Settings.SB_CATEGORY_UNSUBMITTED;
            default -> null;
        };
    }

    private void updateCategoryColorDot() {
        dialogColorDotView.setText(SegmentCategory.getCategoryColorDot(applyOpacityToCategoryColor()));
    }

    private void updateOpacityText() {
        dialogOpacityEditText.setText(String.format(Locale.US, "%.2f", categoryOpacity));
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
