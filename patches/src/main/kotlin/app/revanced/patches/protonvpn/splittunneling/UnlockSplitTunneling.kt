package app.revanced.patches.protonvpn.splittunneling

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.util.indexOfFirstInstructionOrThrow

import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockSplitTunnelingPatch = bytecodePatch(
    name = "Unlock split tunneling",
    description = "Unlocks split tunneling",
) {
    compatibleWith("ch.protonvpn.android")

    execute {
        val registerIndex = uiUnlock.patternMatch!!.endIndex - 1

        uiUnlock.method.apply {
            val register = getInstruction<OneRegisterInstruction>(registerIndex).registerA
            replaceInstruction(
                registerIndex,
                "const/4 v$register, 0x0",
            )
        }

        settingInit.method.apply {
            val initSettingsIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.name == "getSplitTunneling"
            }
            removeInstruction(initSettingsIndex - 1)
    }
}
}
        