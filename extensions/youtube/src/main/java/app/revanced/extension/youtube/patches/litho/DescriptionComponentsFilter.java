package app.revanced.extension.youtube.patches.litho;

import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.shared.StringTrieSearch;
import app.revanced.extension.shared.patches.litho.FilterGroup.*;
import app.revanced.extension.shared.patches.litho.FilterGroupList.ByteArrayFilterGroupList;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class DescriptionComponentsFilter extends Filter {

    private static final String INFOCARDS_SECTION_PATH = "infocards_section.e";

    private final StringTrieSearch exceptions = new StringTrieSearch();
    private final StringFilterGroup macroMarkersCarousel;
    private final ByteArrayFilterGroupList macroMarkersCarouselGroupList = new ByteArrayFilterGroupList();
    private final StringFilterGroup horizontalShelf;
    private final ByteArrayFilterGroupList horizontalShelfGroupList = new ByteArrayFilterGroupList();
    private final StringFilterGroup infoCardsSection;
    private final StringFilterGroup featuredLinksSection;
    private final StringFilterGroup featuredVideosSection;
    private final StringFilterGroup subscribeButton;
    private final StringFilterGroup aiGeneratedVideoSummarySection;
    private final StringFilterGroup hypePoints;

    public DescriptionComponentsFilter() {
        exceptions.addPatterns(
                "compact_channel",
                "description",
                "grid_video",
                "inline_expander",
                "metadata"
        );

        aiGeneratedVideoSummarySection = new StringFilterGroup(
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

        final StringFilterGroup podcastSection = new StringFilterGroup(
                Settings.HIDE_EXPLORE_PODCAST_SECTION,
                "playlist_section"
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

        hypePoints = new StringFilterGroup(
                Settings.HIDE_HYPE_POINTS,
                "hype_points_factoid"
        );

        infoCardsSection = new StringFilterGroup(
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

        horizontalShelf = new StringFilterGroup(
                null,
                "horizontal_shelf.e"
        );

        horizontalShelfGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_ATTRIBUTES_SECTION,
                        // May no longer work on v20.31+, even though the component is still there.
                        "cell_video_attribute"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_FEATURED_PLACES_SECTION,
                        "yt_fill_star",
                        "yt_fill_experimental_star"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_GAMING_SECTION,
                        "yt_outline_gaming",
                        "yt_outline_experimental_gaming"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_MUSIC_SECTION,
                        "yt_outline_audio",
                        "yt_outline_experimental_audio"
                )
        );

        addPathCallbacks(
                aiGeneratedVideoSummarySection,
                askSection,
                courseProgressSection,
                featuredLinksSection,
                featuredVideosSection,
                horizontalShelf,
                howThisWasMadeSection,
                hypePoints,
                infoCardsSection,
                macroMarkersCarousel,
                podcastSection,
                subscribeButton,
                transcriptSection
        );
    }

    @Override
    public boolean isFiltered(String identifier, String accessibility, String path, byte[] buffer,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {

        if (matchedGroup == aiGeneratedVideoSummarySection || matchedGroup == hypePoints) {
            // Only hide if player is open, in case this component is used somewhere else.
            return PlayerType.getCurrent().isMaximizedOrFullscreen();
        }

        if (matchedGroup == featuredLinksSection || matchedGroup == featuredVideosSection || matchedGroup == subscribeButton) {
            return path.startsWith(INFOCARDS_SECTION_PATH);
        }

        if (exceptions.matches(path)) return false;

        if (matchedGroup == macroMarkersCarousel) {
            return contentIndex == 0 && macroMarkersCarouselGroupList.check(buffer).isFiltered();
        }

        if (matchedGroup == horizontalShelf) {
            return horizontalShelfGroupList.check(buffer).isFiltered();
        }

        return true;
    }
}
