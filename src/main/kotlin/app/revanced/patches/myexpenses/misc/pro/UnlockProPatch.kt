package app.revanced.patches.myexpenses.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.myexpenses.misc.pro.fingerprints.isEnabledFingerprint

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro"
) {
    compatibleWith("org.totschnig.myexpenses"("3.4.9"))

    val isEnabledResult by isEnabledFingerprint

    execute {
        isEnabledResult.mutableMethod.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
        """
        )
    }
}
