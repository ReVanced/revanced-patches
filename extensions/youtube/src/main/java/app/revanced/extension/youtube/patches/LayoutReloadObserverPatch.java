package app.revanced.extension.youtube.patches;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.shared.PlayerType;

/**
 * Here is an unintended behavior:
 * <p>
 * 1. The user does not hide Shorts in the Subscriptions tab, but hides them otherwise.
 * 2. Goes to the Subscriptions tab and scrolls to where Shorts is.
 * 3. Opens a regular video.
 * 4. Minimizes the video and turns off the screen.
 * 5. Turns the screen on and maximizes the video.
 * 6. Shorts belonging to related videos are not hidden.
 * <p>
 * Here is an explanation of this special issue:
 * <p>
 * When the user minimizes the video, turns off the screen, and then turns it back on,
 * the components below the player are reloaded, and at this moment the PlayerType is [WATCH_WHILE_MINIMIZED].
 * (Shorts belonging to related videos are also reloaded)
 * Since the PlayerType is [WATCH_WHILE_MINIMIZED] at this moment, the navigation tab is checked.
 * (Even though PlayerType is [WATCH_WHILE_MINIMIZED], this is a Shorts belonging to a related video)
 * <p>
 * As a workaround for this special issue, if a video actionbar is detected, which is one of the components below the player,
 * it is treated as being in the same state as [WATCH_WHILE_MAXIMIZED].
 */
@SuppressWarnings("unused")
public class LayoutReloadObserverPatch {
    private static final String COMPACTIFY_VIDEO_ACTION_BAR_PREFIX = "compactify_video_action_bar.e";
    private static final String VIDEO_ACTION_BAR_PREFIX = "video_action_bar.e";
    public static final AtomicBoolean isActionBarVisible = new AtomicBoolean(false);

    public static void onLazilyConvertedElementLoaded(@NonNull String identifier,
                                                      @NonNull List<Object> treeNodeResultList) {
        if (!Utils.startsWithAny(identifier, COMPACTIFY_VIDEO_ACTION_BAR_PREFIX, VIDEO_ACTION_BAR_PREFIX)) {
            return;
        }

        PlayerType playerType = PlayerType.getCurrent();
        if (playerType == PlayerType.WATCH_WHILE_MINIMIZED || playerType == PlayerType.WATCH_WHILE_PICTURE_IN_PICTURE) {
            if (isActionBarVisible.compareAndSet(false, true)) {
                Utils.runOnMainThreadDelayed(() -> isActionBarVisible.compareAndSet(true, false), 250);
            }
        }
    }
}
