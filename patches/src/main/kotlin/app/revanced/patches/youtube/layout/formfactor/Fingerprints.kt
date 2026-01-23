package app.revanced.patches.youtube.layout.formfactor

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.formFactorEnumConstructorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    strings(
        "UNKNOWN_FORM_FACTOR",
        "SMALL_FORM_FACTOR",
        "LARGE_FORM_FACTOR",
        "AUTOMOTIVE_FORM_FACTOR",
    )
}
