package app.revanced.integrations.adremover;


import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toolbar;

import app.revanced.integrations.utils.LogHelper;

public class AdRemoverAPI {

    /**
     * Removes Reels and Home ads
     *
     * @param view
     */
    //ToDo: refactor this
    public static void HideViewWithLayout1dp(View view) {
        if (view instanceof LinearLayout) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams);
        } else if (view instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams2);
        } else if (view instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams3);
        } else if (view instanceof Toolbar) {
            Toolbar.LayoutParams layoutParams4 = new Toolbar.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams4);
        } else if (view instanceof ViewGroup) {
            ViewGroup.LayoutParams layoutParams5 = new ViewGroup.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams5);
        } else {
            LogHelper.debug(AdRemoverAPI.class, "HideViewWithLayout1dp - Id: " + view.getId() + " Type: " + view.getClass().getName());
        }
    }

}
