package app.revanced.extension.youtube.patches;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.shared.PlayerType;
import app.revanced.extension.youtube.shared.ShortsPlayerState;
import app.revanced.extension.youtube.shared.VideoState;

@SuppressWarnings("unused")
public class PlayerTypeHookPatch {
    /**
     * Injection point.
     */
    public static void setPlayerType(@Nullable Enum<?> youTubePlayerType) {
        if (youTubePlayerType == null) return;

        PlayerType.setFromString(youTubePlayerType.name());
    }

    /**
     * Injection point.
     */
    public static void setVideoState(@Nullable Enum<?> youTubeVideoState) {
        if (youTubeVideoState == null) return;

        VideoState.setFromString(youTubeVideoState.name());
    }

    /**
     * Injection point.
     *
     * Add a listener to the shorts player overlay View.
     * Triggered when a shorts player is attached or detached to Windows.
     *
     * @param view shorts player overlay (R.id.reel_watch_player).
     */
    public static void onShortsCreate(View view) {
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@Nullable View v) {
                ShortsPlayerState.setOpen(true);
            }

            @Override
            public void onViewDetachedFromWindow(@Nullable View v) {
                ShortsPlayerState.setOpen(false);
            }
        });
    }
}
