package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.layout.compactheader.fingerprints.ConstructCategoryBarFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide category bar",
    description = "Hides the category bar at the top of the homepage.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
    use = false,
)
@Suppress("unused")
object HideCategoryBar : BytecodePatch(
    setOf(ConstructCategoryBarFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        ConstructCategoryBarFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex
                val register = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

                addInstructions(
                    insertIndex,
                    """
                        const/16 v2, 0x8
                        invoke-virtual {v$register, v2}, Landroid/view/View;->setVisibility(I)V
                    """,
                )
            }
        } ?: throw ConstructCategoryBarFingerprint.exception
    }
}

@Deprecated("This patch class has been renamed to HideCategoryBar.")
object CompactHeaderPatch : BytecodePatch(
    dependencies = setOf(HideCategoryBar::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
