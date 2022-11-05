package app.revanced.integrations.patches.playback.quality;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class OldQualityLayoutPatch {
    public static void showOldQualityMenu(ListView listView)
    {
        if (!SettingsEnum.OLD_STYLE_QUALITY_SETTINGS.getBoolean()) return;

        listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                LogHelper.debug(OldQualityLayoutPatch.class, "Added: " + child);

                parent.setVisibility(View.GONE);

                final var indexOfAdvancedQualityMenuItem = 4;
                if (listView.indexOfChild(child) != indexOfAdvancedQualityMenuItem) return;

                LogHelper.debug(OldQualityLayoutPatch.class, "Found advanced menu: " + child);

                final var qualityItemMenuPosition = 4;
                listView.performItemClick(null, qualityItemMenuPosition, 0);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {}
        });
    }
}
