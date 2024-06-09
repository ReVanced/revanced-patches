package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.layout.compactheader.fingerprints.constructCategoryBarFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideCategoryBar = bytecodePatch(
    name = "Hide category bar",
    description = "Hides the category bar at the top of the homepage.",
    use = false,
) {
    compatibleWith("com.google.android.apps.youtube.music")

    val constructCategoryBarResult by constructCategoryBarFingerprint

    execute {
        constructCategoryBarResult.mutableMethod.apply {
            val insertIndex = constructCategoryBarResult.scanResult.patternScanResult!!.startIndex
            val register = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

            addInstructions(
                insertIndex,
                """
                    const/16 v2, 0x8
                    invoke-virtual {v$register, v2}, Landroid/view/View;->setVisibility(I)V
                """,
            )
        }
    }
}