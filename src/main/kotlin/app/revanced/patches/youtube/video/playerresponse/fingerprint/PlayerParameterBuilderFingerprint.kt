package app.revanced.patches.youtube.video.playerresponse.fingerprint

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object PlayerParameterBuilderFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "L",
    strings = listOf("psps"),
    customFingerprint = { methodDef, _ ->
        methodDef.parameters.isNotEmpty()
    }
)