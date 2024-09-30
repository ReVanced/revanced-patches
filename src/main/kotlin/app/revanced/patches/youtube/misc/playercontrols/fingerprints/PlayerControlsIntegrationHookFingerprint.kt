package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object PlayerControlsIntegrationHookFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    returnType = "V",
    parameters = listOf("Z"),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "fullscreenButtonVisibilityChanged" &&
                classDef.type == "Lapp/revanced/integrations/youtube/patches/PlayerControlsPatch;"
    }
)