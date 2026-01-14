package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Sanitize sharing links` by creatingBytecodePatch(
    description = "Removes the tracking query parameters from shared links.",
) {
    compatibleWith(
        "com.twitter.android"(
            "10.60.0-release.0",
            "10.86.0-release.0",
        )
    )

    apply {
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
