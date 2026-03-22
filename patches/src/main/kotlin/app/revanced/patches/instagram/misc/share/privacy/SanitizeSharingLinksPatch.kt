package app.revanced.patches.instagram.misc.share.privacy

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.forEachInstructionAsSequence
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links.",
) {
    compatibleWith("com.instagram.android")

    apply {
        forEachInstructionAsSequence(
            match = { classDef, _, instruction, index ->
                if (!classDef.type.startsWith("Lcom/instagram/")) return@forEachInstructionAsSequence null
                if (instruction !is ReferenceInstruction) return@forEachInstructionAsSequence null
                val reference = instruction.reference as? StringReference
                    ?: return@forEachInstructionAsSequence null
                if (reference.string != "igsh") return@forEachInstructionAsSequence null

                return@forEachInstructionAsSequence index
            },
            transform = { method, index ->
                val register = method.getInstruction<OneRegisterInstruction>(index).registerA
                method.replaceInstruction(index, "const-string v$register, \"\"")
            },
        )
    }
}
