package app.revanced.patches.tumblr.annoyances.popups

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

// This method is responsible for loading and displaying the visual Layout of the Gift Message Popup.
internal val BytecodePatchContext.showGiftMessagePopupMethod by gettingFirstMethodDeclaratively("activity", "anchorView", "textMessage") {
    accessFlags(AccessFlags.FINAL, AccessFlags.PUBLIC)
    returnType("V")
}