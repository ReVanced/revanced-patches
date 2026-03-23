package app.revanced.patches.instagram.hide.comments

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideCommentButtonPatch = bytecodePatch(
    name = "Hide comment button",
    description = "Hides the comment icon button on feed posts, reels, and stories.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        commentButtonMethod.returnEarly()
    }
}
