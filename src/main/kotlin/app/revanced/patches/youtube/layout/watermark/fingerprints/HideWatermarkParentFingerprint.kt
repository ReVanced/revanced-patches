package app.revanced.patches.youtube.layout.watermark.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.watermark.annotations.HideWatermarkCompatibility
import org.jf.dexlib2.AccessFlags

@Name("hide-watermark-parent-signature")
@HideWatermarkCompatibility
@Version("0.0.1")
object HideWatermarkParentFingerprint : MethodFingerprint (
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, strings = listOf("player_overlay_in_video_programming")
)
