package app.revanced.patches.messenger.layout

import app.revanced.patcher.fingerprint

internal val isFacebookButtonEnabledFingerprint = fingerprint {
    parameters()
    returns("Z")
    strings("FacebookButtonTabButtonImplementation")
}
