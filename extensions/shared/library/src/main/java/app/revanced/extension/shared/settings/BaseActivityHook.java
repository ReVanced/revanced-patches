package app.revanced.extension.shared.settings;

import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ToolbarPreferenceFragment;
import app.revanced.extension.shared.ui.Dim;

/**
 * Base class for hooking activities to inject a custom PreferenceFragment with a toolbar.
 * Provides common logic for initializing the activity and setting up the toolbar.
 */
@SuppressWarnings({"deprecation", "NewApi"})
public abstract class BaseActivityHook extends Activity {

    private static final int ID_REVANCED_SETTINGS_FRAGMENTS =
            getResourceIdentifierOrThrow("revanced_settings_fragments", "id");
    private static final int ID_REVANCED_TOOLBAR_PARENT =
            getResourceIdentifierOrThrow("revanced_toolbar_parent", "id");
    public static final int LAYOUT_REVANCED_SETTINGS_WITH_TOOLBAR =
            getResourceIdentifierOrThrow("revanced_settings_with_toolbar", "layout");
    private static final int STRING_REVANCED_SETTINGS_TITLE =
            getResourceIdentifierOrThrow("revanced_settings_title", "string");

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
                    .replace(ID_REVANCED_SETTINGS_FRAGMENTS, fragment)
                    .commit();
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Injection point.
     * Overrides the ReVanced settings language.
     */
    @SuppressWarnings("unused")
    public static Context getAttachBaseContext(Context original) {
        AppLanguage language = BaseSettings.REVANCED_LANGUAGE.get();
        if (language == AppLanguage.DEFAULT) {
            return original;
        }

        return Utils.getContext();
    }

    /**
     * Creates and configures a toolbar for the activity, replacing a dummy placeholder.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void createToolbar(Activity activity, PreferenceFragment fragment) {
        // Replace dummy placeholder toolbar.
        // This is required to fix submenu title alignment issue with Android ASOP 15+
        ViewGroup toolBarParent = activity.findViewById(ID_REVANCED_TOOLBAR_PARENT);
        ViewGroup dummyToolbar = Utils.getChildViewByResourceName(toolBarParent, "revanced_toolbar");
        toolbarLayoutParams = dummyToolbar.getLayoutParams();
        toolBarParent.removeView(dummyToolbar);

        // Sets appropriate system navigation bar color for the activity.
        ToolbarPreferenceFragment.setNavigationBarColor(activity.getWindow());

        Toolbar toolbar = new Toolbar(toolBarParent.getContext());
        toolbar.setBackgroundColor(getToolbarBackgroundColor());
        toolbar.setNavigationIcon(getNavigationIcon());
        toolbar.setNavigationOnClickListener(getNavigationClickListener(activity));
        toolbar.setTitle(STRING_REVANCED_SETTINGS_TITLE);

        toolbar.setTitleMarginStart(Dim.dp16);
        toolbar.setTitleMarginEnd(Dim.dp16);
        TextView toolbarTextView = Utils.getChildView(toolbar, false, view -> view instanceof TextView);
        if (toolbarTextView != null) {
            toolbarTextView.setTextColor(Utils.getAppForegroundColor());
            toolbarTextView.setTextSize(20);
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
