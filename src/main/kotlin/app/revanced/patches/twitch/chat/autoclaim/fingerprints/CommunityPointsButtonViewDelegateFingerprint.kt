package app.revanced.patches.twitch.chat.autoclaim.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CommunityPointsButtonViewDelegateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("CommunityPointsButtonViewDelegate;")
                && methodDef.name == "showClaimAvailable"
    }
)