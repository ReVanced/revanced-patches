package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.layout.hide.general.fingerprints.ParseElementFromBufferFingerprint
import app.revanced.patches.youtube.layout.hide.general.fingerprints.PlayerOverlayFingerprint
import app.revanced.patches.youtube.layout.hide.general.fingerprints.ShowWatermarkFingerprint
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch.PreferenceScreen
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide layout components",
    description = "Adds options to hide general layout components.",
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.35"
            ]
        )
    ]
)
@Suppress("unused")
object HideLayoutComponentsPatch : BytecodePatch(
    setOf(ParseElementFromBufferFingerprint, PlayerOverlayFingerprint)
) {
    private const val LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/LayoutComponentsFilter;"
    private const val DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME =
        "Lapp/revanced/integrations/youtube/patches/components/DescriptionComponentsFilter;"

    override fun execute(context: BytecodeContext) {
        PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_gray_separator",
                StringResource("revanced_hide_gray_separator_title", "Hide gray separator"),
                StringResource("revanced_hide_gray_separator_summary_on", "Gray separators are hidden"),
                StringResource("revanced_hide_gray_separator_summary_off", "Gray separators are shown")
            ),
            SwitchPreference(
                "revanced_hide_join_membership_button",
                StringResource("revanced_hide_join_membership_button_title", "Hide \\\'Join\\\' button"),
                StringResource("revanced_hide_join_membership_button_summary_on", "Button is hidden"),
                StringResource("revanced_hide_join_membership_button_summary_off", "Button is shown")
            ),
            SwitchPreference(
                "revanced_hide_channel_watermark_title",
                StringResource(
                    "revanced_hide_channel_watermark_title",
                    "Hide channel watermark in video player"
                ),
                StringResource("revanced_hide_channel_watermark_title_summary_on", "Watermark is hidden"),
                StringResource("revanced_hide_channel_watermark_title_summary_off", "Watermark is shown")
            ),
            SwitchPreference(
                "revanced_hide_for_you_shelf",
                StringResource("revanced_hide_for_you_shelf_title", "Hide \\\'For you\\\' shelf in channel page"),
                StringResource("revanced_hide_for_you_shelf_summary_on", "Shelf is hidden"),
                StringResource("revanced_hide_for_you_shelf_summary_off", "Shelf is shown")
            ),
            SwitchPreference(
                "revanced_hide_notify_me_button",
                StringResource("revanced_hide_notify_me_button_title", "Hide \\\'Notify me\\\' button"),
                StringResource("revanced_hide_notify_me_button_summary_on", "Button is hidden"),
                StringResource("revanced_hide_notify_me_button_summary_off", "Button is shown")
            ),
            SwitchPreference(
                "revanced_hide_timed_reactions",
                StringResource("revanced_hide_timed_reactions_title", "Hide timed reactions"),
                StringResource("revanced_hide_timed_reactions_summary_on", "Timed reactions are hidden"),
                StringResource("revanced_hide_timed_reactions_summary_off", "Timed reactions are shown")
            ),
            SwitchPreference(
                "revanced_hide_search_result_recommendations",
                StringResource(
                    "revanced_hide_search_result_recommendations_title",
                    "Hide \\\'People also watched\\\' recommendations"
                ),
                StringResource(
                    "revanced_hide_search_result_recommendations_summary_on",
                    "Recommendations are hidden"
                ),
                StringResource(
                    "revanced_hide_search_result_recommendations_summary_off",
                    "Recommendations are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_search_result_shelf_header",
                StringResource(
                    "revanced_hide_search_result_shelf_header_title",
                    "Hide search result shelf header"
                ),
                StringResource(
                    "revanced_hide_search_result_shelf_header_summary_on",
                    "Shelf header is hidden"
                ),
                StringResource(
                    "revanced_hide_search_result_shelf_header_summary_off",
                    "Shelf header is shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_channel_guidelines",
                StringResource("revanced_hide_channel_guidelines_title", "Hide channel guidelines"),
                StringResource(
                    "revanced_hide_channel_guidelines_summary_on",
                    "Channel guidelines are hidden"
                ),
                StringResource(
                    "revanced_hide_channel_guidelines_summary_off",
                    "Channel guidelines are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_expandable_chip",
                StringResource(
                    "revanced_hide_expandable_chip_title",
                    "Hide expandable chip under videos"
                ),
                StringResource(
                    "revanced_hide_expandable_chip_summary_on",
                    "Expandable chips are hidden"
                ),
                StringResource(
                    "revanced_hide_expandable_chip_summary_off",
                    "Expandable chips are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_video_quality_menu_footer",
                StringResource(
                    "revanced_hide_video_quality_menu_footer_title",
                    "Hide video quality menu footer"
                ),
                StringResource(
                    "revanced_hide_video_quality_menu_footer_summary_on",
                    "Video quality menu footer is hidden"
                ),
                StringResource(
                    "revanced_hide_video_quality_menu_footer_summary_off",
                    "Video quality menu footer is shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_chapters",
                StringResource(
                    "revanced_hide_chapters_title",
                    "Hide chapters in the video description"
                ),
                StringResource(
                    "revanced_hide_chapters_summary_on",
                    "Chapters are hidden"
                ),
                StringResource(
                    "revanced_hide_chapters_summary_off",
                    "Chapters are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_community_posts",
                StringResource("revanced_hide_community_posts_title", "Hide community posts"),
                StringResource("revanced_hide_community_posts_summary_on", "Community posts are hidden"),
                StringResource("revanced_hide_community_posts_summary_off", "Community posts are shown")
            ),
            SwitchPreference(
                "revanced_hide_compact_banner",
                StringResource("revanced_hide_compact_banner_title", "Hide compact banners"),
                StringResource("revanced_hide_compact_banner_summary_on", "Compact banners are hidden"),
                StringResource("revanced_hide_compact_banner_summary_off", "Compact banners are shown")
            ),
            SwitchPreference(
                "revanced_hide_movies_section",
                StringResource("revanced_hide_movies_section_title", "Hide movies section"),
                StringResource("revanced_hide_movies_section_summary_on", "Movies section is hidden"),
                StringResource("revanced_hide_movies_section_summary_off", "Movies section is shown")
            ),
            SwitchPreference(
                "revanced_hide_feed_survey",
                StringResource("revanced_hide_feed_survey_title", "Hide feed surveys"),
                StringResource("revanced_hide_feed_survey_summary_on", "Feed surveys are hidden"),
                StringResource("revanced_hide_feed_survey_summary_off", "Feed surveys are shown")
            ),
            SwitchPreference(
                "revanced_hide_community_guidelines",
                StringResource("revanced_hide_community_guidelines_title", "Hide community guidelines"),
                StringResource(
                    "revanced_hide_community_guidelines_summary_on",
                    "Community guidelines are hidden"
                ),
                StringResource(
                    "revanced_hide_community_guidelines_summary_off",
                    "Community guidelines are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_subscribers_community_guidelines",
                StringResource(
                    "revanced_hide_subscribers_community_guidelines_title",
                    "Hide subscribers community guidelines"
                ),
                StringResource(
                    "revanced_hide_subscribers_community_guidelines_summary_on",
                    "Subscribers community guidelines are hidden"
                ),
                StringResource(
                    "revanced_hide_subscribers_community_guidelines_summary_off",
                    "Subscribers community guidelines are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_channel_member_shelf",
                StringResource("revanced_hide_channel_member_shelf_title", "Hide channel member shelf"),
                StringResource(
                    "revanced_hide_channel_member_shelf_summary_on",
                    "Channel member shelf is hidden"
                ),
                StringResource(
                    "revanced_hide_channel_member_shelf_summary_off",
                    "Channel member shelf is shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_emergency_box",
                StringResource("revanced_hide_emergency_box_title", "Hide emergency boxes"),
                StringResource("revanced_hide_emergency_box_summary_on", "Emergency boxes are hidden"),
                StringResource("revanced_hide_emergency_box_summary_off", "Emergency boxes are shown")
            ),
            SwitchPreference(
                "revanced_hide_info_panels",
                StringResource("revanced_hide_info_panels_title", "Hide info panels"),
                StringResource("revanced_hide_info_panels_summary_on", "Info panels are hidden"),
                StringResource("revanced_hide_info_panels_summary_off", "Info panels are shown")
            ),
            SwitchPreference(
                "revanced_hide_medical_panels",
                StringResource("revanced_hide_medical_panels_title", "Hide medical panels"),
                StringResource("revanced_hide_medical_panels_summary_on", "Medical panels are hidden"),
                StringResource("revanced_hide_medical_panels_summary_off", "Medical panels are shown")
            ),
            SwitchPreference(
                "revanced_hide_channel_bar",
                StringResource("revanced_hide_channel_bar_title", "Hide channel bar"),
                StringResource("revanced_hide_channel_bar_summary_on", "Channel bar is hidden"),
                StringResource("revanced_hide_channel_bar_summary_off", "Channel bar is shown")
            ),
            SwitchPreference(
                "revanced_hide_quick_actions",
                StringResource("revanced_hide_quick_actions_title", "Hide quick actions in fullscreen"),
                StringResource("revanced_hide_quick_actions_summary_on", "Quick actions are hidden"),
                StringResource("revanced_hide_quick_actions_summary_off", "Quick actions are shown")
            ),
            SwitchPreference(
                "revanced_hide_related_videos",
                StringResource("revanced_hide_related_videos_title", "Hide related videos in quick actions"),
                StringResource("revanced_hide_related_videos_summary_on", "Related videos are hidden"),
                StringResource("revanced_hide_related_videos_summary_off", "Related videos are shown")
            ),
            SwitchPreference(
                "revanced_hide_image_shelf",
                StringResource("revanced_hide_image_shelf", "Hide image shelf in search results"),
                StringResource("revanced_hide_image_shelf_summary_on", "Image shelf is hidden"),
                StringResource("revanced_hide_image_shelf_summary_off", "Image shelf is shown")
            ),
            SwitchPreference(
                "revanced_hide_latest_posts_ads",
                StringResource("revanced_hide_latest_posts_ads_title", "Hide latest posts"),
                StringResource("revanced_hide_latest_posts_ads_summary_on", "Latest posts are hidden"),
                StringResource("revanced_hide_latest_posts_ads_summary_off", "Latest posts are shown")
            ),
            SwitchPreference(
                "revanced_hide_mix_playlists",
                StringResource("revanced_hide_mix_playlists_title", "Hide mix playlists"),
                StringResource("revanced_hide_mix_playlists_summary_on", "Mix playlists are hidden"),
                StringResource("revanced_hide_mix_playlists_summary_off", "Mix playlists are shown")
            ),
            SwitchPreference(
                "revanced_hide_artist_cards",
                StringResource("revanced_hide_artist_cards_title", "Hide artist cards"),
                StringResource("revanced_hide_artist_cards_on", "Artist cards are hidden"),
                StringResource("revanced_hide_artist_cards_off", "Artist cards are shown")
            ),
            SwitchPreference(
                "revanced_hide_chips_shelf",
                StringResource("revanced_hide_chips_shelf_title", "Hide chips shelf"),
                StringResource("revanced_hide_chips_shelf_on", "Chips shelf is hidden"),
                StringResource("revanced_hide_chips_shelf_off", "Chips shelf is shown")
            ),
            PreferenceScreen(
                "revanced_hide_description_components_preference_screen",
                StringResource(
                    "revanced_hide_description_components_preference_screen_title",
                    "Hide components in the video description"
                ),
                listOf(
                    SwitchPreference(
                        "revanced_hide_info_cards_section",
                        StringResource(
                            "revanced_hide_info_cards_section_title",
                            "Hide info cards section"
                        ),
                        StringResource(
                            "revanced_hide_info_cards_section_summary_on",
                            "Info cards section is hidden"
                        ),
                        StringResource(
                            "revanced_hide_info_cards_section_summary_off",
                            "Info cards section is shown"
                        )
                    ),
                    SwitchPreference(
                        "revanced_hide_game_section",
                        StringResource(
                            "revanced_hide_game_section_title",
                            "Hide game section"
                        ),
                        StringResource(
                            "revanced_hide_game_section_summary_on",
                            "Game section is hidden"
                        ),
                        StringResource(
                            "revanced_hide_game_section_summary_off",
                            "Game section is shown"
                        )
                    ),
                    SwitchPreference(
                        "revanced_hide_music_section",
                        StringResource(
                            "revanced_hide_music_section_title",
                            "Hide music section"
                        ),
                        StringResource(
                            "revanced_hide_music_section_summary_on",
                            "Music section is hidden"
                        ),
                        StringResource(
                            "revanced_hide_music_section_summary_off",
                            "Music section is shown"
                        )
                    ),
                    SwitchPreference(
                        "revanced_hide_podcast_section",
                        StringResource(
                            "revanced_hide_podcast_section_title",
                            "Hide podcast section"
                        ),
                        StringResource(
                            "revanced_hide_podcast_section_summary_on",
                            "Podcast section is hidden"
                        ),
                        StringResource(
                            "revanced_hide_podcast_section_summary_off",
                            "Podcast section is shown"
                        )
                    ),
                    SwitchPreference(
                        "revanced_hide_transcript_section",
                        StringResource(
                            "revanced_hide_transcript_section_title",
                            "Hide transcript section"
                        ),
                        StringResource(
                            "revanced_hide_transcript_section_summary_on",
                            "Transcript section is hidden"
                        ),
                        StringResource(
                            "revanced_hide_transcript_section_summary_off",
                            "Transcript section is shown"
                        )
                    ),
                ),
                StringResource(
                    "revanced_hide_description_components_preference_screen_summary",
                    "Hide components under the video description"
                )
            ),
            PreferenceScreen(
                "revanced_custom_filter_preference_screen",
                StringResource("revanced_custom_filter_preference_screen_title", "Custom filter"),
                listOf(
                    SwitchPreference(
                        "revanced_custom_filter",
                        StringResource(
                            "revanced_custom_filter_title",
                            "Enable custom filter"
                        ),
                        StringResource(
                            "revanced_custom_filter_summary_on",
                            "Custom filter is enabled"
                        ),
                        StringResource(
                            "revanced_custom_filter_summary_off",
                            "Custom filter is disabled"
                        )
                    ),
                    // TODO: This should be a dynamic ListPreference, which does not exist yet
                    TextPreference(
                        "revanced_custom_filter_strings",
                        StringResource("revanced_custom_filter_strings_title", "Custom filter"),
                        StringResource(
                            "revanced_custom_filter_strings_summary",
                            "List of component path builder strings to filter separated by new line"
                        ),
                        inputType = InputType.TEXT_MULTI_LINE
                    )
                ),
                StringResource(
                    "revanced_custom_filter_preference_screen_summary",
                    "Hide components using custom filters"
                )
            )
        )

        LithoFilterPatch.addFilter(LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR)
        LithoFilterPatch.addFilter(DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME)

        // region Mix playlists

        ParseElementFromBufferFingerprint.result?.let { result ->
            val returnEmptyComponentInstruction = result.mutableMethod.getInstructions()
                .last { it.opcode == Opcode.INVOKE_STATIC }

            result.mutableMethod.apply {
                val consumeByteBufferIndex = result.scanResult.patternScanResult!!.startIndex
                val conversionContextRegister =
                    getInstruction<TwoRegisterInstruction>(consumeByteBufferIndex - 2).registerA
                val byteBufferRegister =
                    getInstruction<FiveRegisterInstruction>(consumeByteBufferIndex).registerD

                addInstructionsWithLabels(
                    consumeByteBufferIndex,
                    """
                        invoke-static {v$conversionContextRegister, v$byteBufferRegister}, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->filterMixPlaylists(Ljava/lang/Object;[B)Z
                        move-result v0 # Conveniently same register happens to be free. 
                        if-nez v0, :return_empty_component
                    """,
                    ExternalLabel("return_empty_component", returnEmptyComponentInstruction)
                )
            }

        } ?: throw ParseElementFromBufferFingerprint.exception

        // endregion

        // region Watermark (legacy code for old versions of YouTube)

        ShowWatermarkFingerprint.also {
            it.resolve(context, PlayerOverlayFingerprint.result?.classDef ?: throw PlayerOverlayFingerprint.exception)
        }.result?.mutableMethod?.apply {
            val index = implementation!!.instructions.size - 5

            removeInstruction(index)
            addInstructions(
                index,
                """
                    invoke-static {}, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->showWatermark()Z
                    move-result p2
                """
            )
        } ?: throw ShowWatermarkFingerprint.exception

        // endregion
    }
}
