package app.revanced.extension.youtube.patches.components;

import app.revanced.extension.shared.StringTrieSearch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
final class DescriptionComponentsFilter extends Filter {

    private final StringTrieSearch exceptions = new StringTrieSearch();

    private final ByteArrayFilterGroupList macroMarkersCarouselGroupList = new ByteArrayFilterGroupList();

    private final StringFilterGroup macroMarkersCarousel;

    private final StringFilterGroup horizontalShelf;
    private final ByteArrayFilterGroup cellVideoAttribute;

    private final StringFilterGroup aiGeneratedVideoSummarySection;

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

        final StringFilterGroup infoCardsSection = new StringFilterGroup(
                Settings.HIDE_INFO_CARDS_SECTION,
                "infocards_section"
        );

        final StringFilterGroup podcastSection = new StringFilterGroup(
                Settings.HIDE_PODCAST_SECTION,
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

        macroMarkersCarousel = new StringFilterGroup(
                null,
                "macro_markers_carousel.e"
        );

        macroMarkersCarouselGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_CHAPTERS_SECTION,
                        "chapters_horizontal_shelf"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_KEY_CONCEPTS_SECTION,
                        "learning_concept_macro_markers_carousel_shelf"
                )
        );

        horizontalShelf = new StringFilterGroup(
                Settings.HIDE_ATTRIBUTES_SECTION,
                "horizontal_shelf.e"
        );

        cellVideoAttribute = new ByteArrayFilterGroup(
                null,
                "cell_video_attribute"
        );

        addPathCallbacks(
                aiGeneratedVideoSummarySection,
                askSection,
                attributesSection,
                infoCardsSection,
                horizontalShelf,
                howThisWasMadeSection,
                macroMarkersCarousel,
                podcastSection,
                transcriptSection
        );
    }

    @Override
    boolean isFiltered(String identifier, String path, byte[] buffer,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {

        if (matchedGroup == aiGeneratedVideoSummarySection) {
            // Only hide if player is open, in case this component is used somewhere else.
            return PlayerType.getCurrent().isMaximizedOrFullscreen();
        }

        if (exceptions.matches(path)) return false;

        if (matchedGroup == macroMarkersCarousel) {
            return contentIndex == 0 && macroMarkersCarouselGroupList.check(buffer).isFiltered();
        }

        if (matchedGroup == horizontalShelf) {
            return cellVideoAttribute.check(buffer).isFiltered();
        }

        return true;
    }
}
