package app.revanced.extension.youtube.patches.playback.quality;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.components.AdvancedVideoQualityMenuFilter;
import app.revanced.extension.youtube.settings.Settings;

/**
 * This patch contains the logic to always open the advanced video quality menu.
 * Two methods are required, because the quality menu is a RecyclerView in the new YouTube version
 * and a ListView in the old one.
 */
@SuppressWarnings("unused")
public final class AdvancedVideoQualityMenuPatch {

    /**
     * Injection point.
     */
    public static void onFlyoutMenuCreate(RecyclerView recyclerView) {
        if (!Settings.ADVANCED_VIDEO_QUALITY_MENU.get()) return;

        recyclerView.getViewTreeObserver().addOnDrawListener(() -> {
            try {
                // Check if the current view is the quality menu.
                if (!AdvancedVideoQualityMenuFilter.isVideoQualityMenuVisible || recyclerView.getChildCount() == 0) {
                    return;
                }
                AdvancedVideoQualityMenuFilter.isVideoQualityMenuVisible = false;

                ViewParent quickQualityViewParent = Utils.getParentView(recyclerView, 3);
                if (!(quickQualityViewParent instanceof ViewGroup)) {
                    return;
                }

                View firstChild = recyclerView.getChildAt(0);
                if (!(firstChild instanceof ViewGroup firstChildGroup)) {
                    return;
                }

                if (firstChildGroup.getChildCount() < 4) {
                    return;
                }

                View advancedQualityView = firstChildGroup.getChildAt(3);
                if (advancedQualityView == null) {
                    return;
                }

                ((ViewGroup) quickQualityViewParent).setVisibility(View.GONE);

                // Click the "Advanced" quality menu to show the "old" quality menu.
                advancedQualityView.setSoundEffectsEnabled(false);
                advancedQualityView.performClick();
            } catch (Exception ex) {
                Logger.printException(() -> "onFlyoutMenuCreate failure", ex);
            }
        });
    }


    /**
     * Injection point.
     *
     * Used to force the creation of the advanced menu item for the Shorts quality flyout.
     */
    public static boolean forceAdvancedVideoQualityMenuCreation(boolean original) {
        return Settings.ADVANCED_VIDEO_QUALITY_MENU.get() || original;
    }

    /**
     * Injection point.
     *
     * Used if spoofing to an old app version, and also used for the Shorts video quality flyout.
     */
    public static void showAdvancedVideoQualityMenu(ListView listView) {
        if (!Settings.ADVANCED_VIDEO_QUALITY_MENU.get()) return;

        listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                try {
                    parent.setVisibility(View.GONE);

                    final var indexOfAdvancedQualityMenuItem = 4;
                    if (listView.indexOfChild(child) != indexOfAdvancedQualityMenuItem) return;

                    Logger.printDebug(() -> "Found advanced menu item in old type of quality menu");

                    listView.setSoundEffectsEnabled(false);
                    final var qualityItemMenuPosition = 4;
                    listView.performItemClick(null, qualityItemMenuPosition, 0);

                } catch (Exception ex) {
                    Logger.printException(() -> "showOldVideoQualityMenu failure", ex);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
    }
}