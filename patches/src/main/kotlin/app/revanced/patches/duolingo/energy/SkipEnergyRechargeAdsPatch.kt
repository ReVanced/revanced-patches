package app.revanced.patches.duolingo.energy

import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.findFieldFromToString

@Suppress("unused")
val skipEnergyRechargeAdsPatch = bytecodePatch(
    name = "Skip energy recharge ads",
    description = "Skips watching ads to recharge energy.",
) {
    compatibleWith("com.duolingo")

    apply {
        initializeEnergyConfigMethodMatch.match(energyConfigToStringMethod.classDef).method.apply {
            val energyField = energyConfigToStringMethod.findFieldFromToString("energy=")
            val insertIndex = initializeEnergyConfigMethodMatch.indices.first() // TODO

            addInstructions(
                insertIndex,
                """
                    const/16 v0, 99
                    iput v0, p0, $energyField
                """,
            )
        }
    }
}
