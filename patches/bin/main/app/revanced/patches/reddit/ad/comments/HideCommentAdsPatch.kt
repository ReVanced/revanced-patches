package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

val hideCommentAdsPatch = bytecodePatch(
    description = "Removes ads in the comments.",
) {

    execute {
        hideCommentAdsFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Ljava/lang/Object;
                invoke-direct {v0}, Ljava/lang/Object;-><init>()V
                return-object v0
            """,
        )
    }
}
