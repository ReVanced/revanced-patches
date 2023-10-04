package app.revanced.patches.tudortmund.lockscreen.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object BrightnessFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC.value,
    parameters = listOf(),
    customFingerprint = { method, classDef ->
        method.name == "run"
                && method.definingClass.contains("/ScreenPlugin\$")
                && classDef.fields.any { it.type == "Ljava/lang/Float;" }
    }
)
