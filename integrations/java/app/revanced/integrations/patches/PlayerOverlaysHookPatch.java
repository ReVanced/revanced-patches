package app.revanced.integrations.patches;

import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.PlayerOverlays;

/**
 * Hook receiver class for 'player-overlays-hook' patch
 *
 * @usedBy app.revanced.patches.youtube.misc.playeroverlay.patch.PlayerOverlaysHookPatch
 * @smali Lapp/revanced/integrations/patches/PlayerOverlaysHookPatch;
 */
@SuppressWarnings("unused")
public class PlayerOverlaysHookPatch {
    /**
     * Hook into YouTubePlayerOverlaysLayout.onFinishInflate() method
     *
     * @param thisRef reference to the view
     * @smali YouTubePlayerOverlaysLayout_onFinishInflateHook(Ljava / lang / Object ;)V
     */
    public static void YouTubePlayerOverlaysLayout_onFinishInflateHook(@Nullable Object thisRef) {
        if (thisRef == null) return;
        if (thisRef instanceof ViewGroup) {
            PlayerOverlays.attach((ViewGroup) thisRef);
        }
    }
}