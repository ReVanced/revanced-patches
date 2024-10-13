package app.revanced.patches.mifitness.misc.login

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val fixLoginPatch = bytecodePatch(
    name = "Fix login",
    description = "Fixes login for uncertified Mi Fitness app",
) {
    compatibleWith("com.xiaomi.wearable")

    val xiaomiAccountManagerConstructorMatch by xiaomiAccountManagerConstructorFingerprint()

    execute {
        xiaomiAccountManagerConstructorMatch.mutableMethod.addInstruction(0, "const/16 p2, 0x0")
    }
}
