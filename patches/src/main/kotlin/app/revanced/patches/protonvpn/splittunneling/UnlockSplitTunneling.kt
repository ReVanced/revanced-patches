package app.revanced.patches.protonvpn.splittunneling

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused", "ObjectPropertyName")
val `Unlock split tunneling` by creatingBytecodePatch {
    compatibleWith("ch.protonvpn.android")

    apply {
        enableSplitTunnelingUiMethodMatch.let {
            val registerIndex = it.indices.last() - 1
            val register = it.method.getInstruction<OneRegisterInstruction>(registerIndex).registerA
            it.method.replaceInstruction(registerIndex, "const/4 v$register, 0x0")
        }

        initializeSplitTunnelingSettingsUIMethod.apply {
            val initSettingsIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>()?.name == "getSplitTunneling"
            }
            removeInstruction(initSettingsIndex - 1)
        }
    }
}
