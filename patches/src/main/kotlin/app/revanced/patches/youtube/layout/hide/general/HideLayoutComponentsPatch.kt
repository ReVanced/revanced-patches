package app.revanced.patches.youtube.layout.hide.general

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable
import app.revanced.patcher.CompositeMatch
import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.*
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.shared.layout.hide.general.hideLayoutComponentsPatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.youtube.misc.engagement.engagementPanelHookPatch
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.playservice.is_20_21_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.findFreeRegister
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

internal var albumCardId = -1L
    private set
internal var crowdfundingBoxId = -1L
    private set
internal var filterBarHeightId = -1L
    private set
internal var relatedChipCloudMarginId = -1L
    private set
internal var barContainerHeightId = -1L
    private set

private val hideLayoutComponentsResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    apply {
        albumCardId = ResourceType.LAYOUT["album_card"]

        crowdfundingBoxId = ResourceType.LAYOUT["donation_companion"]

        relatedChipCloudMarginId = ResourceType.LAYOUT["related_chip_cloud_reduced_margins"]

        filterBarHeightId = ResourceType.DIMEN["filter_bar_height"]

        barContainerHeightId = ResourceType.DIMEN["bar_container_height"]
    }
}

private const val LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/litho/LayoutComponentsFilter;"
private const val DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME =
    "Lapp/revanced/extension/youtube/patches/litho/DescriptionComponentsFilter;"
private const val COMMENTS_FILTER_CLASS_NAME =
    "Lapp/revanced/extension/youtube/patches/litho/CommentsFilter;"
private const val CUSTOM_FILTER_CLASS_NAME =
    "Lapp/revanced/extension/shared/patches/litho/CustomFilter;"
private const val KEYWORD_FILTER_CLASS_NAME =
    "Lapp/revanced/extension/youtube/patches/litho/KeywordContentFilter;"

