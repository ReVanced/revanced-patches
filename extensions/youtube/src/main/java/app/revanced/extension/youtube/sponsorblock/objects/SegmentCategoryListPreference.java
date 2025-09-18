package app.revanced.extension.youtube.sponsorblock.objects;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;
import static app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings.migrateOldColorString;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.settings.preference.ColorPickerView;
import app.revanced.extension.shared.ui.ColorDot;
import app.revanced.extension.shared.ui.CustomDialog;

@SuppressWarnings("deprecation")
public class SegmentCategoryListPreference extends ColorPickerPreference {
    public final SegmentCategory category;

    /**
     * Current category color in ARGB format (with alpha).
     */
    @ColorInt
    private int categoryColor;

    private int selectedDialogEntryIndex;

    /**
     * View displaying a colored dot in the widget area.
     */
    private View widgetColorDot;

    /**
     * Dialog View displaying a colored dot for the selected color preview in the dialog.
     */
    private View dialogColorDot;
    private EditText dialogColorEditText;
    private ColorPickerView dialogColorPickerView;
    private Dialog dialog;

    public SegmentCategoryListPreference(Context context, SegmentCategory category) {
        super(context);
        this.category = Objects.requireNonNull(category);

        // Set key to color setting for persistence.
        // Edit: Using preferences to sync together multiple pieces of code is messy and should be rethought.
        setKey(category.colorSetting.key);
        setTitle(category.title.toString());
        setSummary(category.description.toString());

        // Enable opacity slider for this preference.
        setOpacitySliderEnabled(true);

        setWidgetLayoutResource(LAYOUT_REVANCED_COLOR_DOT_WIDGET);
        updateUI();

        // Sync initial color from category.
        setText(category.getColorStringWithOpacity());
    }

    @Override
    public final void setText(String colorString) {
        try {
            // Migrate old data imported in the settings UI.
            // This migration is needed here because pasting into the settings
            // immediately syncs the data with the preferences.
            colorString = migrateOldColorString(colorString, SegmentCategory.CATEGORY_DEFAULT_OPACITY);
            super.setText(colorString);

            // Save to category.
            category.setColorWithOpacity(colorString);
            updateUI();

            // Notify the listener about the color change.
            if (colorChangeListener != null) {
                colorChangeListener.onColorChanged(getKey(), getDisplayColor());
            }
        } catch (IllegalArgumentException ex) {
            Utils.showToastShort(str("revanced_settings_color_invalid"));
            setText(category.colorSetting.defaultValue);
        } catch (Exception ex) {
            String colorStringFinal = colorString;
            Logger.printException(() -> "setText failure: " + colorStringFinal, ex);
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        try {
            Context context = getContext();
            categoryColor = category.getColorWithOpacity();

            // Find initial behavior index.
            final boolean isHighlightCategory = category == SegmentCategory.HIGHLIGHT;
            CharSequence[] entryValues = isHighlightCategory
                    ? CategoryBehaviour.getBehaviorKeyValuesWithoutSkipOnce()
                    : CategoryBehaviour.getBehaviorKeyValues();
            String currentBehavior = category.behaviorSetting.get();
            selectedDialogEntryIndex = -1;
            for (int i = 0, length = entryValues.length; i < length; i++) {
                if (entryValues[i].equals(currentBehavior)) {
                    selectedDialogEntryIndex = i;
                    break;
                }
            }

            // Create the main layout for the dialog content.
            LinearLayout contentLayout = new LinearLayout(context);
            contentLayout.setOrientation(LinearLayout.VERTICAL);

            // Add behavior selection radio buttons.
            RadioGroup radioGroup = new RadioGroup(context);
            radioGroup.setOrientation(RadioGroup.VERTICAL);
            CharSequence[] entries = isHighlightCategory
                    ? CategoryBehaviour.getBehaviorDescriptionsWithoutSkipOnce()
                    : CategoryBehaviour.getBehaviorDescriptions();
            for (int i = 0, length = entries.length; i < length; i++) {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(entries[i]);
                radioButton.setId(i);
                radioButton.setChecked(i == selectedDialogEntryIndex);
                radioGroup.addView(radioButton);
            }
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> selectedDialogEntryIndex = checkedId);
            radioGroup.setPadding(dipToPixels(10), 0, 0, 0);
            contentLayout.addView(radioGroup);

            // Inflate the color picker view with opacity slider enabled.
            View colorPickerContainer = LayoutInflater.from(context).inflate(LAYOUT_REVANCED_COLOR_PICKER, null);
            dialogColorPickerView = colorPickerContainer.findViewById(ID_REVANCED_COLOR_PICKER_VIEW);
            dialogColorPickerView.setOpacitySliderEnabled(true);
            dialogColorPickerView.setColor(categoryColor);
            contentLayout.addView(colorPickerContainer);

            // Horizontal layout for color input and preview.
            LinearLayout inputLayout = new LinearLayout(context);
            inputLayout.setOrientation(LinearLayout.HORIZONTAL);
            inputLayout.setGravity(Gravity.CENTER_VERTICAL);

            // Color dot preview.
            dialogColorDot = new View(context);
            LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                    dipToPixels(20),
                    dipToPixels(20)
            );
            previewParams.setMargins(dipToPixels(16), 0, dipToPixels(10), 0);
            dialogColorDot.setLayoutParams(previewParams);
            inputLayout.addView(dialogColorDot);
            updateDialogColorDot();

            // Color EditText.
            dialogColorEditText = new EditText(context);
            dialogColorEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                dialogColorEditText.setAutofillHints((String) null);
            }
            dialogColorEditText.setTypeface(Typeface.MONOSPACE);
            dialogColorEditText.setTextLocale(Locale.US);
            String currentColorString = getColorString(categoryColor, true);
            dialogColorEditText.setText(currentColorString);
            dialogColorEditText.setSelection(currentColorString.length());
            dialogColorEditText.addTextChangedListener(createColorTextWatcher(dialogColorPickerView));
            inputLayout.addView(dialogColorEditText);

