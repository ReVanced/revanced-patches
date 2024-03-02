package app.revanced.patches.reddit.ad.comments

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.ad.comments.fingerprints.HideCommentAdsFingerprint

@Patch(description = "Removes ads in the comments.")
object HideCommentAdsPatch : BytecodePatch(
    setOf(HideCommentAdsFingerprint)
) {
    // Returns a blank object instead of the comment ad.
    override fun execute(context: BytecodeContext) = HideCommentAdsFingerprint.result?.mutableMethod?.addInstructions(
        0, """
            new-instance v0, Ljava/lang/Object;
            invoke-direct {v0}, Ljava/lang/Object;-><init>()V
            return-object v0
            """
    ) ?: throw HideCommentAdsFingerprint.exception
}
