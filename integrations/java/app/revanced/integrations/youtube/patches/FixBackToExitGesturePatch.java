package app.revanced.integrations.youtube.patches;

import android.app.Activity;

import app.revanced.integrations.shared.Logger;

@SuppressWarnings("unused")
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
    public static void onBackPressed(Activity activity) {
        if (!isTopView) return;

        Logger.printDebug(() -> "Activity is closed");

        activity.finish();
    }

    /**
     * Handle the event when the homepage list of views is being scrolled.
     */
    public static void onScrollingViews() {
        Logger.printDebug(() -> "Views are scrolling");

        isTopView = false;
    }

    /**
     * Handle the event when the homepage list of views reached the top.
     */
    public static void onTopView() {
        Logger.printDebug(() -> "Scrolling reached the top");

        isTopView = true;
    }
}
