package app.revanced.integrations.patches;

import android.view.WindowManager;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.swipecontrols.SwipeControlsHostActivity;

/**
 * Patch class for 'hdr-auto-brightness' patch
 *
 * @usedBy app/revanced/patches/youtube/misc/hdrbrightness/patch/HDRBrightnessPatch
 * @smali app/revanced/integrations/patches/HDRAutoBrightnessPatch
 */
public class HDRAutoBrightnessPatch {
    /**
     * get brightness override for HDR brightness
     *
     * @param original brightness youtube would normally set
     * @return brightness to set on HRD video
     * @smali getHDRBrightness(F)F
     */
    public static float getHDRBrightness(float original) {
        // do nothing if disabled
        if (!SettingsEnum.HDR_AUTO_BRIGHTNESS.getBoolean()) {
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
