package app.revanced.integrations.youtube.patches.components;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
@RequiresApi(api = Build.VERSION_CODES.N)
final class ButtonsFilter extends Filter {
    private static final String VIDEO_ACTION_BAR_PATH = "video_action_bar.eml";

    private final StringFilterGroup actionBarGroup;
    private final StringFilterGroup bufferFilterPathGroup;
    private final ByteArrayFilterGroupList bufferButtonsGroupList = new ByteArrayFilterGroupList();

    public ButtonsFilter() {
        actionBarGroup = new StringFilterGroup(
                null,
                VIDEO_ACTION_BAR_PATH
        );
        addIdentifierCallbacks(actionBarGroup);


        bufferFilterPathGroup = new StringFilterGroup(
                null,
                "|CellType|CollectionType|CellType|ContainerType|button.eml|"
        );
        addPathCallbacks(
                new StringFilterGroup(
                        Settings.HIDE_LIKE_DISLIKE_BUTTON,
                        "|segmented_like_dislike_button"
                ),
                new StringFilterGroup(
                        Settings.HIDE_DOWNLOAD_BUTTON,
                        "|download_button.eml|"
                ),
                new StringFilterGroup(
                        Settings.HIDE_PLAYLIST_BUTTON,
                        "|save_to_playlist_button"
                ),
                new StringFilterGroup(
                        Settings.HIDE_CLIP_BUTTON,
                        "|clip_button.eml|"
                ),
                bufferFilterPathGroup
        );

        bufferButtonsGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_LIVE_CHAT_BUTTON,
                        "yt_outline_message_bubble_overlap"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_REPORT_BUTTON,
                        "yt_outline_flag"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHARE_BUTTON,
                        "yt_outline_share"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_REMIX_BUTTON,
                        "yt_outline_youtube_shorts_plus"
                ),
                // Check for clip button both here and using a path filter,
                // as there's a chance the path is a generic action button and won't contain 'clip_button'
                new ByteArrayFilterGroup(
                        Settings.HIDE_CLIP_BUTTON,
                        "yt_outline_scissors"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHOP_BUTTON,
                        "yt_outline_bag"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_THANKS_BUTTON,
                        "yt_outline_dollar_sign_heart"
                )
        );
    }

    private boolean isEveryFilterGroupEnabled() {
        for (var group : pathCallbacks)
            if (!group.isEnabled()) return false;

        for (var group : bufferButtonsGroupList)
            if (!group.isEnabled()) return false;

        return true;
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        // If the current matched group is the action bar group,
        // in case every filter group is enabled, hide the action bar.
        if (matchedGroup == actionBarGroup) {
            if (!isEveryFilterGroupEnabled()) {
                return false;
            }
        } else if (matchedGroup == bufferFilterPathGroup) {
            // Make sure the current path is the right one
            //  to avoid false positives.
            if (!path.startsWith(VIDEO_ACTION_BAR_PATH)) return false;

            // In case the group list has no match, return false.
            if (!bufferButtonsGroupList.check(protobufBufferArray).isFiltered()) return false;
        }

        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }
}
