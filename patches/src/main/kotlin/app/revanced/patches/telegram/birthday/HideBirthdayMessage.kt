package app.revanced.patches.telegram.birthday

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val removeBirthdayMassagePatch = bytecodePatch(
    name = "Remove birthday input message",
    description = "Removes the message asking for your birthday.",
) {
    compatibleWith("org.telegram.messenger")

    execute {
        birthdayStateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """,
        )
    }
}
