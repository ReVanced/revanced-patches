package app.revanced.patches.instagram.patches.links.sanitizeSharingLinks.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object LiveShareUrlFingerprint : MethodFingerprint(
    strings = listOf("live_to_share_url"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "parseFromJson"
    },
)
