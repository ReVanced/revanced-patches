package app.revanced.integrations.patches;

import com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity;
import app.revanced.integrations.utils.LogHelper;

public class FixBackToExitGesturePatch {
    /**
     * State whether the scroll position reaches the top.
     */
    public static boolean isTopView = false;

    /**
     * Handle the event after clicking the back button.
     *
     * @param activity The activity, the app is launched with to finish.
     */
    public static void onBackPressed(WatchWhileActivity activity) {
        if (!isTopView) return;

        LogHelper.printDebug(() -> "Activity is closed");

        activity.finish();
    }

    /**
     * Handle the event when the homepage list of views is being scrolled.
     */
    public static void onScrollingViews() {
        LogHelper.printDebug(() -> "Views are scrolling");

        isTopView = false;
    }

    /**
     * Handle the event when the homepage list of views reached the top.
     */
    public static void onTopView() {
        LogHelper.printDebug(() -> "Scrolling reached the top");

        isTopView = true;
    }
}
