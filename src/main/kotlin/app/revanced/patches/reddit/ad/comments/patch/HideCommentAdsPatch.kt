package app.revanced.patches.reddit.ad.comments.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.reddit.ad.comments.fingerprints.HideCommentAdsFingerprint

@Name("hide-comment-ads")
@Description("Removes all comment ads.")
@Version("0.0.1")
class HideCommentAdsPatch : BytecodePatch(
    listOf(HideCommentAdsFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val method = HideCommentAdsFingerprint.result!!.mutableMethod
        // Returns a blank object instead of the comment ad.
        method.addInstructions(
            0,
            """
            new-instance v0, Ljava/lang/Object;
            invoke-direct {v0}, Ljava/lang/Object;-><init>()V
            return-object v0
            """
        )
        return PatchResultSuccess()
    }
}
