package app.revanced.patches.youtube.layout.reels.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.youtube.layout.reels.annotations.HideReelsCompatibility
import org.jf.dexlib2.AccessFlags

@Name("hide-reels-fingerprint")
@MatchingMethod(
    "Ljvy", "<init>"
)
@FuzzyPatternScanMethod(3) // FIXME: Test this threshold and find the best value.
@HideReelsCompatibility
@Version("0.0.1")
object HideReelsFingerprint : MethodFingerprint(
    null, AccessFlags.PROTECTED or AccessFlags.FINAL, listOf("L", "L"), null,
    listOf("multiReelDismissalCallback", "reelItemRenderers", "reelDismissalInfo")
)