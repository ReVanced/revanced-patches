package app.revanced.patches.youtube.layout.watermark.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.watermark.annotations.HideWatermarkCompatibility
import org.jf.dexlib2.AccessFlags

@Name("hide-watermark-parent-signature")
@MatchingMethod(
    "Ljvy;", "<init>"
)
@FuzzyPatternScanMethod(3)
@HideWatermarkCompatibility
@Version("0.0.1")
object HideWatermarkParentFingerprint : MethodFingerprint (
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, null, null, listOf("player_overlay_in_video_programming"), null
)
