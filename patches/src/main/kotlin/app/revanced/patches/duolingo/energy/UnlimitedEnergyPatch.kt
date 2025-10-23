package app.revanced.patches.duolingo.energy

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.findFieldFromToString

@Suppress("unused")
val skipEnergyRechargeAdsPatch = bytecodePatch(
    name = "Skip energy recharge ads",
    description = "Skips watching ads to recharge energy."
) {
    compatibleWith("com.duolingo")

    execute {
        initializeEnergyConfigFingerprint
            .match(energyConfigToStringFingerprint.classDef)
            .method.apply {
                val energyField = energyConfigToStringFingerprint.method
                    .findFieldFromToString("energy=")
                val insertIndex = initializeEnergyConfigFingerprint.patternMatch!!.startIndex

                addInstructions(
                    insertIndex,
                    """
                        const/16 v0, 99
                        iput v0, p0, $energyField
                    """
                )
            }
    }
}
