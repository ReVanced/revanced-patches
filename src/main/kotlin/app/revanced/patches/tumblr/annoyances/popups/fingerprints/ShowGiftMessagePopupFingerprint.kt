package app.revanced.patches.tumblr.annoyances.popups.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// This method is responsible for loading and displaying the visual Layout of the Gift Message Popup.
internal object ShowGiftMessagePopupFingerprint : MethodFingerprint(
    strings = listOf("activity", "anchorView", "textMessage"),
    returnType = "V",
    accessFlags = AccessFlags.FINAL or AccessFlags.PUBLIC
)