package app.revanced.patches.youtube.video.videoqualitymenu.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.bottomsheet.hook.patch.BottomSheetHookPatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.video.videoqualitymenu.annotations.OldVideoQualityMenuCompatibility
import app.revanced.patches.youtube.video.videoqualitymenu.fingerprints.VideoQualityMenuViewInflateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([
    IntegrationsPatch::class,
    OldVideoQualityMenuResourcePatch::class,
    LithoFilterPatch::class,
    BottomSheetHookPatch::class
])
@Name("Old video quality menu")
@Description("Shows the old video quality with the advanced video quality options instead of the new one.")
@OldVideoQualityMenuCompatibility
class OldVideoQualityMenuPatch : BytecodePatch(
    listOf(VideoQualityMenuViewInflateFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // region Patch for the old type of the video quality menu.

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

        BottomSheetHookPatch.addHook(INTEGRATIONS_CLASS_DESCRIPTOR)

        // Required to check if the video quality menu is currently shown in order to click on the "Advanced" item.
        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion
    }

    private companion object {
        private const val FILTER_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/components/VideoQualityMenuFilterPatch;"

        private const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/playback/quality/OldVideoQualityMenuPatch;"
    }
}