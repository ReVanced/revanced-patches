package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val lithoThemeFingerprint by fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/graphics/Rect;")
    instructions(
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            definingClass = "this",
            type = "Landroid/graphics/Path;"
        ),

        methodCall(
            definingClass = "this",
            name = "isStateful",
            returnType = "Z",
            maxAfter = 5
        ),

        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "this",
            type = "Landroid/graphics/Paint",
            maxAfter = 5
        ),
        methodCall(
            smali = "Landroid/graphics/Paint;->setColor(I)V",
            maxAfter = 0
        )
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
        literal(45412406L)
    )
}

internal val splashScreenStyleFingerprint by fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    instructions(
        literal(269032877L)
    )
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/MainActivity;")
    }
}
