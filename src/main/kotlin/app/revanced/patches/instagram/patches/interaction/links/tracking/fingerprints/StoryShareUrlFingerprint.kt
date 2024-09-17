package app.revanced.patches.instagram.patches.interaction.links.tracking.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object StoryShareUrlFingerprint : MethodFingerprint(
    strings = listOf("story_item_to_share_url"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "parseFromJson"
    },
)
