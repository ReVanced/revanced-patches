package app.revanced.patches.photomath.misc.bookpoint.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.photomath.misc.bookpoint.fingerprints.IsBookpointEnabledFingerprint

@Description("Enables textbook access")
class EnableBookpointPatch : BytecodePatch(listOf(IsBookpointEnabledFingerprint)) {
    override fun execute(context: BytecodeContext) =
        IsBookpointEnabledFingerprint.result?.mutableMethod?.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        ) ?: throw IsBookpointEnabledFingerprint.exception
}