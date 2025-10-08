package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS
import app.revanced.patches.shared.PATCH_NAME_SANITIZE_SHARING_LINKS

@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch(
    name = PATCH_NAME_SANITIZE_SHARING_LINKS,
    description = PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS,
) {
    compatibleWith(
        "com.twitter.android"(
            "10.60.0-release.0",
            "10.86.0-release.0",
        )
    )

    execute {
        sanitizeSharingLinksFingerprint.method.addInstructions(
            0,
            """
                # Method takes in a link (string, param 0) and then appends the tracking query params,
                # so all we need to do is return back the passed-in string
                return-object p0
            """,
        )
    }
}
