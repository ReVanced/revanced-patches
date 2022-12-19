package app.revanced.integrations.adremover;


import android.view.View;
import android.view.ViewGroup;
import app.revanced.integrations.utils.LogHelper;

public class AdRemoverAPI {

    /**
     * Removes Reels and Home ads
     *
     * @param view
     */
    //ToDo: refactor this
    public static void HideViewWithLayout1dp(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams);
            return;
        }
        LogHelper.printDebug(
                () -> "HideViewWithLayout1dp - Id: " + view.getId() + " Type: " + view.getClass().getName());
    }
}
