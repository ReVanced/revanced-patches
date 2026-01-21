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
    description = "Removes the popup that appears after a while, when the app version ages.",
) {
    compatibleWith("com.instagram.android")

    execute {
        appUpdateLockoutBuilderFingerprint.method.apply {
            val longToIntIndex = instructions.first { it.opcode == Opcode.LONG_TO_INT }.location.index
            val appAgeRegister = getInstruction<TwoRegisterInstruction>(longToIntIndex).registerA

            // Set app age to 0 days old such that the build expired popup doesn't appear.
            addInstruction(longToIntIndex + 1, "const v$appAgeRegister, 0x0")
        }
    }
}

