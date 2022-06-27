package app.revanced.integrations.patches;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.player.PlayerType;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SwipeHelper;
import app.revanced.integrations.videoswipecontrols.SwipeControlAPI;

public class VideoSwipeControlsPatch {

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1FTPRrp6NcIF5LC_ByST1a0wcNoGKcAYS/view?usp=sharing for where it needs to be used.
    public static void InitializeFensterController(Context context, ViewGroup viewGroup, ViewConfiguration viewConfiguration) {
        SwipeControlAPI.InitializeFensterController(context, viewGroup, viewConfiguration);
    }

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1Q9TIDDKvc-nQuJWLmCh7jx3ohxPdFIrZ/view?usp=sharing
    //And https://drive.google.com/file/d/1byP5SItks9MYB3fDVC39xH_6nBbaH9NY/view?usp=sharing
    // for where it needs to be used.
    public static boolean FensterTouchEvent(MotionEvent motionEvent) {
        return SwipeControlAPI.FensterTouchEvent(motionEvent);
    }

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1qKmaX4yzAjzYM6T5uUiRtbMxk2zJlwtd/view?usp=sharing for where it needs to be used.
    public static boolean isSwipeControlBrightnessEnabled() {
        return SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean();
    }

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1TMQxs0ul2_p5_GomE-3J1QWO__M189Da/view?usp=sharing for where it needs to be used.
    public static void PlayerTypeChanged(PlayerType playerType) {
        SwipeControlAPI.PlayerTypeChanged(playerType);
    }

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1EoVAyqWOMGUDCovuDb27yeQJBk_CyKsY/view?usp=sharing for where it needs to be used.
    public static boolean isSwipeControlEnabled() {
        if (ReVancedUtils.getPlayerType() != null && ReVancedUtils.getPlayerType() == PlayerType.WATCH_WHILE_FULLSCREEN && !SwipeHelper.IsControlsShown()) {
            return SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean() || SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.getBoolean();
        }
        return false;
    }

}
