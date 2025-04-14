package app.revanced.patches.reddit.customclients.redditisfun.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

// Based on: https://github.com/ReVanced/revanced-patches/issues/661#issuecomment-2549674017
val fakePremiumPatch = bytecodePatch(
    name = "Fake reddit premium",
    description = "Allows using pro features without ads."
) {
    compatibleWith(
        "com.andrewshu.android.reddit",
    )

    execute {
        userPremiumFingerprint.method.returnEarly(true)
    }
}
