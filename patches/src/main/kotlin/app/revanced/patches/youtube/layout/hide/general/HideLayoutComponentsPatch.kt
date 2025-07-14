package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.playservice.is_20_07_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_09_or_greater
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.findFreeRegister
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

var expandButtonDownId = -1L
    private set
var albumCardId = -1L
    private set
var crowdfundingBoxId = -1L
    private set
var youTubeLogo = -1L
    private set
var filterBarHeightId = -1L
    private set
var relatedChipCloudMarginId = -1L
    private set
var barContainerHeightId = -1L
    private set
var fabButtonId = -1L
    private set

private val hideLayoutComponentsResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        expandButtonDownId = resourceMappings[
            "layout",
            "expand_button_down",
        ]

        albumCardId = resourceMappings[
            "layout",
            "album_card",
        ]

        crowdfundingBoxId = resourceMappings[
            "layout",
            "donation_companion",
        ]

        youTubeLogo = resourceMappings[
            "id",
            "youtube_logo",
        ]

        relatedChipCloudMarginId = resourceMappings[
            "layout",
            "related_chip_cloud_reduced_margins",
        ]

        filterBarHeightId = resourceMappings[
            "dimen",
            "filter_bar_height",
        ]

        barContainerHeightId = resourceMappings[
            "dimen",
            "bar_container_height",
        ]

        fabButtonId = resourceMappings[
            "id",
            "fab",
        ]
    }
}

private const val LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/components/LayoutComponentsFilter;"
private const val DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME =
    "Lapp/revanced/extension/youtube/patches/components/DescriptionComponentsFilter;"
private const val COMMENTS_FILTER_CLASS_NAME =
    "Lapp/revanced/extension/youtube/patches/components/CommentsFilter;"
private const val CUSTOM_FILTER_CLASS_NAME =
    "Lapp/revanced/extension/youtube/patches/components/CustomFilter;"
