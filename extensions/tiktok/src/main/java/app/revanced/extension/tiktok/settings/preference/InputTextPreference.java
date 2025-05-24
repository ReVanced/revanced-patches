package app.revanced.extension.tiktok.settings.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import android.view.View;

import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.tiktok.Utils;

@SuppressWarnings("deprecation")
public class InputTextPreference extends EditTextPreference {

    public InputTextPreference(Context context, String title, String summary, StringSetting setting) {
        super(context);
        setTitle(title);
        setSummary(summary);
        setKey(setting.key);
        setText(setting.get());
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Utils.setTitleAndSummaryColor(view);
    }
}
