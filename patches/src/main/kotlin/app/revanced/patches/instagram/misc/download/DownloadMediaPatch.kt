package app.revanced.patches.instagram.misc.download

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val downloadMediaPatch = bytecodePatch(
    name = "Download media",
    description = "Allows downloading all media (photos, videos, reels) regardless of creator's download settings.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        downloadAllowedMethodMatch.method.returnEarly()
    }
}
