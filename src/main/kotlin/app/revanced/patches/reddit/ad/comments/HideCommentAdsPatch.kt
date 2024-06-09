package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.ad.comments.fingerprints.hideCommentAdsFingerprint

@Suppress("unused")
val hideCommentAdsPatch = bytecodePatch(
    description = "Removes ads in the comments.",
) {
    val hideCommentAdsResult by hideCommentAdsFingerprint

    execute {
        hideCommentAdsResult.mutableMethod.addInstructions(
            0,
            """
                new-instance v0, Ljava/lang/Object;
                invoke-direct {v0}, Ljava/lang/Object;-><init>()V
                return-object v0
            """,
        )
    }
}
