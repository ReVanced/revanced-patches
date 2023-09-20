package app.revanced.patches.youtube.video.videoqualitymenu

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.RecyclerViewTreeHookPatch
import app.revanced.patches.youtube.video.videoqualitymenu.fingerprints.VideoQualityMenuViewInflateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Old video quality menu",
    description = "Shows the old video quality with the advanced video quality options instead of the new one.",
    dependencies = [
        IntegrationsPatch::class,
        OldVideoQualityMenuResourcePatch::class,
        LithoFilterPatch::class,
        RecyclerViewTreeHookPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object OldVideoQualityMenuPatch : BytecodePatch(
    setOf(VideoQualityMenuViewInflateFingerprint)
) {
    private const val FILTER_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/components/VideoQualityMenuFilterPatch;"

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/playback/quality/OldVideoQualityMenuPatch;"

    override fun execute(context: BytecodeContext) {
        // region Patch for the old type of the video quality menu.
        // Only used when spoofing to old app version.

        VideoQualityMenuViewInflateFingerprint.result?.let {
            it.mutableMethod.apply {
                val checkCastIndex = it.scanResult.patternScanResult!!.endIndex
                val listViewRegister = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

                addInstruction(
                    checkCastIndex + 1,
                    "invoke-static { v$listViewRegister }, " +
                            "$INTEGRATIONS_CLASS_DESCRIPTOR->" +
                            "showOldVideoQualityMenu(Landroid/widget/ListView;)V"
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