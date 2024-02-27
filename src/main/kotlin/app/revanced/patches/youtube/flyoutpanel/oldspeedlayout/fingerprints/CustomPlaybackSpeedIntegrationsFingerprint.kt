package app.revanced.patches.youtube.flyoutpanel.oldspeedlayout.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object CustomPlaybackSpeedIntegrationsFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.STATIC,
    parameters = emptyList(),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("/CustomPlaybackSpeedPatch;") && methodDef.name == "showOldPlaybackSpeedMenu" }
)