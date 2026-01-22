package app.revanced.patches.strava.groupkudos

import app.revanced.patcher.fingerprint

internal val initFingerprint = fingerprint {
    parameters("Lcom/strava/feed/view/modal/GroupTabFragment;" , "Z" , "Landroidx/fragment/app/FragmentManager;")
    custom { method, _ ->
        method.name == "<init>"
    }
}

internal val actionHandlerFingerprint = fingerprint {
    strings("state")
}
