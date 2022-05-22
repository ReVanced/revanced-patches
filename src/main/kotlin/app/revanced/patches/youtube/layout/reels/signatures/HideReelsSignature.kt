package app.revanced.patches.youtube.layout.reels.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.layout.reels.annotations.HideReelsCompatibility
import org.jf.dexlib2.AccessFlags

@Name("hide-reels-signature")
@MatchingMethod(
    "Ljvy", "<init>"
)
@FuzzyPatternScanMethod(3) // FIXME: Test this threshold and find the best value.
@HideReelsCompatibility
@Version("0.0.1")
object HideReelsSignature : MethodSignature(
    null, AccessFlags.PROTECTED or AccessFlags.FINAL, listOf("L", "L"), null,
    listOf("multiReelDismissalCallback", "reelItemRenderers", "reelDismissalInfo")
)