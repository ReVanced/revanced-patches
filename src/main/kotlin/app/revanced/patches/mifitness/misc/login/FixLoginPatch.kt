package app.revanced.patches.mifitness.misc.login

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.mifitness.misc.login.fingerprints.XiaomiAccountManagerConstructorFingerprint
import app.revanced.patches.mifitness.misc.login.fingerprints.xiaomiAccountManagerConstructorFingerprint
import app.revanced.util.exception

@Suppress("unused")
val fixLoginPatch = bytecodePatch(
    name = "Fix login",
    description = "Fixes login for uncertified Mi Fitness app",
) {
    compatibleWith("com.xiaomi.wearable")

    val xiaomiAccountManagerConstructorResult by xiaomiAccountManagerConstructorFingerprint

    execute {
        xiaomiAccountManagerConstructorResult.mutableMethod.addInstruction(0, "const/16 p2, 0x0")
    }
}