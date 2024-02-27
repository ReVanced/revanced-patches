package app.revanced.patches.youtube.flyoutpanel.oldspeedlayout.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object PlaybackRateBottomSheetClassFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("[L", "I"),
    strings = listOf("menu_item_playback_speed")
)