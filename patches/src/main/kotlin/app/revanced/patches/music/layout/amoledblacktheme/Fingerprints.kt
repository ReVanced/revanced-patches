package app.revanced.patches.music.layout.amoledblacktheme

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val themeHelperFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    opcodes(
        Opcode.CONST_STRING,
        Opcode.RETURN_OBJECT
    )
    custom { method, _ ->
        (method.name == "darkThemeResourceName" || method.name == "navigationBarColorResourceName") &&
                method.definingClass == "Lapp/revanced/extension/music/ThemeHelper;"
    }
}
