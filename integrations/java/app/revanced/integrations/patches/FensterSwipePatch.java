package app.revanced.integrations.patches;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.fenster.WatchWhilePlayerType;
import app.revanced.integrations.fenster.controllers.FensterController;
import app.revanced.integrations.utils.LogHelper;

/**
 * Hook receiver class for 'FensterV2' video swipe controls.
 *
 * @usedBy app.revanced.patches.youtube.interaction.fenster.patch.FensterPatch
 * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;
 */
@SuppressWarnings("unused")
public final class FensterSwipePatch {

    /**
     * main fenster controller instance
     */
    @SuppressLint("StaticFieldLeak")
    private static final FensterController FENSTER = new FensterController();

    /**
     * Hook into the main activity lifecycle
     *
     * @param thisRef reference to the WatchWhileActivity instance
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->WatchWhileActivity_onStartHookEX(Ljava/lang/Object;)V
     */
    public static void WatchWhileActivity_onStartHookEX(@Nullable Object thisRef) {
        if (thisRef == null) return;
        if (thisRef instanceof Activity) {
            FENSTER.initializeController((Activity) thisRef);
        }
    }

    /**
     * hook into the player overlays lifecycle
     *
     * @param thisRef reference to the PlayerOverlays instance
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->YouTubePlayerOverlaysLayout_onFinishInflateHookEX(Ljava/lang/Object;)V
     */
    public static void YouTubePlayerOverlaysLayout_onFinishInflateHookEX(@Nullable Object thisRef) {
        if (thisRef == null) return;
        if (thisRef instanceof ViewGroup) {
            FENSTER.initializeOverlay((ViewGroup) thisRef);
        }
    }

    /**
     * Hook into updatePlayerLayout() method
     *
     * @param type the new player type
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(Ljava/lang/Object;)V
     */
    public static void YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(@Nullable Object type) {
        if (type == null) return;

        // disable processing events if not watching fullscreen video
        WatchWhilePlayerType playerType = WatchWhilePlayerType.safeParseFromString(type.toString());
        FENSTER.setEnabled(playerType == WatchWhilePlayerType.WATCH_WHILE_FULLSCREEN);
        LogHelper.debug(FensterSwipePatch.class, "WatchWhile player type was updated to " + playerType);
    }

    /**
     * Hook into NextGenWatchLayout.onTouchEvent
     *
     * @param thisRef     reference to NextGenWatchLayout instance
     * @param motionEvent event parameter
     * @return was the event consumed by the hook?
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->NextGenWatchLayout_onTouchEventHookEX(Ljava/lang/Object;Ljava/lang/Object;)Z
     */
    public static boolean NextGenWatchLayout_onTouchEventHookEX(@Nullable Object thisRef, @Nullable Object motionEvent) {
        if (motionEvent == null) return false;
        if (motionEvent instanceof MotionEvent) {
            return FENSTER.onTouchEvent((MotionEvent) motionEvent);
        }

        return false;
    }

    /**
     * Hook into NextGenWatchLayout.onInterceptTouchEvent
     *
     * @param thisRef     reference to NextGenWatchLayout instance
     * @param motionEvent event parameter
     * @return was the event consumed by the hook?
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->NextGenWatchLayout_onInterceptTouchEventHookEX(Ljava/lang/Object;Ljava/lang/Object;)Z
     */
    public static boolean NextGenWatchLayout_onInterceptTouchEventHookEX(@Nullable Object thisRef, @Nullable Object motionEvent) {
        if (motionEvent == null) return false;
        if (motionEvent instanceof MotionEvent) {
            return FENSTER.onTouchEvent((MotionEvent) motionEvent);
        }

        return false;
    }
}
