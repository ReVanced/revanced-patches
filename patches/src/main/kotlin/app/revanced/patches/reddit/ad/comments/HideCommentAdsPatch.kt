package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Hide comment ads` by creatingBytecodePatch(
    description = "Removes ads in the comments."
) {

    apply {
        hideCommentAdsMethod.replaceInstructions(0, "return-object p1")
    }
}
