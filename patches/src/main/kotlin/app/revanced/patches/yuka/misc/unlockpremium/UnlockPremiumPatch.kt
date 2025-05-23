package app.revanced.patches.yuka.misc.unlockpremium

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Deprecated("This patch no longer works and will be removed in the future.")
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch {

    compatibleWith("io.yuka.android"("4.29"))

    execute {
        isPremiumFingerprint.match(
            yukaUserConstructorFingerprint.originalClassDef,
        ).method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
