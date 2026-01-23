package app.revanced.patches.facebook.ads.story

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Hide story ads` by creatingBytecodePatch(
    description = "Hides the ads in the Facebook app stories.",
) {
    compatibleWith("com.facebook.katana")

    apply {
        setOf(
            fetchMoreAdsFingerprint,
            adsInsertionFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.replaceInstruction(0, "return-void")
        }
    }
}
