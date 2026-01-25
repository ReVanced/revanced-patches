package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHookPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal var videoQualityBottomSheetListFragmentname = -1L
    private set
internal var videoQualityQuickMenuAdvancedMenuDescription = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/quality/AdvancedVideoQualityMenuPatch;"

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/components/AdvancedVideoQualityMenuFilter;"

internal val advancedVideoQualityMenuPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        lithoFilterPatch,
        recyclerViewTreeHookPatch,
        resourceMappingPatch,
    )

    apply {
        addResources("youtube", "video.quality.advancedVideoQualityMenuPatch")

        settingsMenuVideoQualityGroup.add(
            SwitchPreference("revanced_advanced_video_quality_menu"),
        )

        // Used for the old type of the video quality menu.
        videoQualityBottomSheetListFragmentname =
            ResourceType.LAYOUT["video_quality_bottom_sheet_list_fragment_title"]
        videoQualityQuickMenuAdvancedMenuDescription =
            ResourceType.STRING["video_quality_quick_menu_advanced_menu_description"]

        // region Patch for the old type of the video quality menu.
        // Used for regular videos when spoofing to old app version,
        // and for the Shorts quality flyout on newer app versions.
        videoQualityMenuViewInflateMethodMatch.let {
            it.method.apply {
                val checkCastIndex = it.indices.last()
                val listViewRegister = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

                addInstruction(
                    checkCastIndex + 1,
                    "invoke-static { v$listViewRegister }, $EXTENSION_CLASS_DESCRIPTOR->" +
                        "addVideoQualityListMenuListener(Landroid/widget/ListView;)V",
                )
            }
        }

        // Force YT to add the 'advanced' quality menu for Shorts.
        videoQualityMenuOptionsMethodMatch.let {
            val startIndex = it.indices.first()
            val insertIndex = it.indices.last()

            if (startIndex != 0) throw PatchException("Unexpected opcode start index: $startIndex")

            it.method.apply {
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
        }

        // endregion

        // region Patch for the new type of the video quality menu.

        addRecyclerViewTreeHook(EXTENSION_CLASS_DESCRIPTOR)

        // Required to check if the video quality menu is currently shown in order to click on the "Advanced" item.
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion
    }
}