val hideLayoutComponentsPatch = hideLayoutComponentsPatch(
    lithoFilterPatch = lithoFilterPatch,
    settingsPatch = settingsPatch,
    generalSettingsScreen = PreferenceScreen.GENERAL,
    additionalDependencies = setOf(
        hideLayoutComponentsResourcePatch,
        navigationBarHookPatch,
        versionCheckPatch,
        engagementPanelHookPatch,
        resourceMappingPatch,
    ),
    filterClasses = setOf(
        LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR,
        DESCRIPTION_COMPONENTS_FILTER_CLASS_NAME,
        COMMENTS_FILTER_CLASS_NAME,
        KEYWORD_FILTER_CLASS_NAME,
        CUSTOM_FILTER_CLASS_NAME,
    ),
    compatibleWithPackages = arrayOf(
        "com.google.android.youtube" to setOf(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    ),
) {
    addResources("youtube", "layout.hide.general.hideLayoutComponentsPatch")

    PreferenceScreen.ADS.addPreferences(
        // Uses horizontal shelf and a buffer, which requires managing in a single place in the code
        // to ensure the generic "hide horizontal shelves" doesn't hide when it should show.
        SwitchPreference("revanced_hide_creator_store_shelf")
    )

    PreferenceScreen.PLAYER.addPreferences(
        PreferenceScreenPreference(
            key = "revanced_hide_description_components_screen",
            preferences = setOf(
                SwitchPreference("revanced_hide_ai_generated_video_summary_section"),
                SwitchPreference("revanced_hide_ask_section"),
                SwitchPreference("revanced_hide_attributes_section"),
                SwitchPreference("revanced_hide_chapters_section"),
                SwitchPreference("revanced_hide_course_progress_section"),
                SwitchPreference("revanced_hide_explore_section"),
                SwitchPreference("revanced_hide_explore_course_section"),
                SwitchPreference("revanced_hide_explore_podcast_section"),
                SwitchPreference("revanced_hide_featured_links_section"),
                SwitchPreference("revanced_hide_featured_places_section"),
                SwitchPreference("revanced_hide_featured_videos_section"),
                SwitchPreference("revanced_hide_gaming_section"),
                SwitchPreference("revanced_hide_how_this_was_made_section"),
                SwitchPreference("revanced_hide_hype_points"),
                SwitchPreference("revanced_hide_info_cards_section"),
                SwitchPreference("revanced_hide_key_concepts_section"),
                SwitchPreference("revanced_hide_music_section"),
                SwitchPreference("revanced_hide_subscribe_button"),
                SwitchPreference("revanced_hide_transcript_section"),
                SwitchPreference("revanced_hide_quizzes_section")
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
                SwitchPreference("revanced_hide_comments_section_in_home_feed"),
                SwitchPreference("revanced_hide_comments_community_guidelines"),
                SwitchPreference("revanced_hide_comments_create_a_short_button"),
                SwitchPreference("revanced_hide_comments_emoji_and_timestamp_buttons"),
                SwitchPreference("revanced_hide_comments_preview_comment"),
                SwitchPreference("revanced_hide_comments_thanks_button"),
            ),
            sorting = Sorting.UNSORTED,
        ),
        SwitchPreference("revanced_hide_channel_bar"),
        SwitchPreference("revanced_hide_channel_watermark"),
        SwitchPreference("revanced_hide_crowdfunding_box"),
        SwitchPreference("revanced_hide_emergency_box"),
        SwitchPreference("revanced_hide_info_panels"),
        SwitchPreference("revanced_hide_join_membership_button"),
        SwitchPreference("revanced_hide_live_chat_replay_button"),
        SwitchPreference("revanced_hide_medical_panels"),
        SwitchPreference("revanced_hide_quick_actions"),
        SwitchPreference("revanced_hide_related_videos"),
        SwitchPreference("revanced_hide_subscribers_community_guidelines"),
        SwitchPreference("revanced_hide_timed_reactions"),
        SwitchPreference("revanced_hide_video_title")
    )

    PreferenceScreen.FEED.addPreferences(
        PreferenceScreenPreference(
            key = "revanced_hide_keyword_content_screen",
            sorting = Sorting.UNSORTED,
            preferences = setOf(
                SwitchPreference("revanced_hide_keyword_content_home"),
                SwitchPreference("revanced_hide_keyword_content_subscriptions"),
                SwitchPreference("revanced_hide_keyword_content_search"),
                TextPreference(
                    "revanced_hide_keyword_content_phrases",
                    inputType = InputType.TEXT_MULTI_LINE
                ),
                NonInteractivePreference(
                    key = "revanced_hide_keyword_content_about",
                    tag = "app.revanced.extension.shared.settings.preference.BulletPointPreference",
                ),
                NonInteractivePreference(
                    key = "revanced_hide_keyword_content_about_whole_words",
                    tag = "app.revanced.extension.youtube.settings.preference.HTMLPreference",
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
                PreferenceCategory(
                    titleKey = null,
                    sorting = Sorting.UNSORTED,
                    tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                    preferences = setOf(
                        SwitchPreference("revanced_hide_channel_tab"),
                        TextPreference(
                            "revanced_hide_channel_tab_filter_strings",
                            inputType = InputType.TEXT_MULTI_LINE
                        ),
                    )
                ),
                SwitchPreference("revanced_hide_community_button"),
                SwitchPreference("revanced_hide_for_you_shelf"),
                SwitchPreference("revanced_hide_join_button"),
                SwitchPreference("revanced_hide_links_preview"),
                SwitchPreference("revanced_hide_members_shelf"),
                SwitchPreference("revanced_hide_store_button"),
                SwitchPreference("revanced_hide_subscribe_button_in_channel_page"),
            ),
        ),
        SwitchPreference("revanced_hide_album_cards"),
        SwitchPreference("revanced_hide_artist_cards"),
        SwitchPreference("revanced_hide_chips_shelf"),
        SwitchPreference("revanced_hide_community_posts"),
        SwitchPreference("revanced_hide_compact_banner"),
        SwitchPreference("revanced_hide_expandable_card"),
        PreferenceCategory(
            titleKey = null,
            sorting = Sorting.UNSORTED,
            tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
            preferences = setOf(
                SwitchPreference("revanced_hide_feed_flyout_menu"),
                TextPreference(
                    "revanced_hide_feed_flyout_menu_filter_strings",
                    inputType = InputType.TEXT_MULTI_LINE
                ),
            )
        ),
        SwitchPreference("revanced_hide_floating_microphone_button"),
        SwitchPreference(
            key = "revanced_hide_horizontal_shelves",
            tag = "app.revanced.extension.shared.settings.preference.BulletPointSwitchPreference",
        ),
        SwitchPreference("revanced_hide_image_shelf"),
        SwitchPreference("revanced_hide_latest_posts"),
        SwitchPreference("revanced_hide_latest_videos_button"),
        SwitchPreference("revanced_hide_mix_playlists"),
        SwitchPreference("revanced_hide_movies_section"),
        SwitchPreference("revanced_hide_notify_me_button"),
        SwitchPreference("revanced_hide_playables"),
        SwitchPreference("revanced_hide_show_more_button"),
        SwitchPreference("revanced_hide_surveys"),
        SwitchPreference("revanced_hide_ticket_shelf"),
        SwitchPreference("revanced_hide_upload_time"),
        SwitchPreference("revanced_hide_video_recommendation_labels"),
        SwitchPreference("revanced_hide_view_count"),
        SwitchPreference("revanced_hide_visual_spacer"),
        SwitchPreference("revanced_hide_doodles"),
    )

    if (is_20_21_or_greater) {
        PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_hide_you_may_like_section")
        )
    }

    // region Hide mix playlists

    parseElementFromBufferMethodMatch.let {
        it.method.apply {
            val startIndex = it[0]
            val insertIndex = startIndex + 1

            val byteArrayParameter = "p3"
            val conversionContextRegister =
                getInstruction<TwoRegisterInstruction>(startIndex).registerA
            val returnEmptyComponentInstruction =
                instructions.last { it.opcode == Opcode.INVOKE_STATIC }
            val returnEmptyComponentRegister =
                (returnEmptyComponentInstruction as FiveRegisterInstruction).registerC
            val freeRegister =
                findFreeRegister(
                    insertIndex,
                    conversionContextRegister,
                    returnEmptyComponentRegister
                )

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

    // region Hide watermark (legacy code for old versions of YouTube)

    playerOverlayMethod.immutableClassDef.getShowWatermarkMethod().apply {
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

    // region Hide Show more button

    val (textViewField, buttonContainerField) = hideShowMoreButtonSetViewMethodMatch.let {
        val textViewIndex = it[1]
        val buttonContainerIndex = it[3]

        Pair(
            it.method.getInstruction<ReferenceInstruction>(textViewIndex).reference,
            it.method.getInstruction<ReferenceInstruction>(buttonContainerIndex).reference
        )
    }

    val parentViewMethod = hideShowMoreButtonSetViewMethodMatch.immutableClassDef
        .getHideShowMoreButtonGetParentViewMethod()

    hideShowMoreButtonSetViewMethodMatch.immutableClassDef.getHideShowMoreButtonMethod().apply {
        val helperMethod = ImmutableMethod(
            definingClass,
            "patch_hideShowMoreButton",
            listOf(),
            "V",
            AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
            null,
            null,
            MutableMethodImplementation(7),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    move-object/from16 v0, p0
                    invoke-virtual { v0 }, $parentViewMethod
                    move-result-object v1
                    iget-object v2, v0, $buttonContainerField
                    iget-object v3, v0, $textViewField
                    invoke-static { v1, v2, v3 }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideShowMoreButton(Landroid/view/View;Landroid/view/View;Landroid/widget/TextView;)V
                    return-void
                """
            )
        }.also(classDef.methods::add)

        findInstructionIndicesReversedOrThrow(Opcode.RETURN_VOID).forEach { index ->
            addInstruction(index, "invoke-direct/range { p0 .. p0 }, $helperMethod")
        }
    }

    // endregion


    // region Hide Subscribed channels bar

    // Tablet
    val methodMatch = if (is_20_21_or_greater)
        hideSubscribedChannelsBarConstructorMethodMatch
    else hideSubscribedChannelsBarConstructorLegacyMethodMatch

    methodMatch.let {
        it.method.apply {
            val index = it[1]
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstruction(
                index + 1,
                "invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR" +
                        "->hideSubscribedChannelsBar(Landroid/view/View;)V",
            )
        }
    }

    // Phone (landscape mode)
    methodMatch.immutableClassDef.hideSubscribedChannelsBarLandscapeMethodMatch.let {
        it.method.apply {
            val index = it[-1]
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstructions(
                index + 1,
                """
                    invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideSubscribedChannelsBar(I)I
                    move-result v$register
                """
            )
        }
    }

    // endregion

    // region Hide Crowdfunding box

    crowdfundingBoxMethodMatch.let {
        it.method.apply {
            val insertIndex = it[-1]
            val objectRegister = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

            addInstruction(
                insertIndex,
                "invoke-static {v$objectRegister}, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR" +
                        "->hideCrowdfundingBox(Landroid/view/View;)V",
            )
        }
    }

    // endregion

    // region Hide Album cards

    albumCardsMethodMatch.let {
        it.method.apply {
            val checkCastAnchorIndex = it[-1]
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

    // region Hide Floating microphone

    showFloatingMicrophoneButtonMethodMatch.let {
        it.method.apply {
            val index = it[-1]
            val register = getInstruction<TwoRegisterInstruction>(index).registerA

            addInstructions(
                index + 1,
                """
                        invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideFloatingMicrophoneButton(Z)Z
                        move-result v$register
                    """,
            )
        }
    }

    // endregion

    // region Hide latest videos button

    listOf(
        latestVideosContentPillMethodMatch,
        latestVideosBarMethodMatch,
    ).forEach { match ->
        match.method.apply {
            val moveIndex = match[-1]
            val viewRegister = getInstruction<OneRegisterInstruction>(moveIndex).registerA

            addInstruction(
                moveIndex + 1,
                "invoke-static { v$viewRegister }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR" +
                        "->hideLatestVideosButton(Landroid/view/View;)V"
            )
        }
    }

    // endregion

    // region Hide 'Yoodles'

    yoodlesImageViewMethod.apply {
        findInstructionIndicesReversedOrThrow {
            getReference<MethodReference>()?.name == "setImageDrawable"
        }.forEach { insertIndex ->
            val drawableRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerD
            val imageViewRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

            replaceInstruction(
                insertIndex,
                "invoke-static { v$imageViewRegister, v$drawableRegister }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->" +
                        "setDoodleDrawable(Landroid/widget/ImageView;Landroid/graphics/drawable/Drawable;)V",
            )
        }
    }

    // endregion

    // region Hide view count

    hideViewCountMethodMatch.method.apply {
        val startIndex = hideViewCountMethodMatch[0]
        var returnStringRegister = getInstruction<OneRegisterInstruction>(startIndex).registerA

        // Find the instruction where the text dimension is retrieved.
        val applyDimensionIndex = indexOfFirstInstructionReversedOrThrow {
            val reference = getReference<MethodReference>()
            opcode == Opcode.INVOKE_STATIC &&
                    reference?.definingClass == "Landroid/util/TypedValue;" &&
                    reference.returnType == "F" &&
                    reference.name == "applyDimension" &&
                    reference.parameterTypes == listOf("I", "F", "Landroid/util/DisplayMetrics;")
        }

        // A float value is passed which is used to determine subtitle text size.
        val floatDimensionRegister = getInstruction<OneRegisterInstruction>(
            applyDimensionIndex + 1,
        ).registerA

        addInstructions(
            applyDimensionIndex - 1,
            """
                invoke-static { v$returnStringRegister, v$floatDimensionRegister }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->modifyFeedSubtitleSpan(Landroid/text/SpannableString;F)Landroid/text/SpannableString;
                move-result-object v$returnStringRegister
            """,
        )
    }

    // endregion

    // region Hide filter bar

    /**
     * Patch a [Method] with a given [instructions].
     *
     * @param RegisterInstruction The type of instruction to get the register from.
     * @param insertIndexOffset The offset to add to the end index of the [CompositeMatch.indices].
     * @param hookRegisterOffset The offset to add to the register of the hook.
     * @param instructions The instructions to add with the register as a parameter.
     */
    fun <RegisterInstruction : OneRegisterInstruction> CompositeMatch.patch(
        insertIndexOffset: Int = 0,
        hookRegisterOffset: Int = 0,
        instructions: (Int) -> String,
    ) = method.apply {
        val endIndex = get(-1)
        val insertIndex = endIndex + insertIndexOffset
        val register = getInstruction<RegisterInstruction>(endIndex + hookRegisterOffset).registerA

        addInstructions(insertIndex, instructions(register))
    }

    filterBarHeightMethodMatch.patch<TwoRegisterInstruction> { register ->
        """
            invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideInFeed(I)I
            move-result v$register
        """
    }

    searchResultsChipBarMethodMatch.patch<OneRegisterInstruction>(-1, -2) { register ->
        """
            invoke-static { v$register }, $LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideInSearch(I)I
            move-result v$register
        """
    }

    relatedChipCloudMethodMatch.patch<OneRegisterInstruction>(1) { register ->
        "invoke-static { v$register }, " +
                "$LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR->hideInRelatedVideos(Landroid/view/View;)V"
    }

    // endregion

    // region Hide You may like section

    if (is_20_21_or_greater) {
        val searchSuggestionEndpointField =
            searchSuggestionEndpointConstructorMethod.immutableClassDef
                .searchSuggestionEndpointMethodMatch.let {
                    it.method.getInstruction(it[0]).fieldReference!!
                }
        val searchSuggestionEndpointClass = searchSuggestionEndpointField.definingClass

        searchBoxTypingStringMethodMatch.let {
            it.method.apply {
                // A collection of search suggestions.
                // This includes trending search (also known as 'You may like' section)
                // and your search history.

                val searchSuggestionCollectionField = getInstruction(it[0]).fieldReference!!
                val typedStringField = getInstruction(it[2]).fieldReference!!

                val helperMethod = ImmutableMethod(
                    definingClass,
                    "patch_setSearchSuggestions",
                    listOf(
                        ImmutableMethodParameter(
                            parameterTypes.first().toString(),
                            null,
                            null
                        )
                    ),
                    "V",
                    AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
                    annotations,
                    null,
                    MutableMethodImplementation(7),
                ).toMutable().apply {
                    addInstructionsWithLabels(
                        0,
                        """
                                move-object/from16 v0, p1
                                iget-object v1, v0, $typedStringField
                                
                                # Check if the setting is enabled and if the typed string is empty.
                                invoke-static { v1 }, ${LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR}->hideYouMayLikeSection(Ljava/lang/String;)Z
                                move-result v1
                                
                                # If the setting is disabled or the typed string is not empty, do nothing.
                                if-eqz v1, :ignore

                                ## Get a collection of search suggestions.
                                iget-object v1, v0, $searchSuggestionCollectionField
                                
                                # Iterate through the collection and check if the search suggestion is the search history.
                                invoke-interface { v1 }, Ljava/util/Collection;->iterator()Ljava/util/Iterator;
                                move-result-object v2
                                
                                :loop
                                invoke-interface { v2 }, Ljava/util/Iterator;->hasNext()Z
                                move-result v3
                                if-eqz v3, :exit
                                invoke-interface { v2 }, Ljava/util/Iterator;->next()Ljava/lang/Object;
                                move-result-object v3
                                instance-of v4, v3, $searchSuggestionEndpointClass
                                if-eqz v4, :loop
                                check-cast v3, $searchSuggestionEndpointClass

                                # Each search suggestion has a command endpoint.
                                # If the search suggestion is the search history, the command includes the keyword '/delete'.
                                iget-object v4, v3, $searchSuggestionEndpointField
                                invoke-static { v3, v4 }, ${LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR}->isSearchHistory(Ljava/lang/Object;Ljava/lang/String;)Z
                                move-result v3
                                
                                # If this search suggestion is the search history, do nothing.
                                if-nez v3, :loop
                                
                                # If this search suggestion is not the search history, remove it from the search suggestions collection.
                                invoke-interface { v2 }, Ljava/util/Iterator;->remove()V
                                goto :loop

                                # Save the updated collection to a field.
                                :exit
                                iput-object v1, v0, $searchSuggestionCollectionField

                                :ignore
                                return-void
                            """
                    )
                }.also(it.classDef.methods::add)

                addInstruction(
                    0,
                    "invoke-direct/range { p0 .. p1 }, $helperMethod"
                )
            }
        }
    }

    // endregion

    // region Hide flyout menu items

    bottomSheetMenuItemBuilderMethodMatch.let {
        it.method.apply {
            val index = it[1]
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstructions(
                index + 1,
                """
                        invoke-static { v$register }, ${LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR}->hideFlyoutMenu(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                        move-result-object v$register      
                    """
            )
        }
    }

    contextualMenuItemBuilderMethodMatch.let {
        it.method.apply {
            val index = it[1]
            val targetInstruction = getInstruction<FiveRegisterInstruction>(index)

            addInstruction(
                index + 1,
                "invoke-static { v${targetInstruction.registerC}, v${targetInstruction.registerD} }, " +
                        "${LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR}->hideFlyoutMenu(Landroid/widget/TextView;Ljava/lang/CharSequence;)V"
            )
        }
    }

    // endregion

    // region Hide channel tab

    channelTabRendererMethod.apply {
        val iteratorIndex = indexOfFirstInstructionReversedOrThrow {
            methodReference?.name == "hasNext"
        }

        val iteratorRegister = getInstruction<FiveRegisterInstruction>(iteratorIndex).registerC
        val targetIndex = indexOfFirstInstructionReversedOrThrow {
            val reference = methodReference

            opcode == Opcode.INVOKE_INTERFACE &&
                    reference?.returnType == channelTabBuilderMethod.returnType &&
                    reference.parameterTypes == channelTabBuilderMethod.parameterTypes
        }

        val objectIndex = indexOfFirstInstructionReversedOrThrow(
            targetIndex,
            Opcode.IGET_OBJECT
        )
        val objectInstruction = getInstruction<TwoRegisterInstruction>(objectIndex)
        val objectReference = getInstruction<ReferenceInstruction>(objectIndex).reference

        addInstructionsWithLabels(
            objectIndex + 1,
            """
                invoke-static { v${objectInstruction.registerA} }, ${LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR}->hideChannelTab(Ljava/lang/String;)Z
                move-result v${objectInstruction.registerA}
                if-eqz v${objectInstruction.registerA}, :ignore
                invoke-interface { v$iteratorRegister }, Ljava/util/Iterator;->remove()V
                goto :next_iterator
                :ignore
                iget-object v${objectInstruction.registerA}, v${objectInstruction.registerB}, $objectReference
                """,
            ExternalLabel("next_iterator", getInstruction(iteratorIndex))
        )
    }

    // endregion
}

