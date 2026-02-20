package app.revanced.extension.gamehub.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Updates the battery percentage TextView that was injected as a sibling
 * of the battery icon ImageView by the resource patch.
 */
public final class BatteryHelper {

    /**
     * Called from BatteryUtil.a(Context, ImageView) after the battery level is read.
     * Walks up to the ImageView's parent and finds the tv_battery_percent TextView by ID.
     *
     * @param batteryImageView the battery icon ImageView
     * @param batteryLevel     the battery percentage (0–100)
     */
    public static void updateBatteryText(ImageView batteryImageView, int batteryLevel) {
        if (batteryImageView == null) return;

        ViewGroup parent = (ViewGroup) batteryImageView.getParent();
        if (parent == null) return;

        int tvId = batteryImageView.getResources().getIdentifier(
                "tv_battery_percent", "id", batteryImageView.getContext().getPackageName());
        if (tvId == 0) return;

        View tv = parent.findViewById(tvId);
        if (tv instanceof TextView) {
            ((TextView) tv).setText(batteryLevel + "%");
        }
    }
}
