package app.revanced.integrations.settingsmenu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.ThemeHelper;

/* loaded from: classes6.dex */
public class XSettingActivity extends Activity {
    private static Context context;
    private boolean currentTheme;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        this.currentTheme = ThemeHelper.isDarkTheme();
        if (currentTheme) {
            LogHelper.debug("XSettingsActivity", "set Theme.YouTube.Settings.Dark");
            setTheme(getIdentifier("Theme.YouTube.Settings.Dark", "style"));
        } else {
            LogHelper.debug("XSettingsActivity", "set Theme.YouTube.Settings");
            setTheme(getIdentifier("Theme.YouTube.Settings", "style"));
        }
        super.onCreate(bundle);
        setContentView(getIdentifier("xsettings_with_toolbar", "layout"));
        initImageButton(this.currentTheme);
        String dataString = getIntent().getDataString();
        if (dataString.equalsIgnoreCase("sponsorblock_settings")) {
            trySetTitle(getIdentifier("sb_settings", "string"));
            getFragmentManager().beginTransaction().replace(getIdentifier("xsettings_fragments", "id"), new SponsorBlockSettingsFragment()).commit();
        } else if (dataString.equalsIgnoreCase("ryd_settings")) {
            trySetTitle(getIdentifier("revanced_ryd_settings_title", "string"));
            getFragmentManager().beginTransaction().replace(getIdentifier("xsettings_fragments", "id"), new RYDSettingsFragment()).commit();
        } else {
            trySetTitle(getIdentifier("revanced_settings", "string"));
            getFragmentManager().beginTransaction().replace(getIdentifier("xsettings_fragments", "id"), new ReVancedSettingsFragment()).commit();
        }
        context = getApplicationContext();
    }

    public static ImageButton getImageButton(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return null;
        }
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ImageButton) {
                return (ImageButton) childAt;
            }
        }
        return null;
    }

    public static TextView getTextView(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return null;
        }
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof TextView) {
                return (TextView) childAt;
            }
        }
        return null;
    }

    private static int getIdentifier(String str, String str2) {
        Context appContext = ReVancedUtils.getContext();
        return appContext.getResources().getIdentifier(str, str2, appContext.getPackageName());
    }

    private void trySetTitle(int i) {
        try {
            getTextView((ViewGroup) findViewById(getIdentifier("toolbar", "id"))).setText(i);
        } catch (Exception e) {
            LogHelper.printException("XSettingsActivity", "Couldn't set Toolbar title", e);
        }
    }

    private void trySetTitle(String str) {
        try {
            getTextView((ViewGroup) findViewById(getIdentifier("toolbar", "id"))).setText(str);
        } catch (Exception e) {
            LogHelper.printException("XSettingsActivity", "Couldn't set Toolbar title", e);
        }
    }

    private void initImageButton(boolean z) {
        try {
            ImageButton imageButton = getImageButton((ViewGroup) findViewById(getIdentifier("toolbar", "id")));
            imageButton.setOnClickListener(new View.OnClickListener() { // from class: app.revanced.integrations.theme.XSettingActivity.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    XSettingActivity.this.onBackPressed();
                }
            });
            imageButton.setImageDrawable(getResources().getDrawable(getIdentifier(z ? "quantum_ic_arrow_back_white_24" : "quantum_ic_arrow_back_grey600_24", "drawable")));
        } catch (Exception e) {
            LogHelper.printException("XSettingsActivity", "Couldn't set Toolbar click handler", e);
        }
    }

}
