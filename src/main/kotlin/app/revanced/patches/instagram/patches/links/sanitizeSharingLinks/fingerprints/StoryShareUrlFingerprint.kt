package app.revanced.patches.instagram.patches.links.sanitizeSharingLinks.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object StoryShareUrlFingerprint : MethodFingerprint(
    strings = listOf("story_item_to_share_url"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "parseFromJson"
    },
)
