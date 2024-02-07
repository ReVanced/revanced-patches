package app.revanced.patches.mifitness.device

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.mifitness.device.fingerprints.MiFitnessBandLanguageFingerprint
import app.revanced.util.exception

@Patch(
    name = "Set ENG to all Mi Wear devices like Mi Band CN",
    description = "Sets language to EN for all wear devices like Mi Band 8 CN.",
    compatiblePackages = [CompatiblePackage("com.xiaomi.wearable")],
    dependencies = [MiFitnessBandLanguagePatch::class]
)
@Suppress("unused")
object MiFitnessBandLanguagePatch : BytecodePatch(
    setOf(MiFitnessBandLanguageFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        MiFitnessBandLanguageFingerprint.result?.mutableMethod?.apply {
            this.replaceInstruction(17,
                """
                    const-string v3, "en_gb"
                """
                    .trimIndent())
        } ?: throw MiFitnessBandLanguageFingerprint.exception
    }
}
