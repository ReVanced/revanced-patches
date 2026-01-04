package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

val hideCommentAdsPatch = bytecodePatch(
    description = "Removes ads in the comments.",
) {

    execute {
        hideCommentAdsFingerprint.method.replaceInstructions(0, "return-object p1")
    }
}
