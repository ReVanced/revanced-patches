package app.revanced.patches.youtube.video.videoqualitymenu

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal var videoQualityBottomSheetListFragmentTitle = -1L
    private set
internal var videoQualityQuickMenuAdvancedMenuDescription = -1L
    private set

private val restoreOldVideoQualityMenuResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "video.videoqualitymenu.restoreOldVideoQualityMenuResourcePatch")

        PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_restore_old_video_quality_menu"),
        )

        // Used for the old type of the video quality menu.
        videoQualityBottomSheetListFragmentTitle = resourceMappings[
            "layout",
            "video_quality_bottom_sheet_list_fragment_title",
        ]

        videoQualityQuickMenuAdvancedMenuDescription = resourceMappings[
            "string",
            "video_quality_quick_menu_advanced_menu_description",
        ]
    }
}

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/components/VideoQualityMenuFilterPatch;"

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/quality/RestoreOldVideoQualityMenuPatch;"

@Suppress("unused")
val restoreOldVideoQualityMenuPatch = bytecodePatch(
    name = "Restore old video quality menu",
    description = "Adds an option to restore the old video quality menu with specific video resolution options.",

) {
    dependsOn(
        sharedExtensionPatch,
        restoreOldVideoQualityMenuResourcePatch,
        lithoFilterPatch,
        recyclerViewTreeHookPatch,
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

    execute {
        // region Patch for the old type of the video quality menu.
        // Used for regular videos when spoofing to old app version,
        // and for the Shorts quality flyout on newer app versions.

        videoQualityMenuViewInflateFingerprint.method.apply {
            val checkCastIndex = videoQualityMenuViewInflateFingerprint.filterMatches.last().index
            val listViewRegister = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

            addInstruction(
                checkCastIndex + 1,
                "invoke-static { v$listViewRegister }, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->" +
                    "showOldVideoQualityMenu(Landroid/widget/ListView;)V",
            )
        }

        // Force YT to add the 'advanced' quality menu for Shorts.
        val patternMatch = videoQualityMenuOptionsFingerprint.patternMatch!!
        val startIndex = patternMatch.startIndex
        if (startIndex != 0) throw PatchException("Unexpected opcode start index: $startIndex")
        val insertIndex = patternMatch.endIndex

        videoQualityMenuOptionsFingerprint.method.apply {
            val register = getInstruction<OneRegisterInstruction>(insertIndex).registerA

            // A condition controls whether to show the three or four items quality menu.
            // Force the four items quality menu to make the "Advanced" item visible, necessary for the patch.
            addInstructions(
                insertIndex,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->forceAdvancedVideoQualityMenuCreation(Z)Z
                    move-result v$register
                """,
            )
        }

        // endregion

        // region Patch for the new type of the video quality menu.

        addRecyclerViewTreeHook(EXTENSION_CLASS_DESCRIPTOR)

        // Required to check if the video quality menu is currently shown in order to click on the "Advanced" item.
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion
    }
}
