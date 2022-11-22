package app.revanced.patches.youtube.layout.reels.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object HideReelsFingerprint : MethodFingerprint(
    access = AccessFlags.PROTECTED or AccessFlags.FINAL, parameters = listOf("L", "L"),
    strings = listOf("multiReelDismissalCallback", "reelItemRenderers", "reelDismissalInfo")
)