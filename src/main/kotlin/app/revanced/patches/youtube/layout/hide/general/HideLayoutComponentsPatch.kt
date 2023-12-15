package app.revanced.patches.youtube.layout.hide.general

import app.revanced.util.exception
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
import app.revanced.patches.shared.settings.preference.impl.InputType
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.shared.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.layout.hide.general.fingerprints.ParseElementFromBufferFingerprint
import app.revanced.patches.youtube.layout.hide.general.fingerprints.PlayerOverlayFingerprint
import app.revanced.patches.youtube.layout.hide.general.fingerprints.ShowWatermarkFingerprint
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide layout components",
    description = "Hides general layout components.",
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
                "18.45.41",
                "18.45.43"
            ]
        )
    ]
)
@Suppress("unused")
object HideLayoutComponentsPatch : BytecodePatch(
    setOf(ParseElementFromBufferFingerprint, PlayerOverlayFingerprint)
) {
    private const val LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/components/LayoutComponentsFilter;"
    private const val DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME =
        "Lapp/revanced/integrations/patches/components/DescriptionComponentsFilter;"

    override fun execute(context: BytecodeContext) {
        StringsPatch.includePatchStrings("HideLayoutComponents")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_gray_separator",
                "revanced_hide_gray_separator_title",
                "revanced_hide_gray_separator_summary_on",
                "revanced_hide_gray_separator_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_join_membership_button",
                "revanced_hide_join_membership_button_title",
                "revanced_hide_join_membership_button_summary_on",
                "revanced_hide_join_membership_button_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_channel_watermark_title",
                "revanced_hide_channel_watermark_title",
                "revanced_hide_channel_watermark_title_summary_on",
                "revanced_hide_channel_watermark_title_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_for_you_shelf",
                "revanced_hide_for_you_shelf_title",
                "revanced_hide_for_you_shelf_summary_on",
                "revanced_hide_for_you_shelf_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_notify_me_button",
                "revanced_hide_notify_me_button_title",
                "revanced_hide_notify_me_button_summary_on",
                "revanced_hide_notify_me_button_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_timed_reactions",
                "revanced_hide_timed_reactions_title",
                "revanced_hide_timed_reactions_summary_on",
                "revanced_hide_timed_reactions_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_search_result_shelf_header",
                "revanced_hide_search_result_shelf_header_title",
                "revanced_hide_search_result_shelf_header_summary_on",
                "revanced_hide_search_result_shelf_header_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_channel_guidelines",
                "revanced_hide_channel_guidelines_title",
                "revanced_hide_channel_guidelines_summary_on",
                "revanced_hide_channel_guidelines_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_expandable_chip",
                "revanced_hide_expandable_chip_title",
                "revanced_hide_expandable_chip_summary_on",
                "revanced_hide_expandable_chip_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_video_quality_menu_footer",
                "revanced_hide_video_quality_menu_footer_title",
                "revanced_hide_video_quality_menu_footer_summary_on",
                "revanced_hide_video_quality_menu_footer_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_chapters",
                "revanced_hide_chapters_title",
                "revanced_hide_chapters_summary_on",
                "revanced_hide_chapters_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_community_posts",
                "revanced_hide_community_posts_title",
                "revanced_hide_community_posts_summary_on",
                "revanced_hide_community_posts_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_compact_banner",
                "revanced_hide_compact_banner_title",
                "revanced_hide_compact_banner_summary_on",
                "revanced_hide_compact_banner_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_movies_section",
                "revanced_hide_movies_section_title",
                "revanced_hide_movies_section_summary_on",
                "revanced_hide_movies_section_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_feed_survey",
                "revanced_hide_feed_survey_title",
                "revanced_hide_feed_survey_summary_on",
                "revanced_hide_feed_survey_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_community_guidelines",
                "revanced_hide_community_guidelines_title",
                "revanced_hide_community_guidelines_summary_on",
                "revanced_hide_community_guidelines_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_subscribers_community_guidelines",
                "revanced_hide_subscribers_community_guidelines_title",
                "revanced_hide_subscribers_community_guidelines_summary_on",
                "revanced_hide_subscribers_community_guidelines_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_channel_member_shelf",
                "revanced_hide_channel_member_shelf_title",
                "revanced_hide_channel_member_shelf_summary_on",
                "revanced_hide_channel_member_shelf_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_emergency_box",
                "revanced_hide_emergency_box_title",
                "revanced_hide_emergency_box_summary_on",
                "revanced_hide_emergency_box_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_info_panels",
                "revanced_hide_info_panels_title",
                "revanced_hide_info_panels_summary_on",
                "revanced_hide_info_panels_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_medical_panels",
                "revanced_hide_medical_panels_title",
                "revanced_hide_medical_panels_summary_on",
                "revanced_hide_medical_panels_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_channel_bar",
                "revanced_hide_channel_bar_title",
                "revanced_hide_channel_bar_summary_on",
                "revanced_hide_channel_bar_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_quick_actions",
                "revanced_hide_quick_actions_title",
                "revanced_hide_quick_actions_summary_on",
                "revanced_hide_quick_actions_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_related_videos",
                "revanced_hide_related_videos_title",
                "revanced_hide_related_videos_summary_on",
                "revanced_hide_related_videos_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_image_shelf",
                "revanced_hide_image_shelf_title",
                "revanced_hide_image_shelf_summary_on",
                "revanced_hide_image_shelf_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_latest_posts_ads",
                "revanced_hide_latest_posts_ads_title",
                "revanced_hide_latest_posts_ads_summary_on",
                "revanced_hide_latest_posts_ads_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_mix_playlists",
                "revanced_hide_mix_playlists_title",
                "revanced_hide_mix_playlists_summary_on",
                "revanced_hide_mix_playlists_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_artist_cards",
                "revanced_hide_artist_cards_title",
                "revanced_hide_artist_cards_on",
                "revanced_hide_artist_cards_off",
            ),
            SwitchPreference(
                "revanced_hide_chips_shelf",
                "revanced_hide_chips_shelf_title",
                "revanced_hide_chips_shelf_on",
                "revanced_hide_chips_shelf_off",
            ),
            PreferenceScreen(
                "revanced_hide_description_components_preference_screen",
                "revanced_hide_description_components_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_hide_info_cards_section",
                        "revanced_hide_info_cards_section_title",
                        "revanced_hide_info_cards_section_summary_on",
                        "revanced_hide_info_cards_section_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_game_section",
                        "revanced_hide_game_section_title",
                        "revanced_hide_game_section_summary_on",
                        "revanced_hide_game_section_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_music_section",
                        "revanced_hide_music_section_title",
                        "revanced_hide_music_section_summary_on",
                        "revanced_hide_music_section_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_podcast_section",
                        "revanced_hide_podcast_section_title",
                        "revanced_hide_podcast_section_summary_on",
                        "revanced_hide_podcast_section_summary_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_transcript_section",
                        "revanced_hide_transcript_section_title",
                        "revanced_hide_transcript_section_summary_on",
                        "revanced_hide_transcript_section_summary_off",
                    ),
                ),
                "revanced_hide_description_components_preference_screen_summary"
            ),
            PreferenceScreen(
                "revanced_custom_filter_preference_screen",
                "revanced_custom_filter_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_custom_filter",
                        "revanced_custom_filter_title",
                        "revanced_custom_filter_summary_on",
                        "revanced_custom_filter_summary_off",
                    ),
                    // TODO: This should be a dynamic ListPreference, which does not exist yet
                    TextPreference(
                        "revanced_custom_filter_strings",
                        "revanced_custom_filter_strings_title",
                        "revanced_custom_filter_strings_summary",
                        inputType = InputType.TEXT_MULTI_LINE
                    )
                ),
                "revanced_custom_filter_preference_screen_summary"
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
            it.resolve(context,
                PlayerOverlayFingerprint.result?.classDef
                    ?: throw PlayerOverlayFingerprint.exception
            )
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
