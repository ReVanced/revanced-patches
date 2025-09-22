
package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.fingerprint

internal val initializeTabArrayFingerprint = fingerprint {
    strings("Nav3")
    parameters("Lcom/instagram/common/session/UserSession;", "Z")
    returns("Ljava/util/List;")
}

