package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.StringTrieSearch;

@SuppressWarnings("unused")
final class DescriptionComponentsFilter extends Filter {

    private final StringTrieSearch exceptions = new StringTrieSearch();

    public DescriptionComponentsFilter() {
        exceptions.addPatterns(
                "compact_channel",
                "description",
                "grid_video",
                "inline_expander",
                "metadata"
        );

        final StringFilterGroup attributesSection = new StringFilterGroup(
                Settings.HIDE_ATTRIBUTES_SECTION,
                "gaming_section",
                "music_section",
                "video_attributes_section"
        );

        final StringFilterGroup chaptersSection = new StringFilterGroup(
                Settings.HIDE_CHAPTERS_SECTION,
                "macro_markers_carousel"
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

        addPathCallbacks(
                attributesSection,
                chaptersSection,
                infoCardsSection,
                podcastSection,
                transcriptSection
        );
    }


    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (exceptions.matches(path)) return false;

        return super.isFiltered(path, identifier, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }
}