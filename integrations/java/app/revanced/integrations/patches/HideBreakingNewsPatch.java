package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.patches.spoof.SpoofAppVersionPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public class HideBreakingNewsPatch {

    /**
     * When spoofing to app versions 17.31.00 and older, the watch history preview bar uses
     * the same layout components as the breaking news shelf.
     *
     * Breaking news does not appear to be present in these older versions anyways.
     */
    private static final boolean isSpoofingOldVersionWithHorizontalCardListWatchHistory =
            SpoofAppVersionPatch.isSpoofingToEqualOrLessThan("17.31.00");

    /**
     * Injection point.
     */
    public static void hideBreakingNews(View view) {
        if (!SettingsEnum.HIDE_BREAKING_NEWS.getBoolean()
                || isSpoofingOldVersionWithHorizontalCardListWatchHistory) return;
        ReVancedUtils.hideViewByLayoutParams(view);
    }
}
