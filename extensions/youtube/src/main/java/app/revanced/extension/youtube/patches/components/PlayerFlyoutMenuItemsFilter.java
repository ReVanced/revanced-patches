package app.revanced.extension.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class PlayerFlyoutMenuItemsFilter extends Filter {

    public static final class HideAudioFlyoutMenuAvailability implements Setting.Availability {
        private static final boolean AVAILABLE_ON_LAUNCH = SpoofVideoStreamsPatch.notSpoofingToAndroid();

        @Override
        public boolean isAvailable() {
            // Check conditions of launch and now. Otherwise if spoofing is changed
            // without a restart the setting will show as available when it's not.
            return AVAILABLE_ON_LAUNCH && SpoofVideoStreamsPatch.notSpoofingToAndroid();
        }
    }

    private final ByteArrayFilterGroupList flyoutFilterGroupList = new ByteArrayFilterGroupList();

    private final ByteArrayFilterGroup exception;
    private final StringFilterGroup videoQualityMenuFooter;

    public PlayerFlyoutMenuItemsFilter() {
        exception = new ByteArrayFilterGroup(
                // Whitelist Quality menu item when "Hide Additional settings menu" is enabled
                Settings.HIDE_PLAYER_FLYOUT_ADDITIONAL_SETTINGS,
                "quality_sheet"
        );

        videoQualityMenuFooter = new StringFilterGroup(
                Settings.HIDE_PLAYER_FLYOUT_VIDEO_QUALITY_FOOTER,
                "quality_sheet_footer"
        );

        addPathCallbacks(
                videoQualityMenuFooter,
                new StringFilterGroup(null, "overflow_menu_item.eml|")
        );

        flyoutFilterGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_CAPTIONS,
                        "closed_caption"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_ADDITIONAL_SETTINGS,
                        "yt_outline_gear"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_LOOP_VIDEO,
                        "yt_outline_arrow_repeat_1_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_AMBIENT_MODE,
                        "yt_outline_screen_light"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_STABLE_VOLUME,
                        "volume_stable"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_HELP,
                        "yt_outline_question_circle"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_MORE_INFO,
                        "yt_outline_info_circle"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_LOCK_SCREEN,
                        "yt_outline_lock"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_SPEED,
                        "yt_outline_play_arrow_half_circle"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_AUDIO_TRACK,
                        "yt_outline_person_radar"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_SLEEP_TIMER,
                        "yt_outline_moon_z_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_WATCH_IN_VR,
                        "yt_outline_vr"
                )
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == videoQualityMenuFooter) {
            return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
        }

        if (contentIndex != 0) {
            return false; // Overflow menu is always the start of the path.
        }

        // Shorts also use this player flyout panel
        if (PlayerType.getCurrent().isNoneOrHidden() || exception.check(protobufBufferArray).isFiltered()) {
            return false;
        }

        if (flyoutFilterGroupList.check(protobufBufferArray).isFiltered()) {
            // Super class handles logging.
            return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
        }

        return false;
    }
}
