package app.revanced.integrations.patches.components;

import androidx.annotation.Nullable;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.StringTrieSearch;

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

        final StringFilterGroup chapterSection = new StringFilterGroup(
                SettingsEnum.HIDE_CHAPTERS,
                "macro_markers_carousel"
        );

        final StringFilterGroup infoCardsSection = new StringFilterGroup(
                SettingsEnum.HIDE_INFO_CARDS_SECTION,
                "infocards_section"
        );

        final StringFilterGroup gameSection = new StringFilterGroup(
                SettingsEnum.HIDE_GAME_SECTION,
                "gaming_section"
        );

        final StringFilterGroup musicSection = new StringFilterGroup(
                SettingsEnum.HIDE_MUSIC_SECTION,
                "music_section",
                "video_attributes_section"
        );

        final StringFilterGroup podcastSection = new StringFilterGroup(
                SettingsEnum.HIDE_PODCAST_SECTION,
                "playlist_section"
        );

        final StringFilterGroup transcriptSection = new StringFilterGroup(
                SettingsEnum.HIDE_TRANSCIPT_SECTION,
                "transcript_section"
        );

        pathFilterGroupList.addAll(
                chapterSection,
                infoCardsSection,
                gameSection,
                musicSection,
                podcastSection,
                transcriptSection
        );
    }


    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (exceptions.matches(path)) return false;

        return super.isFiltered(path, identifier, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
    }
}