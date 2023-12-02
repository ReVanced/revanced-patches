package app.revanced.patches.twitch.chat.autoclaim.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CommunityPointsButtonViewDelegateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("CommunityPointsButtonViewDelegate;")
                && methodDef.name == "showClaimAvailable"
    }
)