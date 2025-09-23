
package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.fingerprint

internal val initializeNavigationButtonsListFingerprint by fingerprint {
    returns("Ljava/util/List;")
    parameters("Lcom/instagram/common/session/UserSession;", "Z")
    strings("Nav3")
}

internal val navigationButtonsEnumClassDef by fingerprint {
    strings("FEED", "fragment_feed", "SEARCH", "fragment_search")
}
