package app.revanced.extension.youtube.patches.components;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
final class ButtonsFilter extends Filter {
    private static final String COMPACT_CHANNEL_BAR_PATH_PREFIX = "compact_channel_bar.eml";
    private static final String VIDEO_ACTION_BAR_PATH_PREFIX = "video_action_bar.eml";
    private static final String VIDEO_ACTION_BAR_PATH = "video_action_bar.eml";
    private static final String ANIMATED_VECTOR_TYPE_PATH = "AnimatedVectorType";

    private final StringFilterGroup likeSubscribeGlow;
    private final StringFilterGroup actionBarGroup;
    private final StringFilterGroup bufferFilterPathGroup;
    private final ByteArrayFilterGroupList bufferButtonsGroupList = new ByteArrayFilterGroupList();

    public ButtonsFilter() {
        actionBarGroup = new StringFilterGroup(
                null,
                VIDEO_ACTION_BAR_PATH
        );
        addIdentifierCallbacks(actionBarGroup);


        likeSubscribeGlow = new StringFilterGroup(
                Settings.DISABLE_LIKE_SUBSCRIBE_GLOW,
                "animated_button_border.eml"
        );

        bufferFilterPathGroup = new StringFilterGroup(
                null,
                "|ContainerType|button.eml"
        );

        addPathCallbacks(
                likeSubscribeGlow,
                bufferFilterPathGroup,
                new StringFilterGroup(
                        Settings.HIDE_LIKE_DISLIKE_BUTTON,
                        "|segmented_like_dislike_button"
                ),
                new StringFilterGroup(
                        Settings.HIDE_DOWNLOAD_BUTTON,
                        "|download_button.eml"
                ),
                new StringFilterGroup(
                        Settings.HIDE_SAVE_BUTTON,
                        "|save_to_playlist_button"
                ),
                new StringFilterGroup(
                        Settings.HIDE_CLIP_BUTTON,
                        "|clip_button.eml"
                )
        );

        bufferButtonsGroupList.addAll(
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
                new ByteArrayFilterGroup(
                        Settings.HIDE_THANKS_BUTTON,
                        "yt_outline_dollar_sign_heart"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_ASK_BUTTON,
                        "yt_fill_spark"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_STOP_ADS_BUTTON,
                        "yt_outline_slash_circle_left"
                ),
                // Check for clip button both here and using a path filter,
                // as there's a chance the path is a generic action button and won't contain 'clip_button'
                new ByteArrayFilterGroup(
                        Settings.HIDE_CLIP_BUTTON,
                        "yt_outline_scissors"
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
    boolean isFiltered(String identifier, String path, byte[] buffer,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == likeSubscribeGlow) {
            return (path.startsWith(VIDEO_ACTION_BAR_PATH_PREFIX) || path.startsWith(COMPACT_CHANNEL_BAR_PATH_PREFIX))
                    && path.contains(ANIMATED_VECTOR_TYPE_PATH);
        }

        // If the current matched group is the action bar group,
        // in case every filter group is enabled, hide the action bar.
        if (matchedGroup == actionBarGroup) {
            return isEveryFilterGroupEnabled();
        }

        if (matchedGroup == bufferFilterPathGroup) {
            // Make sure the current path is the right one
            //  to avoid false positives.
            return path.startsWith(VIDEO_ACTION_BAR_PATH)
                    && bufferButtonsGroupList.check(buffer).isFiltered();
        }

        return true;
    }
}
