package app.revanced.patches.twitch.chat.autoclaim

import app.revanced.patcher.fingerprint.methodFingerprint

internal val communityPointsButtonViewDelegateFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("CommunityPointsButtonViewDelegate;") &&
            methodDef.name == "showClaimAvailable"
    }
}
