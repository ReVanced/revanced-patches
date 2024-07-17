package app.revanced.patches.all.analytics.segment

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.analytics.segment.fingerprints.SegmentBuilderFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable Segment analytics SDK",
    use = false,
)
@Suppress("unused")
object DisableSegment : BytecodePatch(
    setOf(SegmentBuilderFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // Empties the writeKey parameter to abort initialization
        SegmentBuilderFingerprint.resultOrThrow().mutableMethod.addInstructions(0,"const-string p2, \"\"")
    }
}