package app.revanced.integrations.patches.playback.quality;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import app.revanced.integrations.patches.components.VideoQualityMenuFilterPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

/**
 * This patch contains the logic to show the old video quality menu.
 * Two methods are required, because the quality menu is a RecyclerView in the new YouTube version
 * and a ListView in the old one.
 */
public final class OldVideoQualityMenuPatch {

    /**
     * Injection point.
     */
    public static void onFlyoutMenuCreate(RecyclerView recyclerView) {
        if (!SettingsEnum.SHOW_OLD_VIDEO_QUALITY_MENU.getBoolean()) return;

        recyclerView.getViewTreeObserver().addOnDrawListener(() -> {
            try {
                // Check if the current view is the quality menu.
                if (VideoQualityMenuFilterPatch.isVideoQualityMenuVisible) {
                    VideoQualityMenuFilterPatch.isVideoQualityMenuVisible = false;
                    ((ViewGroup) recyclerView.getParent().getParent().getParent()).setVisibility(View.GONE);

                    // Click the "Advanced" quality menu to show the "old" quality menu.
                    ((ViewGroup) recyclerView.getChildAt(0)).getChildAt(3).performClick();
                }
            } catch (Exception ex) {
                LogHelper.printException(() -> "onFlyoutMenuCreate failure", ex);
            }
        });
    }

    /**
     * Injection point.  Only used if spoofing to an old app version.
     */
    public static void showOldVideoQualityMenu(final ListView listView) {
        if (!SettingsEnum.SHOW_OLD_VIDEO_QUALITY_MENU.getBoolean()) return;

        listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                LogHelper.printDebug(() -> "Added listener to old type of quality menu");

                parent.setVisibility(View.GONE);

                final var indexOfAdvancedQualityMenuItem = 4;
                if (listView.indexOfChild(child) != indexOfAdvancedQualityMenuItem) return;

                LogHelper.printDebug(() -> "Found advanced menu item in old type of quality menu");

                final var qualityItemMenuPosition = 4;
                listView.performItemClick(null, qualityItemMenuPosition, 0);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
    }
}
