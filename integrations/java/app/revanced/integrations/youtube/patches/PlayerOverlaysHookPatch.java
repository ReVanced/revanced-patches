package app.revanced.integrations.youtube.patches;

import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.shared.PlayerOverlays;

@SuppressWarnings("unused")
public class PlayerOverlaysHookPatch {
    /**
     * Injection point.
     */
    public static void playerOverlayInflated(ViewGroup group) {
        PlayerOverlays.attach(group);
    }
}