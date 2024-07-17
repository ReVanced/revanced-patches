package app.revanced.patches.all.analytics.google

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.analytics.google.fingerprints.AnalyticsInitFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable Google Analytics"
)
@Suppress("unused")
object DisableGoogleAnalytics : BytecodePatch(
    setOf(AnalyticsInitFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // Empties the "context" argument to force an exception
        AnalyticsInitFingerprint.resultOrThrow().mutableMethod.addInstructions(0,"const/4 p0, 0x0")
    }
}