package app.revanced.patches.youtube.misc.playeroverlay.fingerprint

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object PlayerOverlaysOnFinishInflateFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    customFingerprint = { methodDef, _ ->
    methodDef.definingClass.endsWith("/YouTubePlayerOverlaysLayout;")
        && methodDef.name == "onFinishInflate"
    },
)
