package app.revanced.patches.youtube.layout.hide.signintotvpopup

import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceType

internal val signInToTvPopupFingerprint = fingerprint {
    returns("Z")
    parameters("Ljava/lang/String;", "Z", "L")
    instructions(
        resourceLiteral(
            ResourceType.STRING,
            "mdx_seamless_tv_sign_in_drawer_fragment_title",
        ),
    )
}
