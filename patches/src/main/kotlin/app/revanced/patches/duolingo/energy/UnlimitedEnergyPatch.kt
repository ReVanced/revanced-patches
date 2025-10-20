package app.revanced.patches.duolingo.energy

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    "Unlimited energy",
) {
    compatibleWith("com.duolingo")

    execute {
        initializeEnergyConfigFingerprint
            .match(energyConfigToStringFingerprint.classDef)
            .method.apply {
                val insertIndex = initializeEnergyConfigFingerprint.patternMatch!!.startIndex
                val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    "const/16 v$register, 0x63", // Set to 99
                )
            }
    }
}
