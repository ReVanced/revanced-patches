package app.revanced.patches.youtube.layout.formfactor

import app.revanced.patcher.FieldFilter
import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val formFactorEnumConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    strings(
        "UNKNOWN_FORM_FACTOR",
        "SMALL_FORM_FACTOR",
        "LARGE_FORM_FACTOR",
        "AUTOMOTIVE_FORM_FACTOR"
    )
}

internal val createPlayerRequestBodyWithModelFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    instructions(
        FieldFilter("Landroid/os/Build${'$'}VERSION;", "RELEASE", "Ljava/lang/String;"),
        FieldFilter("Landroid/os/Build;", "MODEL", "Ljava/lang/String;"),
    )
}
