package app.revanced.patches.strava.quickedit

import app.revanced.patcher.fingerprint

internal val getHasAccessToQuickEditFingerprint = fingerprint {
    returns("Z")
    custom { method, _ ->
        method.name == "getHasAccessToQuickEdit"
    }
}
