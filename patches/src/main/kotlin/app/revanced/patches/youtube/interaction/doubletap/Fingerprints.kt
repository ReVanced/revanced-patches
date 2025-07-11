package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val doubleTapInfoGetSeekSourceFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Z")
    returns("L")  // Enum SeekSource, but name obfuscated.
    opcodes(
        Opcode.IF_EQZ,
        Opcode.SGET_OBJECT,
        Opcode.RETURN_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    custom { _, classDef ->
        classDef.fields.count() == 4
    }
}

internal val doubleTapInfoCtorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters(
        "Landroid/view/MotionEvent;",
        "I",
        "Z",
        "Lj\$/time/Duration;"
    )
}
