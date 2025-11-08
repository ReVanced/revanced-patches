package app.revanced.patches.twitch.chat.autoclaim

import app.revanced.patcher.fingerprint

internal val communityPointsButtonViewDelegateFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("CommunityPointsButtonViewDelegate;") &&
            method.name == "showClaimAvailable"
    }
}
