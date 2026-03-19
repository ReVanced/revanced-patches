package app.revanced.patches.amznmusic.misc.skips

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlimitedSkipsPatch = bytecodePatch(
    name = "Unlimited track skipping",
    description = "Unlocks the ability to skip tracks without restriction.",
) {
    compatibleWith("com.amazon.mp3")

    apply {
        firstMethod { name == "getRule" && definingClass == $$"Lcom/amazon/music/freetier/featuregating/FMPMFeatureGating$STATION_UNLIMITED_SKIPS;" }
        .addInstructions(0,
        """
            new-instance p0, Lcom/amazon/music/platform/featuregate/rules/TrueRule;
            invoke-direct {p0}, Lcom/amazon/music/platform/featuregate/rules/TrueRule;-><init>()V
            return-object p0
        """)
    }
}
