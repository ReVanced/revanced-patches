package app.revanced.patches.youtube.video.videoqualitymenu

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHookPatch
import app.revanced.patches.youtube.video.videoqualitymenu.fingerprints.videoQualityMenuOptionsFingerprint
import app.revanced.patches.youtube.video.videoqualitymenu.fingerprints.videoQualityMenuViewInflateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/components/VideoQualityMenuFilterPatch;"

private const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/playback/quality/RestoreOldVideoQualityMenuPatch;"

@Suppress("unused")
val restoreOldVideoQualityMenuPatch = bytecodePatch(
    name = "Restore old video quality menu",
    description = "Adds an option to restore the old video quality menu with specific video resolution options.",

) {
    dependsOn(
        integrationsPatch,
        restoreOldVideoQualityMenuResourcePatch,
        lithoFilterPatch,
        recyclerViewTreeHookPatch,
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
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val videoQualityMenuViewInflateResult by videoQualityMenuViewInflateFingerprint
    val videoQualityMenuOptionsResult by videoQualityMenuOptionsFingerprint

    execute {
        // region Patch for the old type of the video quality menu.
        // Used for regular videos when spoofing to old app version,
        // and for the Shorts quality flyout on newer app versions.

        videoQualityMenuViewInflateResult.mutableMethod.apply {
            val checkCastIndex = videoQualityMenuViewInflateResult.scanResult.patternScanResult!!.endIndex
            val listViewRegister = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

            addInstruction(
                checkCastIndex + 1,
                "invoke-static { v$listViewRegister }, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->" +
                    "showOldVideoQualityMenu(Landroid/widget/ListView;)V",
            )
        }

        // Force YT to add the 'advanced' quality menu for Shorts.
        val scanResult = videoQualityMenuOptionsResult.scanResult.patternScanResult!!
        val startIndex = scanResult.startIndex
        if (startIndex != 0) throw PatchException("Unexpected opcode start index: $startIndex")
        val insertIndex = scanResult.endIndex

        videoQualityMenuOptionsResult.mutableMethod.apply {
            val register = getInstruction<OneRegisterInstruction>(insertIndex).registerA

            // A condition controls whether to show the three or four items quality menu.
            // Force the four items quality menu to make the "Advanced" item visible, necessary for the patch.
            addInstructions(
                insertIndex,
                """
                    invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->forceAdvancedVideoQualityMenuCreation(Z)Z
                    move-result v$register
                """,
            )
        }

        // endregion

        // region Patch for the new type of the video quality menu.

        addRecyclerViewTreeHook(INTEGRATIONS_CLASS_DESCRIPTOR)

        // Required to check if the video quality menu is currently shown in order to click on the "Advanced" item.
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion
    }
}
