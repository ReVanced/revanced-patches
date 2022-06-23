package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.sponsorblock.SponsorBlockUtils.formatColorString;
import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.preference.ListPreference;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import app.revanced.integrations.sponsorblock.SponsorBlockSettings;

@SuppressWarnings("deprecation")
public class EditTextListPreference extends ListPreference {

    private EditText mEditText;
    private int mClickedDialogEntryIndex;

    public EditTextListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public EditTextListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditTextListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextListPreference(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        SponsorBlockSettings.SegmentInfo category = getCategoryBySelf();

        mEditText = new EditText(builder.getContext());
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditText.setText(formatColorString(category.color));
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
                    Color.parseColor(s.toString()); // validation
                    getDialog().setTitle(Html.fromHtml(String.format("<font color=\"%s\">⬤</font> %s", s, category.title)));
                } catch (Exception ex) {
                }
            }
        });
        builder.setView(mEditText);
        builder.setTitle(category.getTitleWithDot());

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            EditTextListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
        });
        builder.setNeutralButton(str("reset"), (dialog, which) -> {
            //EditTextListPreference.this.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
            int defaultColor = category.defaultColor;
            category.setColor(defaultColor);
            Toast.makeText(getContext().getApplicationContext(), str("color_reset"), Toast.LENGTH_SHORT).show();
            getSharedPreferences().edit().putString(getColorPreferenceKey(), formatColorString(defaultColor)).apply();
            reformatTitle();
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        mClickedDialogEntryIndex = findIndexOfValue(getValue());
        builder.setSingleChoiceItems(getEntries(), mClickedDialogEntryIndex, (dialog, which) -> mClickedDialogEntryIndex = which);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
            String value = getEntryValues()[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
            String colorString = mEditText.getText().toString();
            SponsorBlockSettings.SegmentInfo category = getCategoryBySelf();
            if (colorString.equals(formatColorString(category.color))) {
                return;
            }
            Context applicationContext = getContext().getApplicationContext();
            try {
                int color = Color.parseColor(colorString);
                category.setColor(color);
                Toast.makeText(applicationContext, str("color_changed"), Toast.LENGTH_SHORT).show();
                getSharedPreferences().edit().putString(getColorPreferenceKey(), formatColorString(color)).apply();
                reformatTitle();
            } catch (Exception ex) {
                Toast.makeText(applicationContext, str("color_invalid"), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private SponsorBlockSettings.SegmentInfo getCategoryBySelf() {
        return SponsorBlockSettings.SegmentInfo.byCategoryKey(getKey());
    }

    private String getColorPreferenceKey() {
        return getKey() + SponsorBlockSettings.PREFERENCES_KEY_CATEGORY_COLOR_SUFFIX;
    }

    private void reformatTitle() {
        this.setTitle(getCategoryBySelf().getTitleWithDot());
    }
}