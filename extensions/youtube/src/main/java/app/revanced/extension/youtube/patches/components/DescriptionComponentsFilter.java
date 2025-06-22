package app.revanced.extension.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.StringTrieSearch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
final class DescriptionComponentsFilter extends Filter {

    private final StringTrieSearch exceptions = new StringTrieSearch();

    private final ByteArrayFilterGroupList macroMarkersCarouselGroupList = new ByteArrayFilterGroupList();

    private final StringFilterGroup macroMarkersCarousel;
    private final StringFilterGroup attributesSection;
    private final ByteArrayFilterGroup cellVideoAttribute;

    public DescriptionComponentsFilter() {
        exceptions.addPatterns(
                "compact_channel",
                "description",
                "grid_video",
                "inline_expander",
                "metadata"
        );

        final StringFilterGroup aiGeneratedVideoSummarySection = new StringFilterGroup(
                Settings.HIDE_AI_GENERATED_VIDEO_SUMMARY_SECTION,
                "cell_expandable_metadata.eml"
        );

        final StringFilterGroup askSection = new StringFilterGroup(
                Settings.HIDE_ASK_SECTION,
                "youchat_entrypoint.eml"
        );

        attributesSection = new StringFilterGroup(
                Settings.HIDE_ATTRIBUTES_SECTION,
                // "gaming_section", "music_section"
                "horizontal_shelf.eml"
                "video_attributes_section"
        );

        cellVideoAttribute = new ByteArrayFilterGroup(
                null,
                "cell_video_attribute"
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
                "macro_markers_carousel.eml"
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

        addPathCallbacks(
                aiGeneratedVideoSummarySection,
                askSection,
                attributesSection,
                infoCardsSection,
                howThisWasMadeSection,
                podcastSection,
                transcriptSection,
                macroMarkersCarousel
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (exceptions.matches(path)) return false;

        if (matchedGroup == macroMarkersCarousel) {
            return contentIndex == 0 && macroMarkersCarouselGroupList.check(protobufBufferArray).isFiltered();
        }

        if (matchedGroup == attributesSection) {
            return cellVideoAttribute.check(protobufBufferArray).isFiltered();
        }

        return true;
    }
}
