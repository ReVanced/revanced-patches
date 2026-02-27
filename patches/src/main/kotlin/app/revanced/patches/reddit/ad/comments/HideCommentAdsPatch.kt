package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

val hideCommentAdsPatch = bytecodePatch(
    description = "Removes ads in the comments."
) {

    apply {
        hideCommentAdsMethod.replaceInstructions(0, "return-object p1")
    }
}
