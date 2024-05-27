package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.youtube.layout.hide.general.fingerprints.*
import app.revanced.patches.youtube.layout.hide.general.fingerprints.parseElementFromBufferFingerprint
import app.revanced.patches.youtube.misc.litho.filter.addFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.sun.org.apache.bcel.internal.generic.InstructionConst.getInstruction

@Suppress("unused")
val hideLayoutComponentsPatch = bytecodePatch(
    name = "Hide layout components",
    description = "Adds options to hide general layout components.",
) {
    dependsOn(
        lithoFilterPatch,
        settingsPatch,
        addResourcesPatch,
        hideLayoutComponentsResourcePatch,
        navigationBarHookPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
            "19.11.43",
        ),
    )

    val parseElementFromBufferResult by parseElementFromBufferFingerprint
    val playerOverlayResult by playerOverlayFingerprint
    val hideShowMoreButtonResult by hideShowMoreButtonFingerprint

    execute { context ->
        val layoutComponentsFilterClassDescriptor =
            "Lapp/revanced/integrations/youtube/patches/components/LayoutComponentsFilter;"
        val descriptionComponentsFilterClassName =
            "Lapp/revanced/integrations/youtube/patches/components/DescriptionComponentsFilter;"
        val keywordFilterClassName =
            "Lapp/revanced/integrations/youtube/patches/components/KeywordContentFilter;"
        val customFilterClassName =
            "Lapp/revanced/integrations/youtube/patches/components/CustomFilter;"

        addResources("youtube", "layout.hide.general.HideLayoutComponentsPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_channel_bar"),
            SwitchPreference("revanced_hide_channel_guidelines"),
            SwitchPreference("revanced_hide_channel_member_shelf"),
            SwitchPreference("revanced_hide_channel_watermark"),
            SwitchPreference("revanced_hide_chips_shelf"),
            SwitchPreference("revanced_hide_community_guidelines"),
            PreferenceScreenPreference(
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
            SwitchPreference("revanced_hide_quick_actions"),
            SwitchPreference("revanced_hide_related_videos"),
            SwitchPreference("revanced_hide_subscribers_community_guidelines"),
            SwitchPreference("revanced_hide_timed_reactions"),
        )

        PreferenceScreen.FEED.addPreferences(
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
            SwitchPreference("revanced_hide_playables"),
            SwitchPreference("revanced_hide_search_result_recommendations"),
            SwitchPreference("revanced_hide_search_result_shelf_header"),
            SwitchPreference("revanced_hide_show_more_button"),
            PreferenceScreenPreference(
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

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_hide_gray_separator"),
            PreferenceScreenPreference(
                key = "revanced_custom_filter_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_custom_filter"),
                    // TODO: This should be a dynamic ListPreference, which does not exist yet
                    TextPreference("revanced_custom_filter_strings", inputType = InputType.TEXT_MULTI_LINE),
                ),
            ),
        )

        PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_hide_video_quality_menu_footer"),
        )

        addFilter(layoutComponentsFilterClassDescriptor)
        addFilter(descriptionComponentsFilterClassName)
        addFilter(keywordFilterClassName)
        addFilter(customFilterClassName)

        // region Mix playlists

        val consumeByteBufferIndex = parseElementFromBufferResult.scanResult.patternScanResult!!.startIndex

        parseElementFromBufferResult.mutableMethod.apply {
            val conversionContextRegister =
                getInstruction<TwoRegisterInstruction>(consumeByteBufferIndex - 2).registerA
            val byteBufferRegister = getInstruction<FiveRegisterInstruction>(consumeByteBufferIndex).registerD
            val returnEmptyComponentInstruction = getInstructions().last { it.opcode == Opcode.INVOKE_STATIC }

            addInstructionsWithLabels(
                consumeByteBufferIndex,
                """
                        invoke-static {v$conversionContextRegister, v$byteBufferRegister}, $layoutComponentsFilterClassDescriptor->filterMixPlaylists(Ljava/lang/Object;[B)Z
                        move-result v0 # Conveniently same register happens to be free. 
                        if-nez v0, :return_empty_component
                    """,
                ExternalLabel("return_empty_component", returnEmptyComponentInstruction),
            )
        }

        // endregion

        // region Watermark (legacy code for old versions of YouTube)

        showWatermarkFingerprint.apply {
            resolve(context, playerOverlayResult.classDef)
        }.resultOrThrow().mutableMethod.apply {
            val index = implementation!!.instructions.size - 5

            removeInstruction(index)
            addInstructions(
                index,
                """
                    invoke-static {}, $layoutComponentsFilterClassDescriptor->showWatermark()Z
                    move-result p2
                """,
            )
        }

        // endregion

        // region Show more button

        hideShowMoreButtonResult.mutableMethod.apply {
            val moveRegisterIndex = hideShowMoreButtonResult.scanResult.patternScanResult!!.endIndex
            val viewRegister =
                getInstruction<OneRegisterInstruction>(moveRegisterIndex).registerA

            val insertIndex = moveRegisterIndex + 1
            addInstruction(
                insertIndex,
                "invoke-static { v$viewRegister }, " +
                    "$layoutComponentsFilterClassDescriptor->hideShowMoreButton(Landroid/view/View;)V",
            )
        }

        // endregion
    }
}
