package app.revanced.patches.instagram.patches.links.sanitizeSharingLinks.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object ProfileShareUrlFingerprint : MethodFingerprint(
    strings = listOf("profile_to_share_url"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "parseFromJson"
    },
)
