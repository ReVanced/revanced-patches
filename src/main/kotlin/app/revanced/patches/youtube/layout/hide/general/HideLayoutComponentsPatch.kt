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
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
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
        SettingsPatch::class,
        AddResourcesPatch::class
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
    private const val CUSTOM_FILTER_CLASS_NAME =
        "Lapp/revanced/integrations/youtube/patches/components/CustomFilter;"


    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference("revanced_hide_gray_separator"),
            SwitchPreference("revanced_hide_join_membership_button"),
            SwitchPreference("revanced_hide_channel_watermark"),
            SwitchPreference("revanced_hide_for_you_shelf"),
            SwitchPreference("revanced_hide_notify_me_button"),
            SwitchPreference("revanced_hide_timed_reactions"),
            SwitchPreference("revanced_hide_search_result_recommendations"),
            SwitchPreference("revanced_hide_search_result_shelf_header"),
            SwitchPreference("revanced_hide_channel_guidelines"),
            SwitchPreference("revanced_hide_expandable_chip"),
            SwitchPreference("revanced_hide_video_quality_menu_footer"),
            SwitchPreference("revanced_hide_chapters"),
            SwitchPreference("revanced_hide_community_posts"),
            SwitchPreference("revanced_hide_compact_banner"),
            SwitchPreference("revanced_hide_movies_section"),
            SwitchPreference("revanced_hide_feed_survey"),
            SwitchPreference("revanced_hide_community_guidelines"),
            SwitchPreference("revanced_hide_subscribers_community_guidelines"),
            SwitchPreference("revanced_hide_channel_member_shelf"),
            SwitchPreference("revanced_hide_emergency_box"),
            SwitchPreference("revanced_hide_info_panels"),
            SwitchPreference("revanced_hide_medical_panels"),
            SwitchPreference("revanced_hide_channel_bar"),
            SwitchPreference("revanced_hide_quick_actions"),
            SwitchPreference("revanced_hide_related_videos"),
            SwitchPreference("revanced_hide_image_shelf"),
            SwitchPreference("revanced_hide_latest_posts_ads"),
            SwitchPreference("revanced_hide_mix_playlists"),
            SwitchPreference("revanced_hide_artist_cards"),
            SwitchPreference("revanced_hide_chips_shelf"),
            app.revanced.patches.shared.misc.settings.preference.PreferenceScreen(
                "revanced_hide_description_components_preference_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_info_cards_section"),
                    SwitchPreference("revanced_hide_game_section"),
                    SwitchPreference("revanced_hide_music_section"),
                    SwitchPreference("revanced_hide_podcast_section"),
                    SwitchPreference("revanced_hide_transcript_section"),
                )
            ),
            app.revanced.patches.shared.misc.settings.preference.PreferenceScreen(
                "revanced_custom_filter_preference_screen",
                preferences = setOf(
                    SwitchPreference("revanced_custom_filter"),
                    // TODO: This should be a dynamic ListPreference, which does not exist yet
                    TextPreference("revanced_custom_filter_strings", inputType = InputType.TEXT_MULTI_LINE)
                )
            )
        )

        LithoFilterPatch.addFilter(LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR)
        LithoFilterPatch.addFilter(DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME)
        LithoFilterPatch.addFilter(CUSTOM_FILTER_CLASS_NAME)

        // region Mix playlists

        ParseElementFromBufferFingerprint.result?.let { result ->
            val returnEmptyComponentInstruction =
                result.mutableMethod.getInstructions().last { it.opcode == Opcode.INVOKE_STATIC }

            result.mutableMethod.apply {
                val consumeByteBufferIndex = result.scanResult.patternScanResult!!.startIndex
                val conversionContextRegister =
                    getInstruction<TwoRegisterInstruction>(consumeByteBufferIndex - 2).registerA
                val byteBufferRegister = getInstruction<FiveRegisterInstruction>(consumeByteBufferIndex).registerD

                addInstructionsWithLabels(
                    consumeByteBufferIndex, """
                        invoke-static {v$conversionContextRegister, v$byteBufferRegister}, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->filterMixPlaylists(Ljava/lang/Object;[B)Z
                        move-result v0 # Conveniently same register happens to be free. 
                        if-nez v0, :return_empty_component
                    """, ExternalLabel("return_empty_component", returnEmptyComponentInstruction)
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
                index, """
                    invoke-static {}, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->showWatermark()Z
                    move-result p2
                """
            )
        } ?: throw ShowWatermarkFingerprint.exception

        // endregion
    }
}
