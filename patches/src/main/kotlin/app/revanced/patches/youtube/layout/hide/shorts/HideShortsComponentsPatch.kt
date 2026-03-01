package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.extensions.wideLiteral
import app.revanced.patcher.firstMethod
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.shared.conversionContextToStringMethod
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.engagement.engagementPanelHookPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.playservice.*
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal val hideShortsAppShortcutOption = booleanOption(
    name = "Hide Shorts app shortcut",
    default = false,
    description = "Permanently hides the shortcut to open Shorts when long pressing the app icon in your launcher.",
)

internal val hideShortsWidgetOption = booleanOption(
    name = "Hide Shorts widget",
    default = false,
    description = "Permanently hides the launcher widget Shorts button.",
)

private val hideShortsComponentsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
        versionCheckPatch,
    )

    apply {
        val hideShortsAppShortcut by hideShortsAppShortcutOption
        val hideShortsWidget by hideShortsWidgetOption

        addResources("youtube", "layout.hide.shorts.hideShortsComponentsResourcePatch")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_hide_shorts_channel"),
            SwitchPreference("revanced_hide_shorts_home"),
            SwitchPreference("revanced_hide_shorts_search"),
            SwitchPreference("revanced_hide_shorts_subscriptions"),
            SwitchPreference("revanced_hide_shorts_video_description"),
            SwitchPreference("revanced_hide_shorts_history"),

            PreferenceScreenPreference(
                key = "revanced_shorts_player_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    // Shorts player components.
                    // Ideally each group should be ordered similar to how they appear in the UI

                    // Vertical row of buttons on right side of the screen.
                    // Like fountain may no longer be used by YT anymore.
                    //SwitchPreference("revanced_hide_shorts_like_fountain"),
                    SwitchPreference("revanced_hide_shorts_like_button"),
                    SwitchPreference("revanced_hide_shorts_dislike_button"),
                    SwitchPreference("revanced_hide_shorts_comments_button"),
                    SwitchPreference("revanced_hide_shorts_share_button"),
                    SwitchPreference("revanced_hide_shorts_remix_button"),
                    SwitchPreference("revanced_hide_shorts_sound_button"),

                    // Upper and middle area of the player.
                    SwitchPreference("revanced_hide_shorts_join_button"),
                    SwitchPreference("revanced_hide_shorts_subscribe_button"),
                    SwitchPreference("revanced_hide_shorts_paused_overlay_buttons"),

                    // Suggested actions.
                    SwitchPreference("revanced_hide_shorts_preview_comment"),
                    SwitchPreference("revanced_hide_shorts_save_sound_button"),
                    SwitchPreference("revanced_hide_shorts_use_sound_button"),
                    SwitchPreference("revanced_hide_shorts_use_template_button"),
                    SwitchPreference("revanced_hide_shorts_upcoming_button"),
                    SwitchPreference("revanced_hide_shorts_effect_button"),
                    SwitchPreference("revanced_hide_shorts_green_screen_button"),
                    SwitchPreference("revanced_hide_shorts_hashtag_button"),
                    SwitchPreference("revanced_hide_shorts_live_preview"),
                    SwitchPreference("revanced_hide_shorts_new_posts_button"),
                    SwitchPreference("revanced_hide_shorts_shop_button"),
                    SwitchPreference("revanced_hide_shorts_tagged_products"),
                    SwitchPreference("revanced_hide_shorts_search_suggestions"),
                    SwitchPreference("revanced_hide_shorts_super_thanks_button"),
                    SwitchPreference("revanced_hide_shorts_stickers"),

                    // Bottom of the screen.
                    SwitchPreference("revanced_hide_shorts_ai_button"),
                    SwitchPreference("revanced_hide_shorts_auto_dubbed_label"),
                    SwitchPreference("revanced_hide_shorts_location_label"),
                    SwitchPreference("revanced_hide_shorts_channel_bar"),
                    SwitchPreference("revanced_hide_shorts_info_panel"),
                    SwitchPreference("revanced_hide_shorts_full_video_link_label"),
                    SwitchPreference("revanced_hide_shorts_video_title"),
                    SwitchPreference("revanced_hide_shorts_sound_metadata_label"),
                    SwitchPreference("revanced_hide_shorts_navigation_bar"),
                ),
            )
        )

        // Verify the file has the expected node, even if the patch option is off.
        document("res/xml/main_shortcuts.xml").use { document ->
            val shortsItem = document.childNodes.findElementByAttributeValueOrThrow(
                "android:shortcutId",
                "shorts-shortcut",
            )

            if (hideShortsAppShortcut == true) {
                shortsItem.removeFromParent()
            }
        }

        document("res/layout/appwidget_two_rows.xml").use { document ->
            val shortsItem = document.childNodes.findElementByAttributeValueOrThrow(
                "android:id",
                "@id/button_shorts_container",
            )

            if (hideShortsWidget == true) {
                shortsItem.removeFromParent()
            }
        }
    }
}

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/litho/ShortsFilter;"

