package app.revanced.extension.youtube.sponsorblock.objects;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings.migrateOldColorString;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ColorPickerPreference;
import app.revanced.extension.shared.ui.ColorDot;
import app.revanced.extension.shared.ui.Dim;

@SuppressWarnings("deprecation")
public class SegmentCategoryPreference extends ColorPickerPreference {
    public final SegmentCategory category;

    /**
     * View displaying a colored dot in the widget area.
     */
    private View widgetColorDot;

    // Fields to store dialog state for the OK button handler.
    private int selectedDialogEntryIndex;
    private CharSequence[] entryValues;


    public SegmentCategoryPreference(Context context, SegmentCategory category) {
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

        // Sync initial color from category.
        setText(category.getColorStringWithOpacity());
        updateUI();
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
                colorChangeListener.onColorChanged(getKey(), category.getColorWithOpacity());
            }
        } catch (IllegalArgumentException ex) {
            Utils.showToastShort(str("revanced_settings_color_invalid"));
            setText(category.colorSetting.defaultValue);
        } catch (Exception ex) {
            String colorStringFinal = colorString;
            Logger.printException(() -> "setText failure: " + colorStringFinal, ex);
        }
    }

    @Nullable
    @Override
    protected View createExtraDialogContentView(Context context) {
        final boolean isHighlightCategory = category == SegmentCategory.HIGHLIGHT;
        entryValues = isHighlightCategory
                ? CategoryBehaviour.getBehaviorKeyValuesWithoutSkipOnce()
                : CategoryBehaviour.getBehaviorKeyValues();

        String currentBehavior = category.behaviorSetting.get();
        selectedDialogEntryIndex = -1;
        for (int i = 0; i < entryValues.length; i++) {
            if (entryValues[i].equals(currentBehavior)) {
                selectedDialogEntryIndex = i;
                break;
            }
        }

        RadioGroup radioGroup = new RadioGroup(context);
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        CharSequence[] entries = isHighlightCategory
                ? CategoryBehaviour.getBehaviorDescriptionsWithoutSkipOnce()
                : CategoryBehaviour.getBehaviorDescriptions();

        for (int i = 0; i < entries.length; i++) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(entries[i]);
            radioButton.setId(i);
            radioButton.setChecked(i == selectedDialogEntryIndex);
            radioGroup.addView(radioButton);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> selectedDialogEntryIndex = checkedId);
        radioGroup.setPadding(Dim.dp10, 0, Dim.dp10, Dim.dp10);
        return radioGroup;
    }

    @Override
    protected void onDialogOkClicked() {
        if (selectedDialogEntryIndex >= 0 && entryValues != null) {
            String value = entryValues[selectedDialogEntryIndex].toString();
            category.setBehaviour(Objects.requireNonNull(CategoryBehaviour.byReVancedKeyValue(value)));
            SegmentCategory.updateEnabledCategories();
        }
    }

    @Override
    protected void onDialogNeutralClicked() {
        try {
            final int defaultColor = category.getDefaultColorWithOpacity();
            dialogColorPickerView.setColor(defaultColor);
        } catch (Exception ex) {
            Logger.printException(() -> "Reset button failure", ex);
        }
    }

    public void updateUI() {
        try {
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
                category.getColorWithOpacity(),
                isEnabled()
        );
    }
}
