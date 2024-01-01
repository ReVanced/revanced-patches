package app.revanced.integrations.youtube.patches.components;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class PlayerFlyoutMenuItemsFilter extends Filter {

    private final ByteArrayFilterGroupList flyoutFilterGroupList = new ByteArrayFilterGroupList();

    private final ByteArrayFilterGroup exception;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PlayerFlyoutMenuItemsFilter() {
        exception = new ByteArrayFilterGroup(
                // Whitelist Quality menu item when "Hide Additional settings menu" is enabled
                Settings.HIDE_ADDITIONAL_SETTINGS_MENU,
                "quality_sheet"
        );

        // Using pathFilterGroupList due to new flyout panel(A/B)
        addPathCallbacks(
                new StringFilterGroup(null, "overflow_menu_item.eml|")
        );

        flyoutFilterGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_CAPTIONS_MENU,
                        "closed_caption"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_ADDITIONAL_SETTINGS_MENU,
                        "yt_outline_gear"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_LOOP_VIDEO_MENU,
                        "yt_outline_arrow_repeat_1_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_AMBIENT_MODE_MENU,
                        "yt_outline_screen_light"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_REPORT_MENU,
                        "yt_outline_flag"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_HELP_MENU,
                        "yt_outline_question_circle"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_MORE_INFO_MENU,
                        "yt_outline_info_circle"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SPEED_MENU,
                        "yt_outline_play_arrow_half_circle"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_AUDIO_TRACK_MENU,
                        "yt_outline_person_radar"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_WATCH_IN_VR_MENU,
                        "yt_outline_vr"
                )
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        // Shorts also use this player flyout panel
        if (PlayerType.getCurrent().isNoneOrHidden() || exception.check(protobufBufferArray).isFiltered())
            return false;

        // Only 1 path callback was added, so the matched group must be the overflow menu.
        if (contentIndex == 0 && flyoutFilterGroupList.check(protobufBufferArray).isFiltered()) {
            // Super class handles logging.
            return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
        }
        return false;
    }
}
