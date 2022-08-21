package app.revanced.patches.youtube.layout.autoplaybutton.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.autoplaybutton.annotations.AutoplayButtonCompatibility

@Name("layout-constructor-fingerprint")
@MatchingMethod(
    "LYouTubeControlsOverlay;", "F"
)
@AutoplayButtonCompatibility
@Version("0.0.1")
object LayoutConstructorFingerprint : MethodFingerprint(
    null, null, null, null, listOf("1.0x"),
    { methodDef ->
        methodDef.definingClass.endsWith("YouTubeControlsOverlay;")
    }
)