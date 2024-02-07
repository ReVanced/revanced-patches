package app.revanced.patches.mifitness

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.mifitness.fingerprints.MiFitnessLoginFingerprint
import app.revanced.util.exception

@Patch(
    name = "Allow login to Xiaomi in Mi Fitness",
    description = "Fixes login for uncertified Mi Fitness app.",
    compatiblePackages = [CompatiblePackage("com.xiaomi.wearable")]
)
@Suppress("unused")
object MiFitnessLoginPatch : BytecodePatch(
    setOf(MiFitnessLoginFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        MiFitnessLoginFingerprint.result?.mutableMethod?.apply {
            this.addInstruction(0,
                """
                    const/4 p2, 0x0
                """.trimIndent())
        } ?: throw MiFitnessLoginFingerprint.exception
    }
}
