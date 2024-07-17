package app.revanced.patches.all.analytics.comscore

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.analytics.comscore.fingerprints.ComScoreSetupFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable ComScore analytics SDK"
)
@Suppress("unused")
object DisableComScore : BytecodePatch(
    setOf(ComScoreSetupFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        ComScoreSetupFingerprint.resultOrThrow().mutableMethod.addInstructions(0, "return-void")
    }
}