package app.revanced.patches.instagram.profile.copybio

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val copyBioTextPatch = bytecodePatch(
    name = "Copy bio text",
    description = "Makes user bio text selectable and copyable on profile pages.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        profileBioMethod.returnEarly()
    }
}
