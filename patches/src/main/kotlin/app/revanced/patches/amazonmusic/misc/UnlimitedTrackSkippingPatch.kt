package app.revanced.patches.amazonmusic.misc

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlimitedTrackSkippingPatch = bytecodePatch(
    name = "Unlimited track skipping",
    description = "Unlocks the ability to skip tracks without restriction.",
) {
    compatibleWith("com.amazon.mp3")

    apply {
        getRuleMethod.addInstructions(0,
        """
            new-instance p0, Lcom/amazon/music/platform/featuregate/rules/TrueRule;
            invoke-direct {p0}, Lcom/amazon/music/platform/featuregate/rules/TrueRule;-><init>()V
            return-object p0
        """)
    }
}
