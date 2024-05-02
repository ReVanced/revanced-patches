package app.revanced.patches.twitch.chat.autoclaim.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val communityPointsButtonViewDelegateFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("CommunityPointsButtonViewDelegate;") &&
            methodDef.name == "showClaimAvailable"
    }
}
