package app.revanced.patches.gamehub.ui.tabs

import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

@Suppress("unused")
val disableTabsPatch = bytecodePatch(
    name = "Disable Discover and Find games tabs",
    description = "Hides the Discover and Find games tabs from the main launcher.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))

    execute {
        initViewFingerprint.method.apply {
            val discoverNewInstanceIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.NEW_INSTANCE &&
                        (this as? ReferenceInstruction)?.reference?.let {
                            it is TypeReference && it.type == "Lcom/xj/landscape/launcher/ui/main/w;"
                        } == true
            }
            val discoverAddIndex = indexOfFirstInstructionOrThrow(discoverNewInstanceIndex) {
                opcode == Opcode.INVOKE_INTERFACE &&
                        (this as? ReferenceInstruction)?.reference?.let {
                            it is MethodReference && it.name == "add"
                        } == true
            }

            val findGamesNewInstanceIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.NEW_INSTANCE &&
                        (this as? ReferenceInstruction)?.reference?.let {
                            it is TypeReference && it.type == "Lcom/xj/landscape/launcher/ui/main/x;"
                        } == true
            }
            val findGamesAddIndex = indexOfFirstInstructionOrThrow(findGamesNewInstanceIndex) {
                opcode == Opcode.INVOKE_INTERFACE &&
                        (this as? ReferenceInstruction)?.reference?.let {
                            it is MethodReference && it.name == "add"
                        } == true
            }

            // Remove in reverse order to preserve indices
            removeInstruction(findGamesAddIndex)
            removeInstruction(discoverAddIndex)
        }
    }
}
