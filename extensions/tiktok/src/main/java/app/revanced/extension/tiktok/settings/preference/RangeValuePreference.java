package app.revanced.extension.tiktok.settings.preference;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.tiktok.Utils;

@SuppressWarnings("deprecation")
public class RangeValuePreference extends DialogPreference {
    private String minValue;

    private String maxValue;

    private String mValue;

    private boolean mValueSet;

    public RangeValuePreference(Context context, String title, String summary, StringSetting setting) {
        super(context);
        setTitle(title);
        setSummary(summary);
        setKey(setting.key);
        setValue(setting.get());
    }

    public void setValue(String value) {
        final boolean changed = !TextUtils.equals(mValue, value);
        if (changed || !mValueSet) {
            mValue = value;
            mValueSet = true;
            persistString(value);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    public String getValue() {
        return mValue;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected View onCreateDialogView() {
        minValue = getValue().split("-")[0];
        maxValue = getValue().split("-")[1];

        Context context = getContext();

        LinearLayout dialogView = new LinearLayout(context);
        dialogView.setOrientation(LinearLayout.VERTICAL);

        // Min view
        LinearLayout minView = new LinearLayout(context);
        minView.setOrientation(LinearLayout.HORIZONTAL);
        dialogView.addView(minView);

        TextView min = new TextView(context);
        min.setText("Min: ");
        minView.addView(min);

        EditText minEditText = new EditText(context);
        minEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        minEditText.setText(minValue);
        minView.addView(minEditText);

        // Max view
        LinearLayout maxView = new LinearLayout(context);
        maxView.setOrientation(LinearLayout.HORIZONTAL);
        dialogView.addView(maxView);

        TextView max = new TextView(context);
        max.setText("Max: ");
        maxView.addView(max);

        EditText maxEditText = new EditText(context);
        maxEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        maxEditText.setText(maxValue);
        maxView.addView(maxEditText);

        minEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                minValue = editable.toString();
            }
        });
        maxEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                maxValue = editable.toString();
            }
        });

        return dialogView;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Utils.setTitleAndSummaryColor(view);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setPositiveButton(android.R.string.ok, (dialog, which)
                -> this.onClick(dialog, DialogInterface.BUTTON_POSITIVE));
        builder.setNegativeButton(android.R.string.cancel, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String newValue = minValue + "-" + maxValue;
            setValue(newValue);
        }
    }
}
