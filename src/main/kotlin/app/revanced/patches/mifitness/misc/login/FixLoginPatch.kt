package app.revanced.patches.mifitness.misc.login

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.mifitness.misc.login.fingerprints.XiaomiAccountManagerConstructorFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

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
        XiaomiAccountManagerConstructorFingerprint.result?.let {
            it.mutableMethod.apply {
                val isCertifiedIndex = it.scanResult.patternScanResult!!.startIndex
                val isCertifiedRegister = getInstruction<OneRegisterInstruction>(isCertifiedIndex).registerA

                addInstruction(
                    isCertifiedIndex,
                    "const/4 p$isCertifiedRegister, 0x0",
                )
            }
        } ?: throw XiaomiAccountManagerConstructorFingerprint.exception
    }
}
