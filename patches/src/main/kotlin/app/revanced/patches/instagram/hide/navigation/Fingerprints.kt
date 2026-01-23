
package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.initializeNavigationButtonsListMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Lcom/instagram/common/session/UserSession;", "Z")
    returnType("Ljava/util/List;")
}

internal val navigationButtonsEnumClassDef = fingerprint {
    strings("FEED", "fragment_feed", "SEARCH", "fragment_search")
}
