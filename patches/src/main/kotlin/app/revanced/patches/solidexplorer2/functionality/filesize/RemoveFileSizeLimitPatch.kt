package app.revanced.patches.solidexplorer2.functionality.filesize

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.ThreeRegisterInstruction

@Suppress("unused")
val removeFileSizeLimitPatch = bytecodePatch(
    name = "Remove file size limit",
    description = "Allows opening files larger than 2 MB in the text editor.",
) {
    compatibleWith("pl.solidexplorer2")

    apply {
        onReadyMethod.apply {
            val cmpIndex = onReadyMethod.instructionMatches.first().index + 1 // TODO
            val cmpResultRegister = getInstruction<ThreeRegisterInstruction>(cmpIndex).registerA

            replaceInstruction(cmpIndex, "const/4 v$cmpResultRegister, 0x0")
        }
    }
}
