package app.revanced.extension.youtube.patches;

import android.view.View;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.ui.Dim;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class WideSearchbarPatch {

    private static final Boolean WIDE_SEARCHBAR_ENABLED = Settings.WIDE_SEARCHBAR.get();

    /**
     * Injection point.
     */
    public static boolean enableWideSearchbar(boolean original) {
        return WIDE_SEARCHBAR_ENABLED || original;
    }

    /**
     * Injection point.
     */
    public static void setActionBar(View view) {
        try {
            if (!WIDE_SEARCHBAR_ENABLED) return;

            View searchBarView = Utils.getChildViewByResourceName(view, "search_bar");

            final int paddingLeft = searchBarView.getPaddingLeft();
            final int paddingRight = searchBarView.getPaddingRight();
            final int paddingTop = searchBarView.getPaddingTop();
            final int paddingBottom = searchBarView.getPaddingBottom();
            final int paddingStart = Dim.dp8;

            if (Utils.isRightToLeftLocale()) {
                searchBarView.setPadding(paddingLeft, paddingTop, paddingStart, paddingBottom);
            } else {
                searchBarView.setPadding(paddingStart, paddingTop, paddingRight, paddingBottom);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setActionBar failure", ex);
        }
    }
}
