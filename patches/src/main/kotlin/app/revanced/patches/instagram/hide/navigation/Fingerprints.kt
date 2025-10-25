
package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext

internal val initializeNavigationButtonsListFingerprint = fingerprint {
    parameters("Lcom/instagram/common/session/UserSession;", "Z")
    returns("Ljava/util/List;")
}

private val navigationButtonsEnumClassDef = fingerprint {
    strings("FEED", "fragment_feed", "SEARCH", "fragment_search")
}

context(BytecodePatchContext)
internal val navigationButtonsEnumInitFingerprint get() = fingerprint {
    custom { method, classDef ->
        method.name == "<init>"
                && classDef == navigationButtonsEnumClassDef.classDef
    }
}
