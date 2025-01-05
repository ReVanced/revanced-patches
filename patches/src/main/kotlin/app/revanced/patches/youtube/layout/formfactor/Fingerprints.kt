package app.revanced.patches.youtube.layout.formfactor

import app.revanced.patcher.FieldCallFilter
import app.revanced.patcher.fingerprint
import app.revanced.patches.youtube.layout.formfactor.formFactorEnumConstructorFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val formFactorEnumConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    strings(
        "UNKNOWN_FORM_FACTOR",
        "SMALL_FORM_FACTOR",
        "LARGE_FORM_FACTOR",
        "AUTOMOTIVE_FORM_FACTOR"
    )
}

internal val createPlayerRequestBodyWithModelFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    instructions(
        FieldCallFilter("Landroid/os/Build;", "MODEL", "Ljava/lang/String;"),
        FieldCallFilter(
            definingClass = { context -> with(context) { formFactorEnumConstructorFingerprint.originalClassDef.type } },
            type = { "I" },
            maxInstructionsBefore = 50
        )
    )
}
