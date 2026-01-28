package app.revanced.patches.solidexplorer2.functionality.filesize

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.ThreeRegisterInstruction

@Suppress("unused", "ObjectPropertyName")
val `Remove file size limit` by creatingBytecodePatch(
    description = "Allows opening files larger than 2 MB in the text editor.",
) {
    compatibleWith("pl.solidexplorer2")

    apply {
        onReadyMethodMatch.let {
            val cmpIndex = it.indices.first() + 1
            val cmpResultRegister = it.method.getInstruction<ThreeRegisterInstruction>(cmpIndex).registerA

            it.method.replaceInstruction(cmpIndex, "const/4 v$cmpResultRegister, 0x0")
        }
    }
}
