package app.revanced.patches.tudortmund.lockscreen.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val brightnessFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC.value)
    returns("V")
    parameters()
    custom { method, classDef ->
        method.name == "run" &&
            method.definingClass.contains("/ScreenPlugin\$") &&
            classDef.fields.any { it.type == "Ljava/lang/Float;" }
    }
}
