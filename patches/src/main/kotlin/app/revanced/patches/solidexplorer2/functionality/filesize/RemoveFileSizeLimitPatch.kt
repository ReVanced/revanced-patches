package app.revanced.patches.solidexplorer2.functionality.filesize

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.ThreeRegisterInstruction

@Suppress("unused")
val removeFileSizeLimitPatch = bytecodePatch(
    name = "Remove file size limit",
    description = "Allows opening files larger than 2 MB in the text editor.",
) {
    compatibleWith("pl.solidexplorer2")

    execute {
        onReadyFingerprint.method.apply {
            val cmpIndex = onReadyFingerprint.patternMatch!!.startIndex + 1
            val cmpResultRegister = getInstruction<ThreeRegisterInstruction>(cmpIndex).registerA

            replaceInstruction(cmpIndex, "const/4 v$cmpResultRegister, 0x0")
        }
    }
}
