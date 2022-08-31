package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility

@Name("text-component-spec-parent-fingerprint")
@MatchingMethod(
    "Lnvy;", "e"
)
@DirectPatternScanMethod
@ReturnYouTubeDislikeCompatibility
@Version("0.0.1")
object TextComponentSpecParentFingerprint : MethodFingerprint(
    strings = listOf("TextComponentSpec: No converter for extension: ")
)