package app.revanced.patches.telegram.birthday

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val removeBirthdayMassagePatch = bytecodePatch(
    name = "Remove birthday input message",
    description = "Removes the message asking for your birthday.",
) {
    compatibleWith("org.telegram.messenger")

    // Return null early. This is also done, when the message is supposed to be hidden.
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
