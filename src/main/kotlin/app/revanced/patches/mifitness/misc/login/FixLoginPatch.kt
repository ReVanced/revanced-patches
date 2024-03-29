package app.revanced.patches.mifitness.misc.login

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.mifitness.misc.login.fingerprints.XiaomiAccountManagerConstructorFingerprint
import app.revanced.util.exception

@Patch(
    name = "Fix login",
    description = "Fixes login for uncertified Mi Fitness app",
    compatiblePackages = [CompatiblePackage("com.xiaomi.wearable")],
)
@Suppress("unused")
object FixLoginPatch : BytecodePatch(
    setOf(XiaomiAccountManagerConstructorFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        XiaomiAccountManagerConstructorFingerprint.result?.mutableMethod?.addInstruction(
            0,
            "const/16 p2, 0x0",
        ) ?: throw XiaomiAccountManagerConstructorFingerprint.exception
    }
}
