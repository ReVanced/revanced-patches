package app.revanced.integrations.tiktok.settings.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import app.revanced.integrations.shared.settings.StringSetting;

@SuppressWarnings("deprecation")
public class DownloadPathPreference extends DialogPreference {
    private final Context context;
    private final String[] entryValues = {"DCIM", "Movies", "Pictures"};
    private String mValue;

    private boolean mValueSet;
    private int mediaPathIndex;
    private String childDownloadPath;

    public DownloadPathPreference(Context context, String title, StringSetting setting) {
        super(context);
        this.context = context;
        this.setTitle(title);
        this.setSummary(Environment.getExternalStorageDirectory().getPath() + "/" + setting.get());
        this.setKey(setting.key);
        this.setValue(setting.get());
    }

    public String getValue() {
        return this.mValue;
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

    @Override
    protected View onCreateDialogView() {
        String currentMedia = getValue().split("/")[0];
        childDownloadPath = getValue().substring(getValue().indexOf("/") + 1);
        mediaPathIndex = findIndexOf(currentMedia);

        LinearLayout dialogView = new LinearLayout(context);
        RadioGroup mediaPath = new RadioGroup(context);
        mediaPath.setLayoutParams(new RadioGroup.LayoutParams(-1, -2));
        for (String entryValue : entryValues) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(entryValue);
            radioButton.setId(View.generateViewId());
            mediaPath.addView(radioButton);
        }
        mediaPath.setOnCheckedChangeListener((radioGroup, id) -> {
            RadioButton radioButton = radioGroup.findViewById(id);
            mediaPathIndex = findIndexOf(radioButton.getText().toString());
        });
        mediaPath.check(mediaPath.getChildAt(mediaPathIndex).getId());
        EditText downloadPath = new EditText(context);
        downloadPath.setInputType(InputType.TYPE_CLASS_TEXT);
        downloadPath.setText(childDownloadPath);
        downloadPath.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                childDownloadPath = editable.toString();
            }
        });
        dialogView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        dialogView.setOrientation(LinearLayout.VERTICAL);
        dialogView.addView(mediaPath);
        dialogView.addView(downloadPath);
        return dialogView;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setTitle("Download Path");
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> this.onClick(dialog, DialogInterface.BUTTON_POSITIVE));
        builder.setNegativeButton(android.R.string.cancel, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mediaPathIndex >= 0) {
            String newValue = entryValues[mediaPathIndex] + "/" + childDownloadPath;
            setSummary(Environment.getExternalStorageDirectory().getPath() + "/" + newValue);
            setValue(newValue);
        }
    }

    private int findIndexOf(String str) {
        for (int i = 0; i < entryValues.length; i++) {
            if (str.equals(entryValues[i])) return i;
        }
        return -1;
    }
}
