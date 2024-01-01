package app.revanced.integrations.twitch.settings.preference;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class CustomPreferenceCategory extends PreferenceCategory {
    public CustomPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View rootView) {
        super.onBindView(rootView);

        if(rootView instanceof TextView) {
            ((TextView) rootView).setTextColor(Color.parseColor("#8161b3"));
        }
    }
}