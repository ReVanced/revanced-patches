package app.revanced.extension.youtube.patches;

import android.view.WindowManager;

import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.swipecontrols.SwipeControlsHostActivity;

/**
 * Patch class for 'hdr-auto-brightness' patch.
 *
 * Edit: This patch no longer does anything, as YT already uses BRIGHTNESS_OVERRIDE_NONE
 * as the default brightness level.  The hooked code was also removed from YT 19.09+ as well.
 */
@Deprecated
@SuppressWarnings("unused")
public class HDRAutoBrightnessPatch {
    /**
     * get brightness override for HDR brightness
     *
     * @param original brightness youtube would normally set
     * @return brightness to set on HRD video
     */
    public static float getHDRBrightness(float original) {
        // do nothing if disabled
        if (!Settings.HDR_AUTO_BRIGHTNESS.get()) {
            return original;
        }

        // override with brightness set by swipe-controls
        // only when swipe-controls is active and has overridden the brightness
        final SwipeControlsHostActivity swipeControlsHost = SwipeControlsHostActivity.getCurrentHost().get();
        if (swipeControlsHost != null
                && swipeControlsHost.getScreen() != null
                && swipeControlsHost.getConfig().getEnableBrightnessControl()
                && !swipeControlsHost.getScreen().isDefaultBrightness()) {
            return swipeControlsHost.getScreen().getRawScreenBrightness();
        }

        // otherwise, set the brightness to auto
        return WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
    }
}
