package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

val hideCommentAdsPatch = bytecodePatch(
    description = "Removes ads in the comments.",
) {

    execute {
        hideCommentAdsFingerprint.classDef.methods.find { method ->
            method.name == "<init>" &&
                method.parameterTypes.firstOrNull() == "Ljava/util/List;"
        }!!.addInstructions(
            0,
            """
                new-instance p1, Ljava/util/ArrayList;
                invoke-direct {p1}, Ljava/util/ArrayList;-><init>()V
            """,
        )
    }
}
