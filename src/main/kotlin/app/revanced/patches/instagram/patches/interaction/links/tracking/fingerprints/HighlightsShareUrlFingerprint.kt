package app.revanced.patches.instagram.patches.interaction.links.tracking.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object HighlightsShareUrlFingerprint : MethodFingerprint(
    strings = listOf("story_highlights_to_share_url"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "parseFromJson"
    },
)
