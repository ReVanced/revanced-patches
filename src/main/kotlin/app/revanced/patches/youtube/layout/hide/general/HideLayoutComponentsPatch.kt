package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen.Sorting
import app.revanced.patches.youtube.layout.hide.general.fingerprints.HideShowMoreButtonFingerprint
import app.revanced.patches.youtube.layout.hide.general.fingerprints.ParseElementFromBufferFingerprint
import app.revanced.patches.youtube.layout.hide.general.fingerprints.PlayerOverlayFingerprint
import app.revanced.patches.youtube.layout.hide.general.fingerprints.ShowWatermarkFingerprint
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.NavigationBarHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide layout components",
    description = "Adds options to hide general layout components.",
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        HideLayoutComponentsResourcePatch::class,
        NavigationBarHookPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
            ],
        ),
    ],
)
@Suppress("unused")
object HideLayoutComponentsPatch : BytecodePatch(
    setOf(ParseElementFromBufferFingerprint, PlayerOverlayFingerprint, HideShowMoreButtonFingerprint),
) {
    private const val LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/LayoutComponentsFilter;"
    private const val DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME =
        "Lapp/revanced/integrations/youtube/patches/components/DescriptionComponentsFilter;"
    private const val CUSTOM_FILTER_CLASS_NAME =
        "Lapp/revanced/integrations/youtube/patches/components/CustomFilter;"
    private const val KEYWORD_FILTER_CLASS_NAME =
        "Lapp/revanced/integrations/youtube/patches/components/KeywordContentFilter;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_channel_bar"),
            SwitchPreference("revanced_hide_channel_guidelines"),
            SwitchPreference("revanced_hide_channel_member_shelf"),
            SwitchPreference("revanced_hide_channel_watermark"),
            SwitchPreference("revanced_hide_chips_shelf"),
            SwitchPreference("revanced_hide_community_guidelines"),
            PreferenceScreen(
                key = "revanced_hide_description_components_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_chapters"),
                    SwitchPreference("revanced_hide_info_cards_section"),
                    SwitchPreference("revanced_hide_game_section"),
                    SwitchPreference("revanced_hide_music_section"),
                    SwitchPreference("revanced_hide_podcast_section"),
                    SwitchPreference("revanced_hide_transcript_section"),
                ),
            ),
            SwitchPreference("revanced_hide_emergency_box"),
            SwitchPreference("revanced_hide_expandable_chip"),
            SwitchPreference("revanced_hide_info_panels"),
            SwitchPreference("revanced_hide_join_membership_button"),
            SwitchPreference("revanced_hide_medical_panels"),
            SwitchPreference("revanced_hide_playables"),
            SwitchPreference("revanced_hide_quick_actions"),
            SwitchPreference("revanced_hide_related_videos"),
            SwitchPreference("revanced_hide_subscribers_community_guidelines"),
            SwitchPreference("revanced_hide_timed_reactions"),
        )

        SettingsPatch.PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_hide_artist_cards"),
            SwitchPreference("revanced_hide_community_posts"),
            SwitchPreference("revanced_hide_compact_banner"),
            SwitchPreference("revanced_hide_feed_survey"),
            SwitchPreference("revanced_hide_for_you_shelf"),
            SwitchPreference("revanced_hide_horizontal_shelves"),
            SwitchPreference("revanced_hide_image_shelf"),
            SwitchPreference("revanced_hide_latest_posts_ads"),
            SwitchPreference("revanced_hide_mix_playlists"),
            SwitchPreference("revanced_hide_movies_section"),
            SwitchPreference("revanced_hide_notify_me_button"),
            SwitchPreference("revanced_hide_search_result_recommendations"),
            SwitchPreference("revanced_hide_search_result_shelf_header"),
            SwitchPreference("revanced_hide_show_more_button"),
            PreferenceScreen(
                key = "revanced_hide_keyword_content_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_hide_keyword_content_home"),
                    SwitchPreference("revanced_hide_keyword_content_subscriptions"),
                    SwitchPreference("revanced_hide_keyword_content_search"),
                    TextPreference("revanced_hide_keyword_content_phrases", inputType = InputType.TEXT_MULTI_LINE),
                    NonInteractivePreference("revanced_hide_keyword_content_about"),
                ),
            ),
        )

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_hide_gray_separator"),
            PreferenceScreen(
                key = "revanced_custom_filter_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_custom_filter"),
                    // TODO: This should be a dynamic ListPreference, which does not exist yet
                    TextPreference("revanced_custom_filter_strings", inputType = InputType.TEXT_MULTI_LINE),
                ),
            ),
        )

        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_hide_video_quality_menu_footer"),
        )

        LithoFilterPatch.addFilter(LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR)
        LithoFilterPatch.addFilter(DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME)
        LithoFilterPatch.addFilter(KEYWORD_FILTER_CLASS_NAME)
        LithoFilterPatch.addFilter(CUSTOM_FILTER_CLASS_NAME)

        // region Mix playlists

        ParseElementFromBufferFingerprint.resultOrThrow().let { result ->
            val consumeByteBufferIndex = result.scanResult.patternScanResult!!.startIndex

            result.mutableMethod.apply {
                val conversionContextRegister =
                    getInstruction<TwoRegisterInstruction>(consumeByteBufferIndex - 2).registerA
                val byteBufferRegister = getInstruction<FiveRegisterInstruction>(consumeByteBufferIndex).registerD
                val returnEmptyComponentInstruction = getInstructions().last { it.opcode == Opcode.INVOKE_STATIC }

                addInstructionsWithLabels(
                    consumeByteBufferIndex,
                    """
                        invoke-static {v$conversionContextRegister, v$byteBufferRegister}, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->filterMixPlaylists(Ljava/lang/Object;[B)Z
                        move-result v0 # Conveniently same register happens to be free. 
                        if-nez v0, :return_empty_component
                    """,
                    ExternalLabel("return_empty_component", returnEmptyComponentInstruction),
                )
            }
        }

        // endregion

        // region Watermark (legacy code for old versions of YouTube)

        ShowWatermarkFingerprint.also {
            it.resolve(context, PlayerOverlayFingerprint.resultOrThrow().classDef)
        }.resultOrThrow().mutableMethod.apply {
            val index = implementation!!.instructions.size - 5

            removeInstruction(index)
            addInstructions(
                index,
                """
                    invoke-static {}, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->showWatermark()Z
                    move-result p2
                """,
            )
        }

        // endregion

        // region Show more button

        HideShowMoreButtonFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val moveRegisterIndex = it.scanResult.patternScanResult!!.endIndex
                val viewRegister =
                    getInstruction<OneRegisterInstruction>(moveRegisterIndex).registerA

                val insertIndex = moveRegisterIndex + 1
                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, " +
                        "$LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideShowMoreButton(Landroid/view/View;)V",
                )
            }
        }

        // endregion
    }
}
