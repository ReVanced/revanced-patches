package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object PlayerControlsVisibilityFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("Z", "Z"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("YouTubeControlsOverlay;")
    }
)