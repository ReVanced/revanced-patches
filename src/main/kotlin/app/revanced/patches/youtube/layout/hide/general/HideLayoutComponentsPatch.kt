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
import app.revanced.patches.youtube.layout.hide.general.fingerprints.YoodlesImageViewFingerprint
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.NavigationBarHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.findOpcodeIndicesReversed
import app.revanced.util.getReference
import app.revanced.util.alsoResolve
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

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
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ],
        ),
    ],
)
@Suppress("unused")
object HideLayoutComponentsPatch : BytecodePatch(
    setOf(
        ParseElementFromBufferFingerprint,
        PlayerOverlayFingerprint,
        HideShowMoreButtonFingerprint,
        YoodlesImageViewFingerprint,
    ),
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
            SwitchPreference("revanced_hide_community_guidelines"),
            PreferenceScreen(
                key = "revanced_hide_description_components_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_attributes_section"),
                    SwitchPreference("revanced_hide_chapters_section"),
                    SwitchPreference("revanced_hide_info_cards_section"),
                    SwitchPreference("revanced_hide_key_concepts_section"),
                    SwitchPreference("revanced_hide_podcast_section"),
                    SwitchPreference("revanced_hide_transcript_section"),
                ),
            ),
            SwitchPreference("revanced_hide_emergency_box"),
            SwitchPreference("revanced_hide_info_panels"),
            SwitchPreference("revanced_hide_join_membership_button"),
            SwitchPreference("revanced_disable_like_subscribe_glow"),
            SwitchPreference("revanced_hide_medical_panels"),
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
            SwitchPreference("revanced_hide_playables"),
            SwitchPreference("revanced_hide_search_result_recommendations"),
            SwitchPreference("revanced_hide_search_result_shelf_header"),
            SwitchPreference("revanced_hide_show_more_button"),
            SwitchPreference("revanced_hide_doodles"),
            PreferenceScreen(
                key = "revanced_hide_keyword_content_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_hide_keyword_content_home"),
                    SwitchPreference("revanced_hide_keyword_content_subscriptions"),
                    SwitchPreference("revanced_hide_keyword_content_search"),
                    TextPreference("revanced_hide_keyword_content_phrases", inputType = InputType.TEXT_MULTI_LINE),
                    NonInteractivePreference("revanced_hide_keyword_content_about"),
                    NonInteractivePreference(key = "revanced_hide_keyword_content_about_whole_words",
                        tag = "app.revanced.integrations.youtube.settings.preference.HtmlPreference")
                )
            )
        )

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_hide_chips_shelf"),
            SwitchPreference("revanced_hide_expandable_chip"),
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

        LithoFilterPatch.addFilter(LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR)
        LithoFilterPatch.addFilter(DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME)
        LithoFilterPatch.addFilter(KEYWORD_FILTER_CLASS_NAME)
        LithoFilterPatch.addFilter(CUSTOM_FILTER_CLASS_NAME)

        // region Mix playlists

        ParseElementFromBufferFingerprint.resultOrThrow().let { result ->
            val startIndex = result.scanResult.patternScanResult!!.startIndex

            result.mutableMethod.apply {
                val freeRegister = "v0"
                val byteArrayParameter = "p3"
                val conversionContextRegister = getInstruction<TwoRegisterInstruction>(startIndex).registerA
                val returnEmptyComponentInstruction = getInstructions().last { it.opcode == Opcode.INVOKE_STATIC }

                addInstructionsWithLabels(
                    startIndex + 1,
                    """
                        invoke-static { v$conversionContextRegister, $byteArrayParameter }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->filterMixPlaylists(Ljava/lang/Object;[B)Z
                        move-result $freeRegister 
                        if-nez $freeRegister, :return_empty_component
                        const/4 $freeRegister, 0x0  # Restore register, required for 19.16
                    """,
                    ExternalLabel("return_empty_component", returnEmptyComponentInstruction),
                )
            }
        }

        // endregion

        // region Watermark (legacy code for old versions of YouTube)

        ShowWatermarkFingerprint.alsoResolve(
            context,
            PlayerOverlayFingerprint
        ).mutableMethod.apply {
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

        // region 'Yoodles'

        YoodlesImageViewFingerprint.resultOrThrow().mutableMethod.apply {
            findOpcodeIndicesReversed{
                opcode == Opcode.INVOKE_VIRTUAL
                        && getReference<MethodReference>()?.name == "setImageDrawable"
            }.forEach { insertIndex ->
                val register = getInstruction<FiveRegisterInstruction>(insertIndex).registerD

                addInstructionsWithLabels(
                    insertIndex,
                    """
                        invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideYoodles(Landroid/graphics/drawable/Drawable;)Landroid/graphics/drawable/Drawable;
                        move-result-object v$register
                        if-eqz v$register, :hide
                    """,
                    ExternalLabel("hide", getInstruction(insertIndex + 1)),
                )
            }
        }

        // endregion
    }
}
