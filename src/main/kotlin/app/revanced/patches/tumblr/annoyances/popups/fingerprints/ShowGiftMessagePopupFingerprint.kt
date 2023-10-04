package app.revanced.patches.tumblr.annoyances.popups.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

// This method is responsible for loading and displaying the visual Layout of the Gift Message Popup.
object ShowGiftMessagePopupFingerprint : MethodFingerprint(
    strings = listOf("activity", "anchorView"),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("GiftMessagePopup;") }
)