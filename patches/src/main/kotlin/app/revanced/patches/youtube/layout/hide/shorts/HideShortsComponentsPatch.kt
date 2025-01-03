package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.playservice.is_19_41_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private var reelPlayerRightPivotV2Size = -1L

internal val hideShortsAppShortcutOption = booleanOption(
    key = "hideShortsAppShortcut",
    default = false,
    title = "Hide Shorts app shortcut",
    description = "Permanently hides the shortcut to open Shorts when long pressing the app icon in your launcher.",
)

internal val hideShortsWidgetOption = booleanOption(
    key = "hideShortsWidget",
    default = false,
    title = "Hide Shorts widget",
    description = "Permanently hides the launcher widget Shorts button.",
)

private val hideShortsComponentsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
        versionCheckPatch,
    )

    execute {
        val hideShortsAppShortcut by hideShortsAppShortcutOption
        val hideShortsWidget by hideShortsWidgetOption

        addResources("youtube", "layout.hide.shorts.hideShortsComponentsResourcePatch")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_hide_shorts_home"),
            SwitchPreference("revanced_hide_shorts_subscriptions"),
            SwitchPreference("revanced_hide_shorts_search"),
            SwitchPreference("revanced_hide_shorts_history"),

            PreferenceScreenPreference(
                key = "revanced_shorts_player_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    // Shorts player components.
                    // Ideally each group should be ordered similar to how they appear in the UI

                    // Vertical row of buttons on right side of the screen.
                    SwitchPreference("revanced_hide_shorts_like_fountain"),
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
                    SwitchPreference("revanced_hide_shorts_save_sound_button"),
                    SwitchPreference("revanced_hide_shorts_use_template_button"),
                    SwitchPreference("revanced_hide_shorts_upcoming_button"),
                    SwitchPreference("revanced_hide_shorts_green_screen_button"),
                    SwitchPreference("revanced_hide_shorts_hashtag_button"),
                    SwitchPreference("revanced_hide_shorts_shop_button"),
                    SwitchPreference("revanced_hide_shorts_tagged_products"),
                    SwitchPreference("revanced_hide_shorts_search_suggestions"),
                    SwitchPreference("revanced_hide_shorts_super_thanks_button"),
                    SwitchPreference("revanced_hide_shorts_stickers"),

                    // Bottom of the screen.
                    SwitchPreference("revanced_hide_shorts_location_label"),
                    SwitchPreference("revanced_hide_shorts_channel_bar"),
                    SwitchPreference("revanced_hide_shorts_info_panel"),
                    SwitchPreference("revanced_hide_shorts_full_video_link_label"),
                    SwitchPreference("revanced_hide_shorts_video_title"),
                    SwitchPreference("revanced_hide_shorts_sound_metadata_label"),
                    SwitchPreference("revanced_hide_shorts_navigation_bar"),
                ),
            ),
        )

        // Verify the file has the expected node, even if the patch option is off.
        document("res/xml/main_shortcuts.xml").use { document ->
            val shortsItem = document.childNodes.findElementByAttributeValueOrThrow(
                "android:shortcutId",
                "shorts-shortcut",
            )

            if (hideShortsAppShortcut == true) {
                shortsItem.parentNode.removeChild(shortsItem)
            }
        }

        document("res/layout/appwidget_two_rows.xml").use { document ->
            val shortsItem = document.childNodes.findElementByAttributeValueOrThrow(
                "android:id",
                "@id/button_shorts_container",
            )

            if (hideShortsWidget == true) {
                shortsItem.parentNode.removeChild(shortsItem)
            }
        }

        reelPlayerRightPivotV2Size = resourceMappings[
            "dimen",
            "reel_player_right_pivot_v2_size",
        ]
    }
}

private const val FILTER_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/components/ShortsFilter;"

@Suppress("unused")
val hideShortsComponentsPatch = bytecodePatch(
    name = "Hide Shorts components",
    description = "Adds options to hide components related to YouTube Shorts.",
) {
    dependsOn(
        sharedExtensionPatch,
        lithoFilterPatch,
        hideShortsComponentsResourcePatch,
        resourceMappingPatch,
        navigationBarHookPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    hideShortsAppShortcutOption()
    hideShortsWidgetOption()

    execute {
        // region Hide the Shorts buttons in older versions of YouTube.

        // Some Shorts buttons are views, hide them by setting their visibility to GONE.
        ShortsButtons.entries.forEach { button -> button.injectHideCall(createShortsButtonsFingerprint.method) }

        // endregion

        // region Hide the Shorts buttons in newer versions of YouTube.

        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        forEachLiteralValueInstruction(
            reelPlayerRightPivotV2Size,
        ) { literalInstructionIndex ->
            val targetIndex = indexOfFirstInstructionOrThrow(literalInstructionIndex) {
                getReference<MethodReference>()?.name == "getDimensionPixelSize"
            } + 1

            val sizeRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            addInstructions(
                targetIndex + 1,
                """
                    invoke-static { v$sizeRegister }, $FILTER_CLASS_DESCRIPTOR->getSoundButtonSize(I)I
                    move-result v$sizeRegister
                """,
            )
        }

        // endregion

        // region Hide the navigation bar.

        // Hook to get the pivotBar view.
        setPivotBarVisibilityFingerprint.match(
            setPivotBarVisibilityParentFingerprint.originalClassDef,
        ).let { result ->
            result.method.apply {
                val insertIndex = result.patternMatch.endIndex
                val viewRegister = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
                addInstruction(
                    insertIndex,
                    "invoke-static {v$viewRegister}," +
                        " $FILTER_CLASS_DESCRIPTOR->setNavigationBar(Lcom/google/android/libraries/youtube/rendering/ui/pivotbar/PivotBar;)V",
                )
            }
        }

        // Hook to hide the shared navigation bar when the Shorts player is opened.
        renderBottomNavigationBarFingerprint.match(
            if (is_19_41_or_greater) {
                renderBottomNavigationBarParentFingerprint
            } else {
                legacyRenderBottomNavigationBarParentFingerprint
            }.originalClassDef,
        ).method.addInstruction(
            0,
            "invoke-static { p1 }, $FILTER_CLASS_DESCRIPTOR->hideNavigationBar(Ljava/lang/String;)V",
        )

        // Hide the bottom bar container of the Shorts player.
        shortsBottomBarContainerFingerprint.let {
            it.method.apply {
                val targetIndex = it.filterMatches.last().index
                val heightRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1,
                    """
                        invoke-static { v$heightRegister }, $FILTER_CLASS_DESCRIPTOR->getNavigationBarHeight(I)I
                        move-result v$heightRegister
                    """
                )
            }
        }

        // endregion
    }
}

private enum class ShortsButtons(private val resourceName: String, private val methodName: String) {
    LIKE("reel_dyn_like", "hideLikeButton"),
    DISLIKE("reel_dyn_dislike", "hideDislikeButton"),
    COMMENTS("reel_dyn_comment", "hideShortsCommentsButton"),
    REMIX("reel_dyn_remix", "hideShortsRemixButton"),
    SHARE("reel_dyn_share", "hideShortsShareButton");

    fun injectHideCall(method: MutableMethod) {
        val referencedIndex = method.indexOfFirstResourceIdOrThrow(resourceName)

        val setIdIndex = method.indexOfFirstInstructionOrThrow(referencedIndex) {
            opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.name == "setId"
        }

        val viewRegister = method.getInstruction<FiveRegisterInstruction>(setIdIndex).registerC

        method.injectHideViewCall(setIdIndex + 1, viewRegister, FILTER_CLASS_DESCRIPTOR, methodName)
    }
}
