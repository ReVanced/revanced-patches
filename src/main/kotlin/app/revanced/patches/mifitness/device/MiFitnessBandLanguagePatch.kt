package app.revanced.patches.mifitness.device

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.mifitness.MiFitnessLoginPatch
import app.revanced.patches.mifitness.device.fingerprints.MiFitnessBandLanguageFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Force English",
    description = "Sets language to EN for all wear devices like Mi Band 8 CN.",
    compatiblePackages = [CompatiblePackage("com.xiaomi.wearable")],
    dependencies = [MiFitnessLoginPatch::class]
)
@Suppress("unused")
object MiFitnessBandLanguagePatch : BytecodePatch(
    setOf(MiFitnessBandLanguageFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        MiFitnessBandLanguageFingerprint.result?.apply {
            val instructionIndex = this.scanResult.patternScanResult!!.startIndex

            this.mutableMethod.apply {
                val registerIndexToUpdate = getInstruction<OneRegisterInstruction>(instructionIndex).registerA

                replaceInstruction(instructionIndex, """
                    const-string v$registerIndexToUpdate, "en_gb"
                """.trimIndent())
            }
        } ?: throw MiFitnessBandLanguageFingerprint.exception
    }
}
