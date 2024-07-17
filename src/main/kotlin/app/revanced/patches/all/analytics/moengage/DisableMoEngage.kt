package app.revanced.patches.all.analytics.moengage

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.analytics.moengage.fingerprints.MoEngageInitFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable MoEngage analytics SDK"
)
@Suppress("unused")
object DisableMoEngage : BytecodePatch(
    setOf(MoEngageInitFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        MoEngageInitFingerprint.resultOrThrow().mutableMethod.addInstructions(0, "return-void")
    }
}