@Suppress("unused")
val hideShortsComponentsPatch = bytecodePatch(
    name = "Hide Shorts components",
    description = "Adds options to hide components related to Shorts. " +
            "Patching version 20.21.37 or lower can hide more Shorts player button types."
) {
    dependsOn(
        sharedExtensionPatch,
        lithoFilterPatch,
        hideShortsComponentsResourcePatch,
        resourceMappingPatch,
        engagementPanelHookPatch,
        navigationBarHookPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    hideShortsAppShortcutOption()
    hideShortsWidgetOption()

    apply {
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // region Hide sound button.

        val id = ResourceType.DIMEN["reel_player_right_pivot_v2_size"]

        if (!is_21_05_or_greater) {
            forEachInstructionAsSequence({ _, method, instruction, index ->
                if (instruction.wideLiteral != id) return@forEachInstructionAsSequence null

                val targetIndex = method.indexOfFirstInstructionOrThrow(index) {
                    methodReference?.name == "getDimensionPixelSize"
                } + 1

                val sizeRegister = method.getInstruction<OneRegisterInstruction>(targetIndex).registerA

                return@forEachInstructionAsSequence targetIndex to sizeRegister
            }) { method, (targetIndex, sizeRegister) ->
                firstMethod(method).addInstructions(
                    targetIndex + 1,
                    """
                    invoke-static { v$sizeRegister }, $FILTER_CLASS_DESCRIPTOR->getSoundButtonSize(I)I
                    move-result v$sizeRegister
                """,
                )
            }
        }

        // endregion

        // region Hide action buttons.

        if (is_20_22_or_greater) {
            componentContextParserMethod.immutableClassDef.getTreeNodeResultListMethod().apply {
                val conversionContextPathBuilderField =
                    conversionContextToStringMethod.immutableClassDef
                        .fields.single { field -> field.type == "Ljava/lang/StringBuilder;" }

                val insertIndex = implementation!!.instructions.lastIndex
                val listRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                val registerProvider = getFreeRegisterProvider(insertIndex, 2)
                val freeRegister = registerProvider.getFreeRegister()
                val pathRegister = registerProvider.getFreeRegister()

                addInstructionsAtControlFlowLabel(
                    insertIndex,
                    """
                        move-object/from16 v$freeRegister, p2
                        
                        # In YouTube 20.41 field is the abstract superclass.
                        # Verify it's the expected subclass just in case.
                        instance-of v$pathRegister, v$freeRegister, ${conversionContextToStringMethod.immutableClassDef}
                        if-eqz v$pathRegister, :ignore
                        
                        iget-object v$pathRegister, v$freeRegister, $conversionContextPathBuilderField
                        invoke-static { v$pathRegister, v$listRegister }, ${FILTER_CLASS_DESCRIPTOR}->hideActionButtons(Ljava/lang/StringBuilder;Ljava/util/List;)V
                        :ignore
                        nop
                    """
                )
            }
        }

        // endregion

        // region Hide the navigation bar.

        // Hook to get the pivotBar view.
        setPivotBarVisibilityParentMethod.immutableClassDef.setPivotBarVisibilityMethodMatch.let { match ->
            match.method.apply {
                val insertIndex = match[-1]
                val viewRegister = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
                addInstruction(
                    insertIndex,
                    "invoke-static {v$viewRegister}," +
                            " $FILTER_CLASS_DESCRIPTOR->setNavigationBar(Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;)V",
                )
            }
        }

        // Hook to hide the shared navigation bar when the Shorts player is opened.
        (
                if (is_20_45_or_greater) {
                    renderBottomNavigationBarParentMethod
                } else if (is_19_41_or_greater) {
                    renderBottomNavigationBarLegacy1941ParentMethod
                } else {
                    legacyRenderBottomNavigationBarLegacyParentMethod
                }
                ).immutableClassDef.getRenderBottomNavigationBarMethodMatch().addInstruction(
                0,
                "invoke-static { p1 }, $FILTER_CLASS_DESCRIPTOR->hideNavigationBar(Ljava/lang/String;)V",
            )

        // Hide the bottom bar container of the Shorts player.
        shortsBottomBarContainerMethodMatch.let {
            it.method.apply {
                val targetIndex = it[-1]
                val heightRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1,
                    """
                        invoke-static { v$heightRegister }, $FILTER_CLASS_DESCRIPTOR->getNavigationBarHeight(I)I
                        move-result v$heightRegister
                    """,
                )
            }
        }

        // endregion

        // region Disable experimental Shorts flags.

        // Flags might be present in earlier targets, but they are not found in 19.47.53.
        // If these flags are forced on, the experimental layout is still not used, and
        // it appears the features requires additional server side data to fully use.
        if (is_20_07_or_greater) {
            // Experimental Shorts player uses Android native buttons and not Litho,
            // and the layout is provided by the server.
            //
            // Since the buttons are native components and not Litho, it should be possible to
            // fix the RYD Shorts loading delay by asynchronously loading RYD and updating
            // the button text after RYD has loaded.
            shortsExperimentalPlayerFeatureFlagMethod.returnLate(false)

            // Experimental UI renderer must also be disabled since it requires the
            // experimental Shorts player. If this is enabled but Shorts player
            // is disabled then the app crashes when the Shorts player is opened.
            renderNextUIFeatureFlagMethod.returnLate(false)
        }

        // endregion
    }
}