            // Add a dummy view to take up remaining horizontal space.
            View paddingView = new View(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
            );
            paddingView.setLayoutParams(params);
            inputLayout.addView(paddingView);

            contentLayout.addView(inputLayout);

            // Create ScrollView to wrap the content layout.
            ScrollView contentScrollView = getScrollView(context);
            contentScrollView.addView(contentLayout);

            // Create the custom dialog.
            Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                    context,
                    category.title.toString(),
                    null,
                    null,
                    null,
                    () -> { // OK button action.
                        if (selectedDialogEntryIndex >= 0) {
                            String value = entryValues[selectedDialogEntryIndex].toString();
                            category.setBehaviour(Objects.requireNonNull(CategoryBehaviour.byReVancedKeyValue(value)));
                            SegmentCategory.updateEnabledCategories();
                        }

                        try {
                            String colorString = dialogColorEditText.getText().toString();
                            if (colorString.length() != COLOR_STRING_LENGTH_WITH_ALPHA) {
                                Utils.showToastShort(str("revanced_settings_color_invalid"));
                                return;
                            }
                            setText(colorString); // Save color.

                            updateUI();
                        } catch (IllegalArgumentException ex) {
                            Utils.showToastShort(str("revanced_settings_color_invalid"));
                        }
                    },
                    () -> {}, // Cancel button action (dismiss only).
                    str("revanced_settings_reset_color"), // Neutral button text.
                    () -> { // Neutral button action (Reset).
                        try {
                            // Reset to default color with default opacity.
                            int defaultColor = category.getDefaultColorWithOpacity();
                            dialogColorPickerView.setColor(defaultColor);
                        } catch (Exception ex) {
                            Logger.printException(() -> "resetButton onClick failure", ex);
                        }
                    },
                    false
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
                String hexColor = getColorString(color, true);
                Logger.printDebug(() -> "onColorChanged: " + hexColor);

                updateDialogColorDot();
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

    @NonNull
    private static ScrollView getScrollView(Context context) {
        ScrollView contentScrollView = new ScrollView(context);
        contentScrollView.setVerticalScrollBarEnabled(false); // Disable vertical scrollbar.
        contentScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER); // Disable overscroll effect.
        LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        contentScrollView.setLayoutParams(scrollViewParams);
        return contentScrollView;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // Nullify dialog references.
        dialogColorDot = null;
        dialogColorEditText = null;
        dialogColorPickerView = null;

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @ColorInt
    private int getDisplayColor() {
        return categoryColor;
    }

    public void updateUI() {
        try {
            categoryColor = category.getColorWithOpacity();

            if (category.behaviorSetting != null) {
                setEnabled(category.behaviorSetting.isAvailable());
            }

            updateWidgetColorDot();
        } catch (Exception ex) {
            Logger.printException(() -> "updateUI failure for category: " + category.keyValue, ex);
        }
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
                getDisplayColor(),
                widgetColorDot.isEnabled()
        );
    }

    /**
     * Updates the color preview View with a colored dot drawable.
     */
    private void updateDialogColorDot() {
        if (dialogColorDot == null) return;

        ColorDot.applyColorDot(
                dialogColorDot,
                categoryColor,
                true
        );
    }

    /**
     * Creates a TextWatcher to monitor changes in the EditText for color input with alpha support.
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
                    String sanitizedColorString = cleanupColorCodeString(colorString, true);

                    if (!sanitizedColorString.equals(colorString)) {
                        edit.replace(0, colorString.length(), sanitizedColorString);
                        return;
                    }

                    if (sanitizedColorString.length() != COLOR_STRING_LENGTH_WITH_ALPHA) {
                        // User is still typing out the color.
                        return;
                    }

                    final int newColor = Color.parseColor(colorString);
                    if (categoryColor != newColor) {
                        categoryColor = newColor;
                        updateDialogColorDot();
                        colorPickerView.setColor(newColor);
                    }
                } catch (Exception ex) {
                    // Should never be reached since input is validated before using.
                    Logger.printException(() -> "colorEditText afterTextChanged failure", ex);
                }
            }
        };
    }
}
