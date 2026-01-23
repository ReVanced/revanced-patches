package app.revanced.patches.tudortmund.lockscreen

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val brightnessFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returnType("V")
    parameterTypes()
    custom { method, classDef ->
        method.name == "run" &&
            method.definingClass.contains("/ScreenPlugin\$") &&
            classDef.fields.any { it.type == "Ljava/lang/Float;" }
    }
}
