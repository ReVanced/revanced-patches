package app.revanced.patches.all.analytics.statsig

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.analytics.statsig.fingerprints.StatsigClientFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable Statsig analytics SDK"
)
@Suppress("unused")
object DisableStatsig : BytecodePatch(
setOf(StatsigClientFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        StatsigClientFingerprint.resultOrThrow().mutableMethod.addInstructions(0,"return-void")
    }
}