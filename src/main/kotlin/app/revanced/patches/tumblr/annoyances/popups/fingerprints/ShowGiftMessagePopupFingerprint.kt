package app.revanced.patches.tumblr.annoyances.popups.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// This method is responsible for loading and displaying the visual Layout of the Gift Message Popup.
internal val showGiftMessagePopupFingerprint = methodFingerprint {
    accessFlags(AccessFlags.FINAL, AccessFlags.PUBLIC)
    returns("V")
    strings("activity", "anchorView", "textMessage")
}
