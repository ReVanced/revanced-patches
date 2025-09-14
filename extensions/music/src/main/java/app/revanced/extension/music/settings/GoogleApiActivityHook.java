package app.revanced.extension.music.settings;

import static app.revanced.extension.shared.Utils.getResourceIdentifier;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceFragment;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;
import app.revanced.extension.music.settings.preference.ReVancedPreferenceFragment;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

/**
 * Hooks GoogleApiActivityHook.
 * <p>
 * This class is responsible for injecting our own fragment by replacing the GoogleApiActivityHook.
 */
@SuppressWarnings("unused")
public class GoogleApiActivityHook {

    private static ViewGroup.LayoutParams toolbarLayoutParams;

    public static void setToolbarLayoutParams(Toolbar toolbar) {
        if (toolbarLayoutParams != null) {
            toolbar.setLayoutParams(toolbarLayoutParams);
        }
    }

    /**
     * Injection point.
     * <p>
     * Hooks GoogleApiActivityHook#onCreate in order to inject our own fragment.
     */
    public static void initialize(Activity googleApiActivityHook) {
        try {
            googleApiActivityHook.setContentView(getResourceIdentifier(
                    "revanced_music_settings_with_toolbar", "layout"));

            // Sanity check.
            String dataString = googleApiActivityHook.getIntent().getDataString();
            if (!"revanced_settings_intent".equals(dataString)) {
                Logger.printException(() -> "Unknown intent: " + dataString);
                return;
            }

            PreferenceFragment fragment = new ReVancedPreferenceFragment();
            createToolbar(googleApiActivityHook, fragment);

            //noinspection deprecation
            googleApiActivityHook.getFragmentManager()
                    .beginTransaction()
                    .replace(getResourceIdentifier("revanced_settings_fragments", "id"), fragment)
                    .commit();
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void createToolbar(Activity activity, PreferenceFragment fragment) {
        // Replace dummy placeholder toolbar.
        // This is required to fix submenu title alignment issue with Android ASOP 15+
        ViewGroup toolBarParent = activity.findViewById(
                getResourceIdentifier("revanced_toolbar_parent", "id"));
        ViewGroup dummyToolbar = Utils.getChildViewByResourceName(toolBarParent, "revanced_toolbar");
        toolbarLayoutParams = dummyToolbar.getLayoutParams();
        toolBarParent.removeView(dummyToolbar);

        Toolbar toolbar = new Toolbar(toolBarParent.getContext());
        toolbar.setBackgroundColor(Color.BLACK);

        Drawable navigationIcon = ReVancedPreferenceFragment.getBackButtonDrawable();
        navigationIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        toolbar.setNavigationIcon(navigationIcon);
        toolbar.setNavigationOnClickListener(view -> activity.finish());
        toolbar.setTitle(getResourceIdentifier("revanced_settings_title", "string"));

        final int margin = Utils.dipToPixels(16);
        toolbar.setTitleMarginStart(margin);
        toolbar.setTitleMarginEnd(margin);
        TextView toolbarTextView = Utils.getChildView(toolbar, false,
                view -> view instanceof TextView);
        if (toolbarTextView != null) {
            toolbarTextView.setTextColor(Utils.getAppForegroundColor());
            toolbarTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }
        setToolbarLayoutParams(toolbar);

        toolBarParent.addView(toolbar, 0);
    }
}
