package app.revanced.patches.youtube.layout.autoplaybutton.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.autoplaybutton.annotations.AutoplayButtonCompatibility

@Name("layout-constructor-fingerprint")
@AutoplayButtonCompatibility
@Version("0.0.1")
object LayoutConstructorFingerprint : MethodFingerprint(
    strings = listOf("1.0x"),
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("YouTubeControlsOverlay;")
    }
)