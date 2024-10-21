package app.revanced.patches.instagram.patches.interaction.links.tracking.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ProfileShareUrlFingerprint : MethodFingerprint(
    strings = listOf("profile_to_share_url"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "parseFromJson"
    },
)
