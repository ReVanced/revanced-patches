package app.revanced.patches.instagram.patches.links.sanitizeSharingLinks.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object HighlightsShareUrlFingerprint : MethodFingerprint(
    strings = listOf("story_highlights_to_share_url"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "parseFromJson"
    },
)
