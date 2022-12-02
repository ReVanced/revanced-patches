package app.revanced.patches.youtube.misc.fix.verticalscroll.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.youtube.misc.fix.verticalscroll.annotations.VerticalScrollCompatibility
import app.revanced.patches.youtube.misc.fix.verticalscroll.fingerprints.CanScrollVerticallyFingerprint

@Description("Fixes issues with scrolling on the home screen when the first component is of type EmptyComponent.")
@VerticalScrollCompatibility
@Version("0.0.1")
class VerticalScrollPatch : BytecodePatch(
    listOf(CanScrollVerticallyFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val result = CanScrollVerticallyFingerprint.result ?: return CanScrollVerticallyFingerprint.toErrorResult()

        result.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )

        return PatchResultSuccess()
    }
}
