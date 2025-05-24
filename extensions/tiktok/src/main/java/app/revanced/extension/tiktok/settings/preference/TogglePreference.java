package app.revanced.extension.tiktok.settings.preference;

import android.content.Context;
import android.preference.SwitchPreference;
import android.view.View;

import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.tiktok.Utils;

@SuppressWarnings("deprecation")
public class TogglePreference extends SwitchPreference {

    public TogglePreference(Context context, String title, String summary, BooleanSetting setting) {
        super(context);
        setTitle(title);
        setSummary(summary);
        setKey(setting.key);
        setChecked(setting.get());
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Utils.setTitleAndSummaryColor(view);
    }
}
