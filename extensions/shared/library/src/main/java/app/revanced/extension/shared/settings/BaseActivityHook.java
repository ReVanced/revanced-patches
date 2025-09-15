package app.revanced.extension.shared.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceFragment;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ToolbarPreferenceFragment;

/**
 * Base class for hooking activities to inject a custom PreferenceFragment with a toolbar.
 * Provides common logic for initializing the activity and setting up the toolbar.
 */
@SuppressWarnings({"deprecation", "NewApi"})
public abstract class BaseActivityHook extends Activity {

    /**
     * Layout parameters for the toolbar, extracted from the dummy toolbar.
     */
    protected static ViewGroup.LayoutParams toolbarLayoutParams;

    /**
     * Sets the layout parameters for the toolbar.
     */
    public static void setToolbarLayoutParams(Toolbar toolbar) {
        if (toolbarLayoutParams != null) {
            toolbar.setLayoutParams(toolbarLayoutParams);
        }
    }

    /**
     * Initializes the activity by setting the theme, content view and injecting a PreferenceFragment.
     */
    public static void initialize(BaseActivityHook hook, Activity activity) {
        try {
            hook.customizeActivityTheme(activity);
            activity.setContentView(hook.getContentViewResourceId());

            // Sanity check.
            String dataString = activity.getIntent().getDataString();
            if (!"revanced_settings_intent".equals(dataString)) {
                Logger.printException(() -> "Unknown intent: " + dataString);
                return;
            }

            PreferenceFragment fragment = hook.createPreferenceFragment();
            hook.createToolbar(activity, fragment);

            activity.getFragmentManager()
                    .beginTransaction()
                    .replace(Utils.getResourceIdentifier("revanced_settings_fragments", "id"), fragment)
                    .commit();
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Creates and configures a toolbar for the activity, replacing a dummy placeholder.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void createToolbar(Activity activity, PreferenceFragment fragment) {
        // Replace dummy placeholder toolbar.
        // This is required to fix submenu title alignment issue with Android ASOP 15+
        ViewGroup toolBarParent = activity.findViewById(
                Utils.getResourceIdentifier("revanced_toolbar_parent", "id"));
        ViewGroup dummyToolbar = Utils.getChildViewByResourceName(toolBarParent, "revanced_toolbar");
        toolbarLayoutParams = dummyToolbar.getLayoutParams();
        toolBarParent.removeView(dummyToolbar);

        // Sets appropriate system navigation bar color for the activity.
        ToolbarPreferenceFragment.setNavigationBarColor(activity.getWindow());

        Toolbar toolbar = new Toolbar(toolBarParent.getContext());
        toolbar.setBackgroundColor(getToolbarBackgroundColor());
        toolbar.setNavigationIcon(getNavigationIcon());
        toolbar.setNavigationOnClickListener(getNavigationClickListener(activity));
        toolbar.setTitle(Utils.getResourceIdentifier("revanced_settings_title", "string"));

        final int margin = Utils.dipToPixels(16);
        toolbar.setTitleMarginStart(margin);
        toolbar.setTitleMarginEnd(margin);
        TextView toolbarTextView = Utils.getChildView(toolbar, false, view -> view instanceof TextView);
        if (toolbarTextView != null) {
            toolbarTextView.setTextColor(Utils.getAppForegroundColor());
            toolbarTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }
        setToolbarLayoutParams(toolbar);

        onPostToolbarSetup(activity, toolbar, fragment);

        toolBarParent.addView(toolbar, 0);
    }

    /**
     * Customizes the activity's theme.
     */
    protected abstract void customizeActivityTheme(Activity activity);

    /**
     * Returns the resource ID for the content view layout.
     */
    protected abstract int getContentViewResourceId();

    /**
     * Returns the background color for the toolbar.
     */
    protected abstract int getToolbarBackgroundColor();

    /**
     * Returns the navigation icon drawable for the toolbar.
     */
    protected abstract Drawable getNavigationIcon();

    /**
     * Returns the click listener for the toolbar's navigation icon.
     */
    protected abstract View.OnClickListener getNavigationClickListener(Activity activity);

    /**
     * Creates the PreferenceFragment to be injected into the activity.
     */
    protected PreferenceFragment createPreferenceFragment() {
        return new ToolbarPreferenceFragment();
    }

    /**
     * Performs additional setup after the toolbar is configured.
     *
     * @param activity The activity hosting the toolbar.
     * @param toolbar  The configured toolbar.
     * @param fragment The PreferenceFragment associated with the activity.
     */
    protected void onPostToolbarSetup(Activity activity, Toolbar toolbar, PreferenceFragment fragment) {}
}
