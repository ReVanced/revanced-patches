package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.initializeNavigationButtonsListMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Lcom/instagram/common/session/UserSession;", "Z")
    returnType("Ljava/util/List;")
}

internal val BytecodePatchContext.navigationButtonsEnumMethod by gettingFirstImmutableMethodDeclaratively(
    "fragment_clips",
    "fragment_feed",
    "fragment_news",
    "fragment_search",
)
