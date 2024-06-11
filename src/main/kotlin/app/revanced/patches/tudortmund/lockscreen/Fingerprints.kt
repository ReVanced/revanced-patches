package app.revanced.patches.tudortmund.lockscreen

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val brightnessFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("V")
    parameters()
    custom { method, classDef ->
        method.name == "run" &&
            method.definingClass.contains("/ScreenPlugin\$") &&
            classDef.fields.any { it.type == "Ljava/lang/Float;" }
    }
}
