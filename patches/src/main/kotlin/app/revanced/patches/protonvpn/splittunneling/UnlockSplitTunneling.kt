package app.revanced.patches.protonvpn.splittunneling

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val unlockSplitTunnelingPatch =
    bytecodePatch(
        name = "Unlock split tunneling",
    ) {
        compatibleWith("ch.protonvpn.android")

        execute {
            val registerIndex = enableSplitTunnelingUiFingerprint.patternMatch!!.endIndex - 1

            enableSplitTunnelingUiFingerprint.method.apply {
                val register = getInstruction<OneRegisterInstruction>(registerIndex).registerA
                replaceInstruction(registerIndex, "const/4 v$register, 0x0")
            }

            initializeSplitTunnelingSettingsUIFingerprint.method.apply {
                val initSettingsIndex = indexOfFirstInstructionOrThrow {
                    getReference<MethodReference>()?.name == "getSplitTunneling"
                }
                removeInstruction(initSettingsIndex - 1)
            }
        }
    }
