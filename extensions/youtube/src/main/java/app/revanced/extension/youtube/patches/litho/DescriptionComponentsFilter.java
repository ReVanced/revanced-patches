package app.revanced.extension.youtube.patches.litho;

import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.shared.patches.litho.FilterGroup.*;
import app.revanced.extension.shared.patches.litho.FilterGroupList.ByteArrayFilterGroupList;
import app.revanced.extension.youtube.shared.EngagementPanel;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class DescriptionComponentsFilter extends Filter {

    private static final String INFOCARDS_SECTION_PATH = "infocards_section.e";

    private final StringFilterGroup macroMarkersCarousel;
    private final ByteArrayFilterGroupList macroMarkersCarouselGroupList = new ByteArrayFilterGroupList();
    private final StringFilterGroup playlistSection;
    private final ByteArrayFilterGroupList playlistSectionGroupList = new ByteArrayFilterGroupList();
    private final StringFilterGroup featuredLinksSection;
    private final StringFilterGroup featuredVideosSection;
    private final StringFilterGroup subscribeButton;

    public DescriptionComponentsFilter() {
        final StringFilterGroup aiGeneratedVideoSummarySection = new StringFilterGroup(
                Settings.HIDE_AI_GENERATED_VIDEO_SUMMARY_SECTION,
                "cell_expandable_metadata.e"
        );

        final StringFilterGroup askSection = new StringFilterGroup(
                Settings.HIDE_ASK_SECTION,
                "youchat_entrypoint.e"
        );

        final StringFilterGroup attributesSection = new StringFilterGroup(
                Settings.HIDE_ATTRIBUTES_SECTION,
                // "gaming_section", "music_section"
                "video_attributes_section"
        );

        featuredLinksSection = new StringFilterGroup(
                Settings.HIDE_FEATURED_LINKS_SECTION,
                "media_lockup"
        );

        featuredVideosSection = new StringFilterGroup(
                Settings.HIDE_FEATURED_VIDEOS_SECTION,
                "structured_description_video_lockup"
        );

        playlistSection = new StringFilterGroup(
                // YT v20.14.43 doesn't use any buffer for Courses and Podcasts.
                // So this component is also needed.
                null,
                "playlist_section.e"
        );

        playlistSectionGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_EXPLORE_COURSE_SECTION,
                        "yt_outline_creator_academy", // For Disable bold icons.
                        "yt_outline_experimental_graduation_cap"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_EXPLORE_PODCAST_SECTION,
                        "FEpodcasts_destination"
                )
        );

        final StringFilterGroup transcriptSection = new StringFilterGroup(
                Settings.HIDE_TRANSCRIPT_SECTION,
                "transcript_section"
        );

        final StringFilterGroup howThisWasMadeSection = new StringFilterGroup(
                Settings.HIDE_HOW_THIS_WAS_MADE_SECTION,
                "how_this_was_made_section"
        );

        final StringFilterGroup courseProgressSection = new StringFilterGroup(
                Settings.HIDE_COURSE_PROGRESS_SECTION,
                "course_progress"
        );

        final StringFilterGroup hypePoints = new StringFilterGroup(
                Settings.HIDE_HYPE_POINTS,
                "hype_points_factoid"
        );

        final StringFilterGroup infoCardsSection = new StringFilterGroup(
                Settings.HIDE_INFO_CARDS_SECTION,
                INFOCARDS_SECTION_PATH
        );

        subscribeButton = new StringFilterGroup(
                Settings.HIDE_SUBSCRIBE_BUTTON,
                "subscribe_button"
        );

        macroMarkersCarousel = new StringFilterGroup(
                null,
                "macro_markers_carousel.e"
        );

        macroMarkersCarouselGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_CHAPTERS_SECTION,
                        "chapters_horizontal_shelf",
                        "auto-chapters",
                        "description-chapters"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_KEY_CONCEPTS_SECTION,
                        "learning_concept_macro_markers_carousel_shelf",
                        "learning-concept"
                )
        );

        addPathCallbacks(
                aiGeneratedVideoSummarySection,
                askSection,
                courseProgressSection,
                featuredLinksSection,
                featuredVideosSection,
                howThisWasMadeSection,
                hypePoints,
                infoCardsSection,
                macroMarkersCarousel,
                playlistSection,
                subscribeButton,
                transcriptSection
        );
    }

    @Override
    public boolean isFiltered(String identifier, String accessibility, String path, byte[] buffer,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        // The description panel can be opened in both the regular player and Shorts.
        // If the description panel is opened in a Shorts, PlayerType is 'HIDDEN',
        // so 'PlayerType.getCurrent().isMaximizedOrFullscreen()' does not guarantee that the description panel is open.
        // Instead, use the engagement id to check if the description panel is opened.
        if (!EngagementPanel.isDescription()
                // The user can minimize the player while the engagement panel is open.
                //
                // In this case, the engagement panel is treated as open.
                // (If the player is dismissed, the engagement panel is considered closed)
                //
                // Therefore, the following exceptions can occur:
                // 1. The user opened a regular video and opened the description panel.
                // 2. The 'horizontalShelf' elements were hidden.
                // 3. The user minimized the player.
                // 4. The user manually refreshed the library tab without dismissing the player.
                // 5. Since the engagement panel is treated as open, the history shelf is filtered.
                //
                // To handle these exceptions, filtering is not performed even when the player is minimized.
                || PlayerType.getCurrent() == PlayerType.WATCH_WHILE_MINIMIZED
        ) {
            return false;
        }

        if (matchedGroup == featuredLinksSection || matchedGroup == featuredVideosSection || matchedGroup == subscribeButton) {
            return path.startsWith(INFOCARDS_SECTION_PATH);
        }

        if (matchedGroup == playlistSection) {
            if (contentIndex != 0) return false;
            return Settings.HIDE_EXPLORE_SECTION.get() || playlistSectionGroupList.check(buffer).isFiltered();
        }

        if (matchedGroup == macroMarkersCarousel) {
            return contentIndex == 0 && macroMarkersCarouselGroupList.check(buffer).isFiltered();
        }

        return true;
    }
}
