package app.revanced.patches.protonvpn.splittunneling

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.extensions.replaceInstruction
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

        apply {
            val registerIndex = enableSplitTunnelingUiMethod.patternMatch!!.endIndex - 1 // TODO

            enableSplitTunnelingUiMethod.apply {
                val register = getInstruction<OneRegisterInstruction>(registerIndex).registerA
                replaceInstruction(registerIndex, "const/4 v$register, 0x0")
            }

            initializeSplitTunnelingSettingsUIMethod.apply {
                val initSettingsIndex = indexOfFirstInstructionOrThrow {
                    getReference<MethodReference>()?.name == "getSplitTunneling"
                }
                removeInstruction(initSettingsIndex - 1)
            }
        }
    }
