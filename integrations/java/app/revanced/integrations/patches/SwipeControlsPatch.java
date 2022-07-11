package app.revanced.integrations.patches;

import android.app.Activity;

import androidx.annotation.Nullable;

import app.revanced.integrations.swipecontrols.views.SwipeControlsHostLayout;

/**
 * Hook receiver class for 'swipe-controls' patch
 *
 * @usedBy app.revanced.patches.youtube.interaction.swipecontrols.patch.SwipeControlsPatch
 * @smali Lapp/revanced/integrations/patches/SwipeControlsPatch;
 */
@SuppressWarnings("unused")
public class SwipeControlsPatch {
    /**
     * Hook into the main activity lifecycle
     * (using onStart here, but really anything up until onResume should be fine)
     *
     * @param thisRef reference to the WatchWhileActivity instance
     * @smali WatchWhileActivity_onStartHookEX(Ljava / lang / Object ;)V
     */
    public static void WatchWhileActivity_onStartHookEX(@Nullable Object thisRef) {
        if (thisRef == null) return;
        if (thisRef instanceof Activity) {
            SwipeControlsHostLayout.attachTo((Activity) thisRef, false);
        }
    }
}