private const val KEYWORD_FILTER_CLASS_NAME =
    "Lapp/revanced/extension/youtube/patches/components/KeywordContentFilter;"

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
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
            "20.13.41",
        )
    )

    execute {
        addResources("youtube", "layout.hide.general.hideLayoutComponentsPatch")

        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_hide_description_components_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_ai_generated_video_summary_section"),
                    SwitchPreference("revanced_hide_ask_section"),
                    SwitchPreference("revanced_hide_attributes_section"),
                    SwitchPreference("revanced_hide_chapters_section"),
                    SwitchPreference("revanced_hide_info_cards_section"),
                    SwitchPreference("revanced_hide_how_this_was_made_section"),
                    SwitchPreference("revanced_hide_key_concepts_section"),
                    SwitchPreference("revanced_hide_podcast_section"),
                    SwitchPreference("revanced_hide_transcript_section"),
                ),
            ),
            PreferenceScreenPreference(
                "revanced_comments_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_comments_ai_chat_summary"),
                    SwitchPreference("revanced_hide_comments_ai_summary"),
                    SwitchPreference("revanced_hide_comments_channel_guidelines"),
                    SwitchPreference("revanced_hide_comments_by_members_header"),
                    SwitchPreference("revanced_hide_comments_section"),
                    SwitchPreference("revanced_hide_comments_community_guidelines"),
                    SwitchPreference("revanced_hide_comments_create_a_short_button"),
                    SwitchPreference("revanced_hide_comments_preview_comment"),
                    SwitchPreference("revanced_hide_comments_thanks_button"),
                    SwitchPreference("revanced_hide_comments_timestamp_button"),
                ),
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            ),
            SwitchPreference("revanced_hide_channel_bar"),
            SwitchPreference("revanced_hide_channel_watermark"),
            SwitchPreference("revanced_hide_emergency_box"),
            SwitchPreference("revanced_hide_info_panels"),
            SwitchPreference("revanced_hide_join_membership_button"),
            SwitchPreference("revanced_hide_medical_panels"),
            SwitchPreference("revanced_hide_quick_actions"),
            SwitchPreference("revanced_hide_related_videos"),
            SwitchPreference("revanced_hide_subscribers_community_guidelines"),
            SwitchPreference("revanced_hide_timed_reactions"),
        )

        PreferenceScreen.FEED.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_hide_keyword_content_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_hide_keyword_content_home"),
                    SwitchPreference("revanced_hide_keyword_content_subscriptions"),
                    SwitchPreference("revanced_hide_keyword_content_search"),
                    TextPreference("revanced_hide_keyword_content_phrases", inputType = InputType.TEXT_MULTI_LINE),
                    NonInteractivePreference("revanced_hide_keyword_content_about"),
                    NonInteractivePreference(
                        key = "revanced_hide_keyword_content_about_whole_words",
                        tag = "app.revanced.extension.youtube.settings.preference.HtmlPreference",
                    ),
                ),
            ),
            PreferenceScreenPreference(
                key = "revanced_hide_filter_bar_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_filter_bar_feed_in_feed"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_related_videos"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_search"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_history"),
                ),
            ),
            PreferenceScreenPreference(
                key = "revanced_channel_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_for_you_shelf"),
                    SwitchPreference("revanced_hide_links_preview"),
                    SwitchPreference("revanced_hide_members_shelf"),
                    SwitchPreference("revanced_hide_visit_community_button"),
                    SwitchPreference("revanced_hide_visit_store_button"),
                ),
            ),
            SwitchPreference("revanced_hide_album_cards"),
            SwitchPreference("revanced_hide_artist_cards"),
            SwitchPreference("revanced_hide_community_posts"),
            SwitchPreference("revanced_hide_compact_banner"),
            SwitchPreference("revanced_hide_crowdfunding_box"),
            SwitchPreference("revanced_hide_chips_shelf"),
            SwitchPreference("revanced_hide_expandable_card"),
            SwitchPreference("revanced_hide_floating_microphone_button"),
            SwitchPreference("revanced_hide_horizontal_shelves"),
            SwitchPreference("revanced_hide_image_shelf"),
            SwitchPreference("revanced_hide_latest_posts"),
            SwitchPreference("revanced_hide_mix_playlists"),
            SwitchPreference("revanced_hide_movies_section"),
            SwitchPreference("revanced_hide_notify_me_button"),
            SwitchPreference("revanced_hide_playables"),
            SwitchPreference("revanced_hide_show_more_button"),
            SwitchPreference("revanced_hide_surveys"),
            SwitchPreference("revanced_hide_ticket_shelf"),
            SwitchPreference("revanced_hide_video_recommendation_labels"),
            SwitchPreference("revanced_hide_doodles"),
        )

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_custom_filter_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_custom_filter"),
                    TextPreference("revanced_custom_filter_strings", inputType = InputType.TEXT_MULTI_LINE),
                ),
            ),
        )

        addLithoFilter(LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR)
        addLithoFilter(DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME)
        addLithoFilter(COMMENTS_FILTER_CLASS_NAME)
        addLithoFilter(KEYWORD_FILTER_CLASS_NAME)
        addLithoFilter(CUSTOM_FILTER_CLASS_NAME)

        // region Mix playlists

        (if (is_20_09_or_greater) parseElementFromBufferFingerprint
        else if (is_20_07_or_greater) parseElementFromBufferLegacy2007Fingerprint
        else parseElementFromBufferLegacy1901Fingerprint).let {
            it.method.apply {
                val byteArrayParameter = "p3"
                val startIndex = it.patternMatch!!.startIndex
                val conversionContextRegister = getInstruction<TwoRegisterInstruction>(startIndex).registerA
                val returnEmptyComponentInstruction = instructions.last { it.opcode == Opcode.INVOKE_STATIC }
                val returnEmptyComponentRegister = (returnEmptyComponentInstruction as FiveRegisterInstruction).registerC
                val insertIndex = startIndex + 1
                val freeRegister = findFreeRegister(insertIndex, conversionContextRegister, returnEmptyComponentRegister)

                addInstructionsWithLabels(
                    insertIndex,
                    """
                        invoke-static { v$conversionContextRegister, $byteArrayParameter }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->filterMixPlaylists(Ljava/lang/Object;[B)Z
                        move-result v$freeRegister 
                        if-eqz v$freeRegister, :show
                        move-object v$returnEmptyComponentRegister, p1   # Required for 19.47
                        goto :return_empty_component
                        :show
                        nop
                    """,
                    ExternalLabel("return_empty_component", returnEmptyComponentInstruction),
                )
            }
        }

        // endregion

        // region Watermark (legacy code for old versions of YouTube)

        showWatermarkFingerprint.match(
            playerOverlayFingerprint.originalClassDef,
        ).method.apply {
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

        hideShowMoreButtonFingerprint.method.apply {
            val moveRegisterIndex = hideShowMoreButtonFingerprint.patternMatch!!.endIndex
            val viewRegister = getInstruction<OneRegisterInstruction>(moveRegisterIndex).registerA

            val insertIndex = moveRegisterIndex + 1
            addInstruction(
                insertIndex,
                "invoke-static { v$viewRegister }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR" +
                    "->hideShowMoreButton(Landroid/view/View;)V",
            )
        }

        // endregion

        // region crowdfunding box
        crowdfundingBoxFingerprint.let {
            it.method.apply {
                val insertIndex = it.patternMatch!!.endIndex
                val objectRegister = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static {v$objectRegister}, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR" +
                        "->hideCrowdfundingBox(Landroid/view/View;)V",
                )
            }
        }

        // endregion

        // region hide album cards

        albumCardsFingerprint.let {
            it.method.apply {
                val checkCastAnchorIndex = it.patternMatch!!.endIndex
                val insertIndex = checkCastAnchorIndex + 1
                val register = getInstruction<OneRegisterInstruction>(checkCastAnchorIndex).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR" +
                        "->hideAlbumCard(Landroid/view/View;)V",
                )
            }
        }

        // endregion

        // region hide floating microphone

        showFloatingMicrophoneButtonFingerprint.method.apply {
            val literalIndex = indexOfFirstLiteralInstructionOrThrow(fabButtonId)
            val booleanIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.IGET_BOOLEAN)
            val register = getInstruction<TwoRegisterInstruction>(booleanIndex).registerA

            addInstructions(
                booleanIndex + 1,
                """
                    invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideFloatingMicrophoneButton(Z)Z
                    move-result v$register
                """
            )
        }

        // endregion

        // region 'Yoodles'

        yoodlesImageViewFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow {
                getReference<MethodReference>()?.name == "setImageDrawable"
            }.forEach { insertIndex ->
                val drawableRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerD
                val imageViewRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

                replaceInstruction(
                    insertIndex,
                    "invoke-static { v$imageViewRegister, v$drawableRegister }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->" +
                            "setDoodleDrawable(Landroid/widget/ImageView;Landroid/graphics/drawable/Drawable;)V"
                )
            }
        }

        // endregion

        // region hide filter bar

        /**
         * Patch a [Method] with a given [instructions].
         *
         * @param RegisterInstruction The type of instruction to get the register from.
         * @param insertIndexOffset The offset to add to the end index of the [Match.patternMatch].
         * @param hookRegisterOffset The offset to add to the register of the hook.
         * @param instructions The instructions to add with the register as a parameter.
         */
        fun <RegisterInstruction : OneRegisterInstruction> Fingerprint.patch(
            insertIndexOffset: Int = 0,
            hookRegisterOffset: Int = 0,
            instructions: (Int) -> String,
        ) = method.apply {
            val endIndex = patternMatch!!.endIndex

            val insertIndex = endIndex + insertIndexOffset
            val register =
                getInstruction<RegisterInstruction>(endIndex + hookRegisterOffset).registerA

            addInstructions(insertIndex, instructions(register))
        }

        filterBarHeightFingerprint.patch<TwoRegisterInstruction> { register ->
            """
                invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideInFeed(I)I
                move-result v$register
            """
        }

        searchResultsChipBarFingerprint.patch<OneRegisterInstruction>(-1, -2) { register ->
            """
                invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideInSearch(I)I
                move-result v$register
            """
        }

        relatedChipCloudFingerprint.patch<OneRegisterInstruction>(1) { register ->
            "invoke-static { v$register }, " +
                "$LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideInRelatedVideos(Landroid/view/View;)V"
        }
    }
}
