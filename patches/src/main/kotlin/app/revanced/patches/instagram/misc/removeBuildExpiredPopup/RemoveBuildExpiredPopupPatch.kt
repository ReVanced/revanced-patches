package app.revanced.patches.instagram.misc.removeBuildExpiredPopup

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val removeBuildExpiredPopupPatch = bytecodePatch(
    name = "Remove build expired popup",
    description = "Removes build expired popup while using an older build.",
) {
    compatibleWith("com.instagram.android")

    execute {
        invokeMethodFingerprint.method.apply {
            val longToIntIndex = instructions.first { it.opcode == Opcode.LONG_TO_INT }.location.index
            val register = getInstruction<TwoRegisterInstruction>(longToIntIndex).registerA
            addInstruction(longToIntIndex+1,"const v$register, 0x0")
        }
    }
}

