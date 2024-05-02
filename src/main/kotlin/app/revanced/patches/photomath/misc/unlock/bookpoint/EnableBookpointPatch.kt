package app.revanced.patches.photomath.misc.unlock.bookpoint

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.photomath.misc.unlock.bookpoint.fingerprints.IsBookpointEnabledFingerprint

@Patch(description = "Enables textbook access")
internal object EnableBookpointPatch : BytecodePatch(
    setOf(IsBookpointEnabledFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        IsBookpointEnabledFingerprint.result?.mutableMethod?.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        ) ?: throw IsBookpointEnabledFingerprint.exception
}