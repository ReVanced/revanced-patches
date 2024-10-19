package app.revanced.patches.youtube.video.videoqualitymenu

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.RecyclerViewTreeHookPatch
import app.revanced.patches.youtube.video.videoqualitymenu.fingerprints.VideoQualityMenuOptionsFingerprint
import app.revanced.patches.youtube.video.videoqualitymenu.fingerprints.VideoQualityMenuViewInflateFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Restore old video quality menu",
    description = "Adds an option to restore the old video quality menu with specific video resolution options.",
    dependencies = [
        IntegrationsPatch::class,
        RestoreOldVideoQualityMenuResourcePatch::class,
        LithoFilterPatch::class,
        RecyclerViewTreeHookPatch::class,
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
object RestoreOldVideoQualityMenuPatch : BytecodePatch(
    setOf(VideoQualityMenuViewInflateFingerprint, VideoQualityMenuOptionsFingerprint),
) {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/VideoQualityMenuFilterPatch;"

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/playback/quality/RestoreOldVideoQualityMenuPatch;"

    override fun execute(context: BytecodeContext) {
        // region Patch for the old type of the video quality menu.
        // Used for regular videos when spoofing to old app version,
        // and for the Shorts quality flyout on newer app versions.

        VideoQualityMenuViewInflateFingerprint.result?.let {
            it.mutableMethod.apply {
                val checkCastIndex = it.scanResult.patternScanResult!!.endIndex
                val listViewRegister = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

                addInstruction(
                    checkCastIndex + 1,
                    "invoke-static { v$listViewRegister }, " +
                        "$INTEGRATIONS_CLASS_DESCRIPTOR->" +
                        "showOldVideoQualityMenu(Landroid/widget/ListView;)V",
                )
            }
        }

        // Force YT to add the 'advanced' quality menu for Shorts.
        VideoQualityMenuOptionsFingerprint.resultOrThrow().let {
            val scanResult = it.scanResult.patternScanResult!!
            val startIndex = scanResult.startIndex
            if (startIndex != 0) throw PatchException("Unexpected opcode start index: $startIndex")
            val insertIndex = scanResult.endIndex

            it.mutableMethod.apply {
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
        }

        // endregion

        // region Patch for the new type of the video quality menu.

        RecyclerViewTreeHookPatch.addHook(INTEGRATIONS_CLASS_DESCRIPTOR)

        // Required to check if the video quality menu is currently shown in order to click on the "Advanced" item.
        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion
    }
}
