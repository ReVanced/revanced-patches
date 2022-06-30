package app.revanced.integrations.videoswipecontrols;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.NewSegmentHelperLayout;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.sponsorblock.player.PlayerType;
import app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SwipeHelper;

public class SwipeControlAPI {

    private static SwipeGestureController swipeGestureController;

    public static void InitializeFensterController(Context context, ViewGroup viewGroup, ViewConfiguration viewConfiguration) {
        swipeGestureController = new SwipeGestureController();
        swipeGestureController.setFensterEventsListener(new SwipeListener(context, viewGroup), context, viewConfiguration);
        LogHelper.debug(SwipeControlAPI.class, "XFenster initialized");
    }

    public static boolean FensterTouchEvent(MotionEvent motionEvent) {
        if (swipeGestureController == null) {
            LogHelper.debug(SwipeControlAPI.class, "fensterGestureController is null");
            return false;
        } else if (motionEvent == null) {
            LogHelper.debug(SwipeControlAPI.class, "motionEvent is null");
            return false;
        } else if (!SwipeHelper.IsControlsShown()) {
            return swipeGestureController.onTouchEvent(motionEvent);
        } else {
            LogHelper.debug(SwipeControlAPI.class, "skipping onTouchEvent dispatching because controls are shown.");
            return false;
        }
    }

    public static void PlayerTypeChanged(PlayerType playerType) {
        LogHelper.debug(SwipeControlAPI.class, playerType.toString());
        if (ReVancedUtils.getPlayerType() != playerType) {
            if (playerType == PlayerType.WATCH_WHILE_FULLSCREEN) {
                EnableSwipeControl();
            } else {
                DisableSwipeControl();
            }
            if (playerType == PlayerType.WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED || playerType == PlayerType.WATCH_WHILE_MINIMIZED || playerType == PlayerType.WATCH_WHILE_PICTURE_IN_PICTURE) {
                NewSegmentHelperLayout.hide();
            }
            SponsorBlockView.playerTypeChanged(playerType);
            SponsorBlockUtils.playerTypeChanged(playerType);
        }
        ReVancedUtils.setPlayerType(playerType);
    }

    private static void EnableSwipeControl() {
        if (SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean() || SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.getBoolean()) {
            SwipeGestureController swipeGestureController2 = swipeGestureController;
            swipeGestureController2.TouchesEnabled = true;
            ((SwipeListener) swipeGestureController2.listener).enable(SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean(), SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.getBoolean());
        }
    }

    private static void DisableSwipeControl() {
        SwipeGestureController swipeGestureController2 = swipeGestureController;
        swipeGestureController2.TouchesEnabled = false;
        ((SwipeListener) swipeGestureController2.listener).disable();
    }

}
