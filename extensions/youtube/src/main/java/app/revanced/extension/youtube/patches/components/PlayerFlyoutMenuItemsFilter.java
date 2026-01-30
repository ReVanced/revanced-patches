package app.revanced.extension.youtube.patches.components;

import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch;
import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.shared.patches.litho.FilterGroup.*;
import app.revanced.extension.shared.patches.litho.FilterGroupList.*;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.ShortsPlayerState;

import java.util.List;

@SuppressWarnings("unused")
public final class PlayerFlyoutMenuItemsFilter extends Filter {

    public static final class HideAudioFlyoutMenuAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return !SpoofVideoStreamsPatch.spoofingToClientWithNoMultiAudioStreams();
        }

        @Override
        public List<Setting<?>> getParentSettings() {
            return List.of(BaseSettings.SPOOF_VIDEO_STREAMS);
        }
    }

    private final ByteArrayFilterGroupList flyoutFilterGroupList = new ByteArrayFilterGroupList();
    private final StringFilterGroup videoQualityMenuFooter;

    public PlayerFlyoutMenuItemsFilter() {
        videoQualityMenuFooter = new StringFilterGroup(
                Settings.HIDE_PLAYER_FLYOUT_VIDEO_QUALITY_FOOTER,
                "quality_sheet_footer"
        );

        addPathCallbacks(
                videoQualityMenuFooter,
                new StringFilterGroup(null, "overflow_menu_item.e")
        );

        flyoutFilterGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_CAPTIONS,
                        "closed_caption_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_ADDITIONAL_SETTINGS,
                        "yt_outline_gear_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_LOOP_VIDEO,
                        "yt_outline_arrow_repeat_1_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_AMBIENT_MODE,
                        "yt_outline_screen_light_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_STABLE_VOLUME,
                        "volume_stable_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_LISTEN_WITH_YOUTUBE_MUSIC,
                        "yt_outline_youtube_music_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_HELP,
                        "yt_outline_question_circle_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_LOCK_SCREEN,
                        "yt_outline_lock_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_SPEED,
                        "yt_outline_play_arrow_half_circle_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_AUDIO_TRACK,
                        "yt_outline_person_radar_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_SLEEP_TIMER,
                        "yt_outline_moon_z_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_WATCH_IN_VR,
                        "yt_outline_vr_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_PLAYER_FLYOUT_VIDEO_QUALITY,
                        "yt_outline_adjust_"
                )
        );
    }

    @Override
    public boolean isFiltered(String identifier, String path, byte[] buffer,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == videoQualityMenuFooter) {
            return true;
        }

        if (contentIndex != 0) {
            return false; // Overflow menu is always the start of the path.
        }

        // Shorts also use this player flyout panel
        if (ShortsPlayerState.isOpen()) {
            return false;
        }

        return flyoutFilterGroupList.check(buffer).isFiltered();
    }
}
