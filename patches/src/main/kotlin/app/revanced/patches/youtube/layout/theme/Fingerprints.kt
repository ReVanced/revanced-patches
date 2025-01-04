package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val lithoThemeFingerprint by fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/graphics/Rect;")
    opcodes(
        Opcode.APUT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IGET_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_OBJECT,
        Opcode.IGET,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
    custom { method, _ ->
        method.name == "onBoundsChange"
    }
}

internal val themeHelperDarkColorFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { method, _ ->
        method.name == "darkThemeResourceName" &&
            method.definingClass == "Lapp/revanced/extension/youtube/ThemeHelper;"
    }
}

internal val themeHelperLightColorFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { method, _ ->
        method.name == "lightThemeResourceName" &&
            method.definingClass == "Lapp/revanced/extension/youtube/ThemeHelper;"
    }
}

internal val useGradientLoadingScreenFingerprint by fingerprint {
    instructions(
        LiteralFilter(45412406L)
    )
}
