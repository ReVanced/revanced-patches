package app.revanced.patches.youtube.layout.watermark.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object HideWatermarkParentFingerprint : MethodFingerprint (
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, strings = listOf("player_overlay_in_video_programming")
)
