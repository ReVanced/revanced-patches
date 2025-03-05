package app.revanced.extension.shared.settings.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Empty preference category with no title, used to organize and group related preferences together.
 */
@SuppressWarnings({"unused", "deprecation"})
public class NoTitlePreferenceCategory extends PreferenceCategory {
    public NoTitlePreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoTitlePreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NoTitlePreferenceCategory(Context context) {
        super(context);
    }

    @Override
    @SuppressLint("MissingSuperCall")
    protected View onCreateView(ViewGroup parent) {
        // Return an empty, zero-height view to eliminate spacing
        return new View(getContext());
    }
}

