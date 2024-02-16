package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.mifitness.misc.locale.fingerprints.SyncBluetoothLanguageFingerprint
import app.revanced.patches.mifitness.misc.login.FixLoginPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Force English locale",
    description = "Forces the app to use the English locale.",
    compatiblePackages = [CompatiblePackage("com.xiaomi.wearable")],
    dependencies = [FixLoginPatch::class],
)
@Suppress("unused")
object ForceEnglishLocalePatch : BytecodePatch(
    setOf(SyncBluetoothLanguageFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        SyncBluetoothLanguageFingerprint.result?.apply {
            val instructionIndex = scanResult.patternScanResult!!.startIndex

            mutableMethod.apply {
                val registerIndexToUpdate = getInstruction<OneRegisterInstruction>(instructionIndex).registerA

                replaceInstruction(
                    instructionIndex,
                    "const-string v$registerIndexToUpdate, \"en_gb\"",
                )
            }
        } ?: throw SyncBluetoothLanguageFingerprint.exception
    }
}
