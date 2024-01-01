package app.revanced.integrations.youtube.sponsorblock.objects;

import static app.revanced.integrations.shared.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.preference.ListPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Objects;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

public class SegmentCategoryListPreference extends ListPreference {
    private final SegmentCategory category;
    private EditText mEditText;
    private int mClickedDialogEntryIndex;

    public SegmentCategoryListPreference(Context context, SegmentCategory category) {
        super(context);
        final boolean isHighlightCategory = category == SegmentCategory.HIGHLIGHT;
        this.category = Objects.requireNonNull(category);
        setKey(category.keyValue);
        setDefaultValue(category.behaviour.reVancedKeyValue);
        setEntries(isHighlightCategory
                ? CategoryBehaviour.getBehaviorDescriptionsWithoutSkipOnce()
                : CategoryBehaviour.getBehaviorDescriptions());
        setEntryValues(isHighlightCategory
                ? CategoryBehaviour.getBehaviorKeyValuesWithoutSkipOnce()
                : CategoryBehaviour.getBehaviorKeyValues());
        setSummary(category.description.toString());
        updateTitle();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        try {
            Context context = builder.getContext();
            TableLayout table = new TableLayout(context);
            table.setOrientation(LinearLayout.HORIZONTAL);
            table.setPadding(70, 0, 150, 0);

            TableRow row = new TableRow(context);

            TextView colorTextLabel = new TextView(context);
            colorTextLabel.setText(str("sb_color_dot_label"));
            row.addView(colorTextLabel);

            TextView colorDotView = new TextView(context);
            colorDotView.setText(category.getCategoryColorDot());
            colorDotView.setPadding(30, 0, 30, 0);
            row.addView(colorDotView);

            mEditText = new EditText(context);
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            mEditText.setText(category.colorString());
            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        String colorString = s.toString();
                        if (!colorString.startsWith("#")) {
                            s.insert(0, "#"); // recursively calls back into this method
                            return;
                        }
                        if (colorString.length() > 7) {
                            s.delete(7, colorString.length());
                            return;
                        }
                        final int color = Color.parseColor(colorString);
                        colorDotView.setText(SegmentCategory.getCategoryColorDot(color));
                    } catch (IllegalArgumentException ex) {
                        // ignore
                    }
                }
            });
            mEditText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(mEditText);

            table.addView(row);
            builder.setView(table);
            builder.setTitle(category.title.toString());

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            });
            builder.setNeutralButton(str("sb_reset_color"), (dialog, which) -> {
                try {
                    category.resetColor();
                    updateTitle();
                    Utils.showToastShort(str("sb_color_reset"));
                } catch (Exception ex) {
                    Logger.printException(() -> "setNeutralButton failure", ex);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            mClickedDialogEntryIndex = findIndexOfValue(getValue());
            builder.setSingleChoiceItems(getEntries(), mClickedDialogEntryIndex, (dialog, which) -> mClickedDialogEntryIndex = which);
        } catch (Exception ex) {
            Logger.printException(() -> "onPrepareDialogBuilder failure", ex);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        try {
            if (positiveResult && mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
                String value = getEntryValues()[mClickedDialogEntryIndex].toString();
                if (callChangeListener(value)) {
                    setValue(value);
                    category.setBehaviour(Objects.requireNonNull(CategoryBehaviour.byReVancedKeyValue(value)));
                    SegmentCategory.updateEnabledCategories();
                }
                String colorString = mEditText.getText().toString();
                try {
                    if (!colorString.equals(category.colorString())) {
                        category.setColor(colorString);
                        Utils.showToastShort(str("sb_color_changed"));
                    }
                } catch (IllegalArgumentException ex) {
                    Utils.showToastShort(str("sb_color_invalid"));
                }
                updateTitle();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onDialogClosed failure", ex);
        }
    }

    private void updateTitle() {
        setTitle(category.getTitleWithColorDot());
    }
}