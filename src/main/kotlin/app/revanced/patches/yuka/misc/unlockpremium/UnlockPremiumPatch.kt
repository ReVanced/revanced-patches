package app.revanced.patches.yuka.misc.unlockpremium

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
) {
    compatibleWith("io.yuka.android"("4.29"))

    val yukaUserConstructorMatch by yukaUserConstructorFingerprint()

    execute { context ->
        isPremiumFingerprint.apply {
            match(context, yukaUserConstructorMatch.classDef)
        }.matchOrThrow().mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
