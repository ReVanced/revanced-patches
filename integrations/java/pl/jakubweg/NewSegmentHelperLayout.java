package pl.jakubweg;

import android.content.Context;

import static fi.vanced.libraries.youtube.sponsors.player.ui.SponsorBlockView.hideNewSegmentLayout;
import static fi.vanced.libraries.youtube.sponsors.player.ui.SponsorBlockView.showNewSegmentLayout;

public class NewSegmentHelperLayout {
    public static Context context;
    private static boolean isShown = false;

    public static void show() {
        if (isShown) return;
        isShown = true;
        showNewSegmentLayout();
    }

    public static void hide() {
        if (!isShown) return;
        isShown = false;
        hideNewSegmentLayout();
    }

    public static void toggle() {
        if (isShown) hide();
        else show();
    }
}
